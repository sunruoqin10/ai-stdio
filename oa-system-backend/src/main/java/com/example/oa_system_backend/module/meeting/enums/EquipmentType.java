package com.example.oa_system_backend.module.meeting.enums;

public enum EquipmentType {
    PROJECTOR("projector", "投影仪"),
    WHITEBOARD("whiteboard", "白板"),
    VIDEO_CONFERENCE("video_conference", "视频会议设备"),
    AUDIO_SYSTEM("audio_system", "音响系统"),
    TV("tv", "电视"),
    TELEPHONE("telephone", "电话会议"),
    WATER_POT("water_pot", "茶水"),
    OTHER("other", "其他");

    private final String code;
    private final String name;

    EquipmentType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static EquipmentType fromCode(String code) {
        for (EquipmentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
