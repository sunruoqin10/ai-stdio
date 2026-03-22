package com.example.oa_system_backend.module.menu.vo;

import lombok.Data;

import java.util.List;

@Data
public class RouteMenuItem {

    private String path;

    private String name;

    private String component;

    private String redirect;

    private RouteMeta meta;

    private List<RouteMenuItem> children;

    @Data
    public static class RouteMeta {
        private String title;
        private String icon;
        private Boolean hidden;
        private Boolean keepAlive;
        private String[] permissions;
    }
}
