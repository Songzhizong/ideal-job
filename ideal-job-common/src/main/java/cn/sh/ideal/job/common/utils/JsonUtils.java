package cn.sh.ideal.job.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
public class JsonUtils {
    private static final DateTimeFormatter dateTimeFormatter
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.SIMPLIFIED_CHINESE);
    private static final DateTimeFormatter dateFormatter
            = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE);
    private static final DateTimeFormatter timeFormatter
            = DateTimeFormatter.ofPattern("HH:mm:ss.SSS", Locale.SIMPLIFIED_CHINESE);

    private static final SimpleModule javaTimeModule = new JavaTimeModule()
            .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
            .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter))
            .addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter))
            .addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter))
            .addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter))
            .addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));

    public static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(javaTimeModule)
            .findAndRegisterModules();

    public static final ObjectMapper ignoreNullMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(javaTimeModule)
            .findAndRegisterModules();

    public static SimpleModule getJavaTimeModule() {
        return javaTimeModule;
    }

    public static <T> String toJsonString(T t) {
        return toJsonString(t, false, false);
    }

    public static <T> String toJsonStringIgnoreNull(T t) {
        return toJsonString(t, true, false);
    }

    public static <T> String toJsonString(T t, boolean ignoreNull, boolean pretty) {
        ObjectMapper writer = JsonUtils.mapper;
        if (ignoreNull) {
            writer = JsonUtils.ignoreNullMapper;
        }
        try {
            if (pretty) {
                return writer.writerWithDefaultPrettyPrinter().writeValueAsString(t);
            } else {
                return writer.writeValueAsString(t);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseJson(String jsonString, Class<T> clazz) {
        try {
            return ignoreNullMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseJson(String jsonString, TypeReference<T> type) {
        try {
            return ignoreNullMapper.readValue(jsonString, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
