package cn.sh.ideal.job.scheduler.core.dispatch.handler.impl

import cn.sh.ideal.job.common.constants.HandleStatusEnum
import cn.sh.ideal.job.common.constants.RouteStrategyEnum
import cn.sh.ideal.job.common.constants.TriggerTypeEnum
import cn.sh.ideal.job.common.exception.VisibleException
import cn.sh.ideal.job.common.http.HttpMethod
import cn.sh.ideal.job.common.http.HttpScriptUtils
import cn.sh.ideal.job.common.utils.JsonUtils
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInfo
import cn.sh.ideal.job.scheduler.core.admin.entity.JobInstance
import cn.sh.ideal.job.scheduler.core.admin.service.JobInstanceService
import cn.sh.ideal.job.scheduler.core.dispatch.handler.ExecuteHandler
import cn.sh.ideal.job.scheduler.core.utils.WebClients
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.ExecutorService

/**
 * @author 宋志宗
 * @date 2020/8/28
 */
abstract class BaseHttpExecuteHandler(
    private val instanceService: JobInstanceService,
    private val jobCallbackThreadPool: ExecutorService) : ExecuteHandler {
  val log: Logger = LoggerFactory.getLogger(this.javaClass)

  private val webClient = WebClients.createWebClient(
      400, 400, 120_000)

  @Suppress("DuplicatedCode")
  override fun execute(instance: JobInstance,
                       jobInfo: JobInfo,
                       triggerType: TriggerTypeEnum,
                       customExecuteParam: String?) {
    val jobId = jobInfo.jobId
    val executeParam = customExecuteParam ?: jobInfo.executeParam
    if (executeParam.isBlank()) {
      log.info("任务: {} Http script为空", jobId)
      throw VisibleException("Http script为空")
    }
    val httpRequest = try {
      HttpScriptUtils.parse(executeParam)
    } catch (e: Exception) {
      log.info("任务: {} http script解析异常: {}", jobId, e.message)
      throw VisibleException("http script解析异常: ${e.message}")
    }
    val routeStrategy = jobInfo.routeStrategy
    val url = httpRequest.url
    val chooseServer = getAddressList(jobId, routeStrategy, url)
    if (chooseServer.isEmpty()) {
      log.info("任务: {} 选取远程服务为空", jobId)
      throw VisibleException("选取远程服务为空")
    }

    val method = httpRequest.method
    val queryString = httpRequest.queryString
    val headers = httpRequest.headers
    val body = httpRequest.body
    val multiValueMap = LinkedMultiValueMap<String, String>()
    var requestBody: Any? = null
    if (body?.isNotBlank() == true) {
      if ((body.startsWith("{") && body.endsWith("}"))
          || (body.startsWith("[") && body.endsWith("]"))) {
        requestBody = JsonUtils.parseJson(body, Any::class.java)
      } else {
        StringUtils.split(body, "&")
            .forEach { single ->
              StringUtils.split(single, "=", 2)
                  .let { sp ->
                    if (sp.size == 2) {
                      multiValueMap.add(sp[0], sp[1])
                    } else {
                      log.info("任务: {} http script body不合法")
                    }
                  }
            }
      }
    }
    val currentTimeMillis = System.currentTimeMillis()
    if (chooseServer.size == 1) {
      val uri = chooseServer[0]
      instance.executorInstance = uri
      val requestUri = if (queryString?.isNotBlank() == true) "$uri:$queryString" else uri
      val client = buildWebClientSpec(
          method, requestUri, requestBody, multiValueMap, headers)
      val bodyToMono = client.retrieve().bodyToMono(String::class.java)
      subscriptResponse(bodyToMono, instance, currentTimeMillis)
    } else {
      for (uri in chooseServer) {
        val jobInstance = JobInstance.createInitialized()
        jobInstance.parentId = instance.instanceId
        jobInstance.jobId = instance.jobId
        jobInstance.executorId = instance.executorId
        jobInstance.triggerType = triggerType
        jobInstance.schedulerInstance = instance.schedulerInstance
        jobInstance.executorHandler = instance.executorHandler
        jobInstance.executeParam = executeParam
        jobInstance.executorInstance = uri
        val requestUri = if (queryString?.isNotBlank() == true) "$uri:$queryString" else uri
        val client = buildWebClientSpec(
            method, requestUri, requestBody, multiValueMap, headers)
        val bodyToMono = client.retrieve().bodyToMono(String::class.java)
        subscriptResponse(bodyToMono, jobInstance, currentTimeMillis)
      }
    }
  }

  private fun subscriptResponse(bodyToMono: Mono<String>,
                                instance: JobInstance,
                                currentTimeMillis: Long) {
    bodyToMono.onErrorResume { e ->
      val errMsg = "${e.javaClass.name}:${e.message}"
      instance.handleStatus = HandleStatusEnum.ABNORMAL
      Mono.just(errMsg)
    }.map { result ->
      instance.result = result
      if (instance.handleStatus != HandleStatusEnum.ABNORMAL) {
        instance.handleStatus = HandleStatusEnum.COMPLETE
      }
    }.doFinally {
      instance.handleTime = currentTimeMillis
      instance.finishedTime = System.currentTimeMillis()
      instance.sequence = 2
      jobCallbackThreadPool.execute { instanceService.saveInstance(instance) }
    }.subscribe()
  }

  private fun buildWebClientSpec(method: HttpMethod,
                                 requestUri: String,
                                 requestBody: Any?,
                                 multiValueMap: LinkedMultiValueMap<String, String>,
                                 headers: HttpHeaders?): WebClient.RequestHeadersSpec<*> {
    val client = when (method) {
      HttpMethod.GET -> webClient.get().uri(requestUri)
      HttpMethod.DELETE -> webClient.delete().uri(requestUri)
      HttpMethod.POST -> {
        val requestBodySpec = webClient.post().uri(requestUri)
        if (requestBody != null) {
          requestBodySpec.body(BodyInserters.fromValue(requestBody))
        } else if (multiValueMap.isNotEmpty()) {
          BodyInserters.fromFormData(multiValueMap)
        }
        requestBodySpec
      }
      HttpMethod.PATCH -> {
        val requestBodySpec = webClient.patch().uri(requestUri)
        if (requestBody != null) {
          requestBodySpec.body(BodyInserters.fromValue(requestBody))
        } else if (multiValueMap.isNotEmpty()) {
          BodyInserters.fromFormData(multiValueMap)
        }
        requestBodySpec
      }
      HttpMethod.PUT -> {
        val requestBodySpec = webClient.put().uri(requestUri)
        if (requestBody != null) {
          requestBodySpec.body(BodyInserters.fromValue(requestBody))
        } else if (multiValueMap.isNotEmpty()) {
          BodyInserters.fromFormData(multiValueMap)
        }
        requestBodySpec
      }
      else -> {
        // 不应该发生的
        val message = "不合法的Http method: $method"
        log.error(message)
        throw VisibleException(message)
      }
    }
    if (headers != null) {
      client.headers {
        headers.forEach { name, values -> it[name] = values }
      }
    }
    return client
  }

  open fun getAddressList(jobId: Long,
                          routeStrategy: RouteStrategyEnum,
                          scriptUrl: String): List<String> {
    return listOf(scriptUrl)
  }
}