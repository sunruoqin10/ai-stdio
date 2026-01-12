package com.oa.module.auth.vo;

import lombok.Data;
import java.util.List;

@Data
public class UserInfoVO {
    private String id;
    private String employeeNo;
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String departmentId;
    private String departmentName;
    private String position;
    private List<RoleVO> roles;
    private List<String> permissions;
}
