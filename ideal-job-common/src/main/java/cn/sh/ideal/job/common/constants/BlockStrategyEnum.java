package cn.sh.ideal.job.common.constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author 宋志宗
 * @date 2020/8/23
 */
public enum BlockStrategyEnum {
    /**
     * 并行
     */
    PARALLEL("并行"),
    /**
     * 丢弃后续调度
     */
    DISCARD("丢弃后续调度"),
    /**
     * 覆盖掉之前的调度
     */
    COVER("覆盖掉之前的调度"),
    /**
     * 串行执行
     */
    SERIAL("串行执行"),
    ;

    @Nullable
    public static BlockStrategyEnum valueOfName(@Nonnull String name) {
        String upperCase = name.toUpperCase();
        switch (upperCase) {
            case "SERIAL":
                return SERIAL;
            case "DISCARD":
                return DISCARD;
            case "COVER":
                return COVER;
            case "PARALLEL":
                return PARALLEL;
            default:
                return null;
        }
    }

    @Nonnull
    private final String desc;

    BlockStrategyEnum(@Nonnull String desc) {
        this.desc = desc;
    }

    @Nonnull
    public String getDesc() {
        return desc;
    }
}
