package com.zdzc.sender.Enum;

/**
 * 协议类型
 */
public enum ProtocolType {
    JT808("1", "部标808协议"), WRT("2", "沃瑞特C11协议");
    private String value;
    private String desc;

    ProtocolType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

}
