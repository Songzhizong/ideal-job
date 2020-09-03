package com.zzsong.job.common.message.payload;

import com.zzsong.job.common.exception.ParseException;
import com.zzsong.job.common.message.MessageType;
import com.zzsong.job.common.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;

/**
 * @author 宋志宗
 * @date 2020/8/20
 */
@Getter
@Setter
public class IdleBeatParam {
    public static String typeCode = MessageType.IDLE_BEAT.getCode();
    @Nonnull
    private String jobId = "";

    public String toMessageString() {
        return JsonUtils.toJsonString(this);
    }

    public static IdleBeatParam parseMessage(@Nonnull String message) throws ParseException {
        try {
            return JsonUtils.parseJson(message, IdleBeatParam.class);
        } catch (Exception exception) {
            throw new ParseException(exception);
        }
    }
}
