package com.yunhuakeji.attendance.constants;

/**
 * 系统错误码定义
 */
public enum ErrorCode {
    SUCCESS("000000", "success"),
    CLOCK_TIME_NOT_CONFIG("001000", "打卡时间未配置"),
    CHECK_TIME_NOT_CONFIG("001000", "查寝时间未配置"),
    CLOCK_ADDRESS_NOT_CONFIG("001000", "打卡地址未配置"),
    CLOCK_NOT_IN_CONFIG_AREA("001001", "未在打卡区域内"),
    NOT_NEED_TO_CLOCK("001001", "今天不需要打卡"),
    ACCOUNT_NOT_EXSIT("001001", "账号不存在"),
    ADD_TERM_TIME_REPEATED("001001", "添加学期时间重复"),
    CARE_CAN_NOT_DELETE("001001", "已关怀不能撤销"),
    DEVICE_ERROR("001001", "不是常用的打卡设备"),
    NOT_IN_CHECK_TIME("001001", "不在查寝时间范围内"),
    QR_CODE_IS_EXPIRE("001001", "二维码已过期"),
    INSTRUCTOR_HAS_CLOCK("001001", "今天已经打过卡了"),
    NOT_IN_TIME_RANGE("001001", "不在时间范围内"),
    INSTRUCTOR_NOT_EXSIT("001001", "辅导员不存在"),
    USER_HAS_ONLY_ONE_ROLE("001001", "一个用户只能有一个角色"),
    START_CARE_ONLY_ONCE_ONEDAY("001001", "一天同一个学生只能发起一次关怀"),
    PARAMS_ERROR("001001", "参数错误"),
    PASSWORD_ERROR("001001", "密码错误"),
    USERNAME_OR_PASSWORD_ERROR("001001", "用户名或者密码错误");

    private String code;
    private String desc;

    ErrorCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ErrorCode get(String code) {
        for (ErrorCode e : ErrorCode.values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
