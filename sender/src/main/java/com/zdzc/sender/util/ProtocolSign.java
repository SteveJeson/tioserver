package com.zdzc.sender.util;

public enum ProtocolSign {
    JT808("7E", "beginMark"), WRT("54", "beginMark");
    private String value;
    private String desc;

    private ProtocolSign(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}