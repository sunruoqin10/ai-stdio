# 部门管理 API 接口规范

> **模块**: department
> **版本**: v1.0.0
> **更新日期**: 2026-01-15

---

## 📡 API 接口列表

### 1. 查询接口

#### 1.1 获取部门列表

**接口地址**: `GET /api/departments`

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 | 示例值 |
|--------|------|------|------|--------|
| type | String | 否 | 返回类型: tree-树形, flat-扁平 | tree |
| keyword | String | 否 | 搜索关键词(部门名称/简称) | 技术 |
| status | String | 否 | 状态: active-正常, disabled-停用 | active |
| leaderId | String | 否 | 负责人ID | EMP001 |
| level | Integer | 否 | 层级(1-5) | 2 |
| page | Integer | 否 | 页码,默认1 | 1 |
| pageSize | Integer | 否 | 每页数量,默认20 | 20 |

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "DEPT0001",
        "name": "XX科技有限公司",
        "shortName": "总公司",
        "parentId": null,
        "parentName": null,
        "leaderId": "EMP20230115001",
        "leaderName": "张三",
        "level": 1,
        "sort": 1,
        "employeeCount": 150,
        "status": "active",
        "createdAt": "2026-01-01T00:00:00",
        "children": [
          {
            "id": "DEPT0002",
            "name": "技术部",
            "shortName": "技术",
            "parentId": "DEPT0001",
            "parentName": "XX科技有限公司",
            "leaderId": "EMP20230115001",
            "leaderName": "张三",
            "level": 2,
            "sort": 1,
            "employeeCount": 50,
            "status": "active",
            "createdAt": "2026-01-01T00:00:00",
            "children": []
          }
        ]
      }
    ],
    "total": 10,
    "page": 1,
    "pageSize": 20
  }
}
```

---

#### 1.2 获取部门详情

**接口地址**: `GET /api/departments/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 部门ID |

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": "DEPT0001",
    "name": "技术部",
    "shortName": "技术",
    "parentId": "DEPT0001",
    "parentName": "XX科技有限公司",
    "leaderId": "EMP20230115001",
    "leaderName": "张三",
    "leaderPosition": "技术总监",
    "leaderPhone": "13800138000",
    "leaderEmail": "zhangsan@example.com",
    "level": 2,
    "sort": 1,
    "establishedDate": "2020-01-01",
    "description": "负责公司技术研发工作",
    "icon": null,
    "employeeCount": 50,
    "childCount": 3,
    "status": "active",
    "createdAt": "2026-01-01T00:00:00",
    "createdBy": "ADMIN",
    "updatedAt": "2026-01-15T12:00:00",
    "updatedBy": "ADMIN"
  }
}
```

---

#### 1.3 获取子部门列表

**接口地址**: `GET /api/departments/{id}/children`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 父部门ID |

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "id": "DEPT0005",
      "name": "前端开发组",
      "shortName": "前端",
      "parentId": "DEPT0002",
      "parentName": "技术部",
      "leaderId": "EMP20230115004",
      "leaderName": "李四",
      "level": 3,
      "sort": 1,
      "employeeCount": 15,
      "status": "active",
      "createdAt": "2026-01-01T00:00:00"
    },
    {
      "id": "DEPT0006",
      "name": "后端开发组",
      "shortName": "后端",
      "parentId": "DEPT0002",
      "parentName": "技术部",
      "leaderId": "EMP20230115001",
      "leaderName": "张三",
      "level": 3,
      "sort": 2,
      "employeeCount": 25,
      "status": "active",
      "createdAt": "2026-01-01T00:00:00"
    }
  ]
}
```

---

#### 1.4 获取部门成员列表

**接口地址**: `GET /api/departments/{id}/employees`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 部门ID |

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "employeeId": "EMP20230115001",
      "employeeName": "张三",
      "employeeAvatar": "https://example.com/avatar1.jpg",
      "position": "技术总监",
      "status": "active",
      "isLeader": true,
      "joinDepartmentDate": "2020-01-01"
    },
    {
      "employeeId": "EMP20230115002",
      "employeeName": "李四",
      "employeeAvatar": "https://example.com/avatar2.jpg",
      "position": "前端工程师",
      "status": "active",
      "isLeader": false,
      "joinDepartmentDate": "2020-03-15"
    }
  ]
}
```

---

#### 1.5 获取部门统计信息

**接口地址**: `GET /api/departments/statistics`

**响应示例**:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "totalCount": 25,
    "level1Count": 5,
    "level2Count": 10,
    "level3Count": 8,
    "level4Count": 2,
    "maxLevel": 4,
    "withLeaderCount": 23,
    "totalEmployees": 500,
    "activeDepartmentCount": 24,
    "disabledDepartmentCount": 1
  }
}
```

---

### 2. 操作接口

#### 2.1 创建部门

**接口地址**: `POST /api/departments`

**请求体**:

```json
{
  "name": "前端开发组",
  "shortName": "前端",
  "parentId": "DEPT0002",
  "leaderId": "EMP20230115004",
  "sort": 1,
  "establishedDate": "2020-01-01",
  "description": "负责前端开发工作",
  "status": "active"
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 | 验证规则 |
|--------|------|------|------|----------|
| name | String | 是 | 部门名称 | 2-50字符,同级唯一 |
| shortName | String | 否 | 部门简称 | 2-20字符 |
| parentId | String | 否 | 上级部门ID | 必须存在 |
| leaderId | String | 是 | 负责人ID | 必须存在 |
| sort | Integer | 否 | 排序号 | ≥0, 默认0 |
| establishedDate | String | 否 | 成立日期 | YYYY-MM-DD |
| description | String | 否 | 部门描述 | 最多500字符 |
| icon | String | 否 | 部门图标URL | 最多500字符 |
| status | String | 否 | 状态 | active/disabled,默认active |

**响应示例**:

```json
{
  "code": 0,
  "message": "创建成功",
  "data": "DEPT0005"
}
```

---

#### 2.2 更新部门

**接口地址**: `PUT /api/departments/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 部门ID |

**请求体**:

```json
{
  "name": "前端开发组",
  "shortName": "前端",
  "leaderId": "EMP20230115005",
  "sort": 2,
  "description": "负责Web前端开发工作",
  "status": "active",
  "version": 1
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| name | String | 否 | 部门名称(2-50字符) |
| shortName | String | 否 | 部门简称(2-20字符) |
| leaderId | String | 否 | 负责人ID |
| sort | Integer | 否 | 排序号(≥0) |
| establishedDate | String | 否 | 成立日期 |
| description | String | 否 | 部门描述 |
| icon | String | 否 | 部门图标URL |
| status | String | 否 | 状态 |
| version | Integer | 是 | 乐观锁版本号 |

**响应示例**:

```json
{
  "code": 0,
  "message": "更新成功",
  "data": null
}
```

---

#### 2.3 移动部门

**接口地址**: `PUT /api/departments/{id}/move`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 要移动的部门ID |

**请求体**:

```json
{
  "newParentId": "DEPT0003",
  "version": 1
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| newParentId | String | 否 | 新父部门ID(null表示移到根级) |
| version | Integer | 是 | 乐观锁版本号 |

**业务规则**:

1. 不能移动到自己
2. 不能移动到自己的子部门
3. 移动后层级不能超过5级
4. 会自动更新所有子部门的层级

**响应示例**:

```json
{
  "code": 0,
  "message": "移动成功",
  "data": null
}
```

---

#### 2.4 删除部门

**接口地址**: `DELETE /api/departments/{id}`

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | String | 是 | 部门ID |

**业务规则**:

1. 有子部门的部门不能删除
2. 有员工的部门不能删除
3. 执行软删除,不物理删除数据

**响应示例**:

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

#### 2.5 批量删除部门

**接口地址**: `DELETE /api/departments/batch`

**请求体**:

```json
{
  "ids": ["DEPT0001", "DEPT0002", "DEPT0003"]
}
```

**请求参数说明**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ids | Array[String] | 是 | 部门ID列表 |

**响应示例**:

```json
{
  "code": 0,
  "message": "批量删除完成",
  "data": {
    "total": 3,
    "success": 2,
    "failed": 1,
    "errors": [
      {
        "id": "DEPT0003",
        "message": "该部门下还有子部门,无法删除"
      }
    ]
  }
}
```

---

#### 2.6 导出部门列表

**接口地址**: `POST /api/departments/export`

**请求体** (可选):

```json
{
  "type": "flat",
  "status": "active",
  "keyword": "技术"
}
```

**响应**:

- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- 文件名: `部门列表_2026-01-15.xlsx`
- 二进制Excel文件流

---

## 🔍 错误码说明

| 错误码 | 说明 | 示例 |
|--------|------|------|
| 0 | 成功 | 请求成功 |
| 1001 | 参数错误 | 必填参数缺失 |
| 1002 | 参数格式错误 | 参数类型不正确 |
| 2001 | 部门不存在 | 指定的部门ID不存在 |
| 2002 | 部门名称重复 | 同级下已存在相同名称的部门 |
| 2003 | 层级超限 | 部门层级超过5级 |
| 2004 | 不能移动到自己 | 不能将部门设置为自己的父部门 |
| 2005 | 不能移动到子部门 | 不能将部门移动到自己的子部门下 |
| 2006 | 有子部门无法删除 | 部门下还有子部门,无法删除 |
| 2007 | 有员工无法删除 | 部门下还有员工,无法删除 |
| 2008 | 乐观锁冲突 | 数据已被其他用户修改,请刷新后重试 |
| 2009 | 上级部门不存在 | 指定的上级部门不存在 |
| 2010 | 负责人不存在 | 指定的负责人不存在 |
| 3001 | 数据库错误 | 数据库操作失败 |
| 3002 | 缓存错误 | 缓存服务异常 |

---

## 📝 DTO/VO 定义

### DepartmentQueryDTO

```java
package com.oa.system.module.department.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 部门查询DTO
 */
@Data
@Builder
public class DepartmentQueryDTO {
    /**
     * 返回类型: tree-树形, flat-扁平
     */
    private String type;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 状态: active-正常, disabled-停用
     */
    private String status;

    /**
     * 负责人ID
     */
    private String leaderId;

    /**
     * 层级
     */
    private Integer level;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer pageSize;
}
```

### DepartmentCreateDTO

```java
package com.oa.system.module.department.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * 部门创建DTO
 */
@Data
public class DepartmentCreateDTO {

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(min = 2, max = 50, message = "部门名称长度在2-50个字符之间")
    private String name;

    /**
     * 部门简称
     */
    @Size(min = 2, max = 20, message = "部门简称长度在2-20个字符之间")
    private String shortName;

    /**
     * 上级部门ID
     */
    private String parentId;

    /**
     * 部门负责人ID
     */
    @NotBlank(message = "部门负责人不能为空")
    private String leaderId;

    /**
     * 排序号
     */
    @Min(value = 0, message = "排序号不能小于0")
    private Integer sort;

    /**
     * 成立时间
     */
    private LocalDate establishedDate;

    /**
     * 部门描述
     */
    @Size(max = 500, message = "部门描述最多500个字符")
    private String description;

    /**
     * 部门图标URL
     */
    @Size(max = 500, message = "部门图标URL最多500个字符")
    private String icon;

    /**
     * 状态
     */
    private String status;
}
```

### DepartmentUpdateDTO

```java
package com.oa.system.module.department.dto;

import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

/**
 * 部门更新DTO
 */
@Data
public class DepartmentUpdateDTO {

    /**
     * 部门名称
     */
    @Size(min = 2, max = 50, message = "部门名称长度在2-50个字符之间")
    private String name;

    /**
     * 部门简称
     */
    @Size(min = 2, max = 20, message = "部门简称长度在2-20个字符之间")
    private String shortName;

    /**
     * 部门负责人ID
     */
    private String leaderId;

    /**
     * 排序号
     */
    @Min(value = 0, message = "排序号不能小于0")
    private Integer sort;

    /**
     * 成立时间
     */
    private LocalDate establishedDate;

    /**
     * 部门描述
     */
    @Size(max = 500, message = "部门描述最多500个字符")
    private String description;

    /**
     * 部门图标URL
     */
    @Size(max = 500, message = "部门图标URL最多500个字符")
    private String icon;

    /**
     * 状态
     */
    private String status;

    /**
     * 乐观锁版本号
     */
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
```

### DepartmentMoveDTO

```java
package com.oa.system.module.department.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 部门移动DTO
 */
@Data
public class DepartmentMoveDTO {

    /**
     * 新父部门ID
     */
    private String newParentId;

    /**
     * 乐观锁版本号
     */
    @NotNull(message = "版本号不能为空")
    private Integer version;
}
```

### DepartmentVO

```java
package com.oa.system.module.department.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门VO
 */
@Data
public class DepartmentVO {
    /**
     * 部门ID
     */
    private String id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 部门简称
     */
    private String shortName;

    /**
     * 上级部门ID
     */
    private String parentId;

    /**
     * 上级部门名称
     */
    private String parentName;

    /**
     * 部门负责人ID
     */
    private String leaderId;

    /**
     * 部门负责人姓名
     */
    private String leaderName;

    /**
     * 部门层级
     */
    private Integer level;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 员工数量
     */
    private Integer employeeCount;

    /**
     * 状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
```

### DepartmentDetailVO

```java
package com.oa.system.module.department.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 部门详情VO
 */
@Data
public class DepartmentDetailVO {
    private String id;
    private String name;
    private String shortName;
    private String parentId;
    private String parentName;
    private String leaderId;
    private String leaderName;
    private String leaderPosition;
    private String leaderPhone;
    private String leaderEmail;
    private Integer level;
    private Integer sort;
    private LocalDate establishedDate;
    private String description;
    private String icon;
    private Integer employeeCount;
    private Integer childCount;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

### DepartmentStatisticsVO

```java
package com.oa.system.module.department.vo;

import lombok.Data;

/**
 * 部门统计VO
 */
@Data
public class DepartmentStatisticsVO {
    /**
     * 总部门数
     */
    private Integer totalCount;

    /**
     * 一级部门数
     */
    private Integer level1Count;

    /**
     * 二级部门数
     */
    private Integer level2Count;

    /**
     * 三级部门数
     */
    private Integer level3Count;

    /**
     * 四级部门数
     */
    private Integer level4Count;

    /**
     * 最大层级
     */
    private Integer maxLevel;

    /**
     * 有负责人的部门数
     */
    private Integer withLeaderCount;

    /**
     * 总员工数
     */
    private Integer totalEmployees;

    /**
     * 启用部门数
     */
    private Integer activeDepartmentCount;

    /**
     * 停用部门数
     */
    private Integer disabledDepartmentCount;
}
```

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-15
