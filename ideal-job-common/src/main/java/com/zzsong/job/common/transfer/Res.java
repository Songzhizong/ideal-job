package com.zzsong.job.common.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.beans.Transient;
import java.io.Serializable;
import java.util.function.Function;

/**
 * @author 宋志宗 on 2019-05-14
 */
@SuppressWarnings({"unused", "UnusedReturnValue", "RedundantSuppression"})
public class Res<T> implements Serializable {
  private static final long serialVersionUID = 1L;

  /**
   * 唯一id
   */
  @Nullable
  private String id;

  /**
   * 是否成功
   */
  private boolean success;
  /**
   * 状态码
   */
  private int code = -1;
  /**
   * 提示信息
   */
  @Nonnull
  private String message = "";
  /**
   * 响应数据
   */
  @Nullable
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private T data;

  /**
   * 分页查询 页码
   */
  @Nullable
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer page;

  /**
   * 分页查询 页大小
   */
  @Nullable
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer size;

  /**
   * 分页查询 匹配总数
   */
  @Nullable
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer total;

  /**
   * 分页查询 总页数
   */
  @Nullable
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer totalPages;

  Res() {
  }

  private Res(@Nullable String id, boolean success,
              int code, @Nonnull String message, @Nullable T data,
              @Nullable Integer page, @Nullable Integer size,
              @Nullable Integer total, @Nullable Integer totalPages) {
    this.id = id;
    this.success = success;
    this.code = code;
    this.message = message;
    this.data = data;
    this.page = page;
    this.size = size;
    this.total = total;
    this.totalPages = totalPages;
  }

  public boolean isSuccess() {
    return success;
  }

  @Transient
  public boolean isFailure() {
    return !isSuccess();
  }

  // ================== 静态工厂方法 ================== ~
  @Nonnull
  public static <T> Res<T> create() {
    Res<T> res = new Res<>();
    res.setId("");
    return res;
  }

  @Nonnull
  public static <T> Res<T> success() {
    Res<T> res = new Res<>();
    res.setSuccess(true);
    res.setCode(CommonResMsg.SUCCESS.code());
    res.setMessage(CommonResMsg.SUCCESS.message());
    return res;
  }

  @Nonnull
  public static <T> Res<T> successWithId(@Nonnull String id) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(true);
    res.setCode(CommonResMsg.SUCCESS.code());
    res.setMessage(CommonResMsg.SUCCESS.message());
    return res;
  }

  @Nonnull
  public static <T> Res<T> success(@Nonnull String message) {
    Res<T> res = new Res<>();
    res.setSuccess(true);
    res.setCode(CommonResMsg.SUCCESS.code());
    res.setMessage(message);
    return res;
  }

  @Nonnull
  public static <T> Res<T> successWithId(@Nonnull String id, @Nonnull String message) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(true);
    res.setCode(CommonResMsg.SUCCESS.code());
    res.setMessage(message);
    return res;
  }

  @Nonnull
  public static <T> Res<T> success(@Nonnull ResMsg resMsg) {
    Res<T> res = new Res<>();
    res.setSuccess(true);
    res.setCode(resMsg.code());
    res.setMessage(resMsg.message());
    return res;
  }

  @Nonnull
  public static <T> Res<T> successWithId(@Nonnull String id, @Nonnull ResMsg resMsg) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(true);
    res.setCode(resMsg.code());
    res.setMessage(resMsg.message());
    return res;
  }

  @Nonnull
  public static <T> Res<T> success(@Nonnull ResMsg resMsg, @Nonnull String message) {
    Res<T> res = new Res<>();
    res.setSuccess(true);
    res.setCode(resMsg.code());
    res.setMessage(message);
    return res;
  }

  @Nonnull
  public static <T> Res<T> successWithId(@Nonnull String id,
                                         @Nonnull ResMsg resMsg,
                                         @Nonnull String message) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(true);
    res.setCode(resMsg.code());
    res.setMessage(message);
    return res;
  }

  @Nonnull
  public static <T> Res<T> data(@Nullable T data) {
    Res<T> res = new Res<>();
    res.setSuccess(true);
    res.setCode(CommonResMsg.SUCCESS.code());
    res.setMessage(CommonResMsg.SUCCESS.message());
    res.setData(data);
    return res;
  }

  @Nonnull
  public static <T> Res<T> dataWithId(@Nonnull String id, @Nonnull T data) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(true);
    res.setCode(CommonResMsg.SUCCESS.code());
    res.setMessage(CommonResMsg.SUCCESS.message());
    res.setData(data);
    return res;
  }

  @Nonnull
  public static <T> Res<T> data(@Nonnull T data, @Nonnull String message) {
    Res<T> res = new Res<>();
    res.setSuccess(true);
    res.setCode(CommonResMsg.SUCCESS.code());
    res.setMessage(message);
    res.setData(data);
    return res;
  }

  @Nonnull
  public static <T> Res<T> dataWithId(@Nonnull String id, T data, @Nonnull String message) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(true);
    res.setCode(CommonResMsg.SUCCESS.code());
    res.setMessage(message);
    res.setData(data);
    return res;
  }

  @Nonnull
  public static <T> Res<T> err() {
    Res<T> res = new Res<>();
    res.setSuccess(false);
    res.setCode(CommonResMsg.BAD_REQUEST.code());
    res.setMessage(CommonResMsg.BAD_REQUEST.message());
    return res;
  }

  @Nonnull
  public static <T> Res<T> errWithId(@Nonnull String id) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(false);
    res.setCode(CommonResMsg.BAD_REQUEST.code());
    res.setMessage(CommonResMsg.BAD_REQUEST.message());
    return res;
  }

  @Nonnull
  public static <T> Res<T> err(@Nonnull String message) {
    Res<T> res = new Res<>();
    res.setSuccess(false);
    res.setCode(CommonResMsg.BAD_REQUEST.code());
    res.setMessage(message);
    return res;
  }

  @Nonnull
  public static <T> Res<T> errWithId(@Nonnull String id, @Nonnull String message) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(false);
    res.setCode(CommonResMsg.BAD_REQUEST.code());
    res.setMessage(message);
    return res;
  }

  @Nonnull
  public static <T> Res<T> err(@Nonnull ResMsg resMsg) {
    Res<T> res = new Res<>();
    res.setSuccess(false);
    res.setCode(resMsg.code());
    res.setMessage(resMsg.message());
    return res;
  }

  @Nonnull
  public static <T> Res<T> errWithId(@Nonnull String id, @Nonnull ResMsg resMsg) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(false);
    res.setCode(resMsg.code());
    res.setMessage(resMsg.message());
    return res;
  }

  @Nonnull
  public static <T> Res<T> err(@Nonnull ResMsg resMsg, @Nonnull String message) {
    Res<T> res = new Res<>();
    res.setSuccess(false);
    res.setCode(resMsg.code());
    res.setMessage(message);
    return res;
  }

  @Nonnull
  public static <T> Res<T> errWithId(@Nonnull String id,
                                     @Nonnull ResMsg resMsg,
                                     @Nonnull String message) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(false);
    res.setCode(resMsg.code());
    res.setMessage(message);
    return res;
  }

  @Nonnull
  public static <T> Res<T> exception(@Nonnull Throwable t) {
    Res<T> res = new Res<>();
    res.setSuccess(false);
    res.setCode(CommonResMsg.INTERNAL_SERVER_ERROR.code());
    res.setMessage(t.getMessage());
    return res;
  }

  @Nonnull
  public static <T> Res<T> exceptionWithId(@Nonnull String id, @Nonnull Throwable t) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(false);
    res.setCode(CommonResMsg.INTERNAL_SERVER_ERROR.code());
    res.setMessage(t.getMessage());
    return res;
  }

  @Nonnull
  public static <T> Res<T> exception(@Nonnull String message) {
    Res<T> res = new Res<>();
    res.setSuccess(false);
    res.setCode(CommonResMsg.INTERNAL_SERVER_ERROR.code());
    res.setMessage(message);
    return res;
  }

  @Nonnull
  public static <T> Res<T> exceptionWithId(@Nonnull String id, @Nonnull String message) {
    Res<T> res = new Res<>();
    res.setId(id);
    res.setSuccess(false);
    res.setCode(CommonResMsg.INTERNAL_SERVER_ERROR.code());
    res.setMessage(message);
    return res;
  }

  @Nonnull
  public static <T> ResBuilder<T> builder() {
    return new ResBuilder<>();
  }

  @Nonnull
  public <R> Res<R> convertData(@Nonnull Function<T, R> function) {
    Res<R> retRes = new Res<>();
    retRes.setId(this.getId());
    retRes.setSuccess(this.isSuccess());
    retRes.setCode(this.getCode());
    retRes.setMessage(this.getMessage());
    if (this.getData() != null) {
      retRes.setData(function.apply(this.getData()));
    }
    retRes.setPage(this.getPage());
    retRes.setSize(this.getSize());
    retRes.setTotal(this.getTotal());
    retRes.setTotalPages(this.getTotalPages());
    return retRes;
  }

  @Nullable
  public String getId() {
    return id;
  }

  @Nonnull
  public Res<T> setId(@Nullable String id) {
    this.id = id;
    return this;
  }

  @Nonnull
  public Res<T> setSuccess(boolean success) {
    this.success = success;
    return this;
  }

  public int getCode() {
    return code;
  }

  @Nonnull
  public Res<T> setCode(int code) {
    this.code = code;
    return this;
  }

  @Nonnull
  public String getMessage() {
    return message;
  }

  @Nonnull
  public Res<T> setMessage(@Nonnull String message) {
    this.message = message;
    return this;
  }

  @Nullable
  public T getData() {
    return data;
  }

  @Nonnull
  public Res<T> setData(@Nullable T data) {
    this.data = data;
    return this;
  }

  @Nullable
  public Integer getPage() {
    return page;
  }

  @Nonnull
  public Res<T> setPage(@Nullable Integer page) {
    this.page = page;
    return this;
  }

  @Nullable
  public Integer getSize() {
    return size;
  }

  @Nonnull
  public Res<T> setSize(@Nullable Integer size) {
    this.size = size;
    return this;
  }

  @Nullable
  public Integer getTotal() {
    return total;
  }

  @Nonnull
  public Res<T> setTotal(@Nullable Integer total) {
    this.total = total;
    return this;
  }

  @Nullable
  public Integer getTotalPages() {
    return totalPages;
  }

  @Nonnull
  public Res<T> setTotalPages(@Nullable Integer totalPages) {
    this.totalPages = totalPages;
    return this;
  }


  public static class ResBuilder<T> {
    private String id;
    private boolean success;
    private int code = -1;
    private String message;
    private T data;
    private Integer page;
    private Integer size;
    private Integer total;
    private Integer totalPages;

    ResBuilder() {
    }

    public ResBuilder<T> id(String id) {
      this.id = id;
      return this;
    }

    public ResBuilder<T> success(boolean success) {
      this.success = success;
      return this;
    }

    public ResBuilder<T> code(int code) {
      this.code = code;
      return this;
    }

    public ResBuilder<T> message(String message) {
      this.message = message;
      return this;
    }

    public ResBuilder<T> data(@Nullable T data) {
      this.data = data;
      return this;
    }

    public ResBuilder<T> page(Integer page) {
      this.page = page;
      return this;
    }

    public ResBuilder<T> size(Integer size) {
      this.size = size;
      return this;
    }

    public ResBuilder<T> total(Integer total) {
      this.total = total;
      return this;
    }

    public ResBuilder<T> totalPages(Integer totalPages) {
      this.totalPages = totalPages;
      return this;
    }

    public Res<T> build() {
      return new Res<>(this.id, this.success, this.code, this.message,
          this.data, this.page, this.size, this.total, this.totalPages);
    }
  }
}
