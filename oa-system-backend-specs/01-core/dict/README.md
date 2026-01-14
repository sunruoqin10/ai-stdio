# 数据字典后端模块说明

## 📁 模块信息

- **模块名称**: 数据字典 (Dict)
- **模块路径**: `oa-system-backend/src/main/java/com/example/oa_system_backend/module/dict/`
- **技术栈**: Spring Boot 3.x + MyBatis Plus + MySQL 8.0
- **版本**: v1.0.0

## 📄 文档列表

1. **[dict_Technical.md](./dict_Technical.md)** - 数据字典后端技术实现规范
   - 包含完整的项目结构、实体类设计、DTO/VO设计
   - 详细的Mapper接口、Service层、Controller层实现
   - 完整的业务逻辑实现、数据验证、权限控制、缓存策略

## 🗂️ 项目结构

```
oa-system-backend/src/main/java/com/example/oa_system_backend/module/dict/
├── controller/
│   └── DictController.java              # 数据字典控制器
├── service/
│   ├── DictService.java                 # 数据字典服务接口
│   └── impl/
│       └── DictServiceImpl.java         # 数据字典服务实现
├── mapper/
│   ├── DictTypeMapper.java              # 字典类型Mapper
│   └── DictItemMapper.java              # 字典项Mapper
├── entity/
│   ├── DictType.java                    # 字典类型实体
│   └── DictItem.java                    # 字典项实体
├── dto/
│   ├── DictTypeCreateRequest.java       # 创建字典类型DTO
│   ├── DictTypeUpdateRequest.java       # 更新字典类型DTO
│   ├── DictTypeQueryRequest.java        # 查询字典类型DTO
│   ├── DictItemCreateRequest.java       # 创建字典项DTO
│   ├── DictItemUpdateRequest.java       # 更新字典项DTO
│   ├── DictItemQueryRequest.java        # 查询字典项DTO
│   ├── DictItemSortUpdateRequest.java   # 排序更新DTO
│   └── DictBatchOperationRequest.java   # 批量操作DTO
└── vo/
    ├── DictTypeVO.java                  # 字典类型视图对象
    ├── DictItemVO.java                  # 字典项视图对象
    ├── DictTreeVO.java                  # 字典树视图对象
    └── DictDataVO.java                  # 字典数据视图对象
```

## 🔌 API接口列表

### 字典类型接口
- `GET /api/dict/types` - 获取字典类型列表
- `GET /api/dict/types/{id}` - 获取字典类型详情
- `POST /api/dict/types` - 创建字典类型
- `PUT /api/dict/types/{id}` - 更新字典类型
- `DELETE /api/dict/types/{id}` - 删除字典类型
- `GET /api/dict/types/check-code` - 检查字典编码是否存在

### 字典项接口
- `GET /api/dict/items` - 获取字典项列表
- `GET /api/dict/items/{id}` - 获取字典项详情
- `POST /api/dict/items` - 创建字典项
- `PUT /api/dict/items/{id}` - 更新字典项
- `DELETE /api/dict/items/{id}` - 删除字典项
- `DELETE /api/dict/items/batch` - 批量删除字典项
- `PUT /api/dict/items/batch/status` - 批量更新字典项状态
- `PUT /api/dict/items/sort` - 批量更新字典项排序
- `GET /api/dict/items/check-value` - 检查字典项值是否存在

### 字典树和数据接口
- `GET /api/dict/tree` - 获取字典树
- `GET /api/dict/{code}` - 根据字典类型编码获取字典数据
- `DELETE /api/dict/cache/{code}` - 清除字典缓存

## 🔑 核心功能

### 1. 字典类型管理
- ✅ 创建、查询、更新、删除字典类型
- ✅ 系统字典和业务字典分类
- ✅ 字典编码唯一性验证
- ✅ 级联删除字典项

### 2. 字典项管理
- ✅ 创建、查询、更新、删除字典项
- ✅ 字典项值唯一性验证(同一字典类型下)
- ✅ 批量操作(删除、更新状态、更新排序)
- ✅ 支持颜色类型和自定义颜色

### 3. 数据验证
- ✅ 外键约束验证
- ✅ 唯一性约束验证
- ✅ 检查约束验证
- ✅ 数据完整性验证

### 4. 权限控制
- ✅ 基于Spring Security的权限控制
- ✅ 系统字典和业务字典分离权限
- ✅ 数据权限过滤

### 5. 缓存策略
- ✅ Redis缓存支持
- ✅ 字典数据缓存(30分钟)
- ✅ 字典树缓存(30分钟)
- ✅ 缓存自动清除

## 🔗 关联文档

- **前端规范**: [dict_Technical.md](../../../../oa-system-frontend-specs/core/dict/dict_Technical.md)
- **数据库规范**: [dict.md](../../../../oa-system-database-specs/01-core/dict.md)

## 📝 注意事项

### 外键约束
虽然数据库层面定义了外键约束,但为了提高性能和灵活性,应用层也实现了相应的验证逻辑:
- 字典项的`dict_type_id`必须关联到存在的字典类型
- 删除字典类型时会级联软删除所有关联的字典项

### 检查约束
数据库的检查约束在应用层也进行了验证:
- 字典类别必须是`system`或`business`
- 字典状态必须是`enabled`或`disabled`
- 字典项状态必须是`enabled`或`disabled`
- 颜色类型必须是`primary`、`success`、`warning`、`danger`或`info`
- 排序序号不能为负数

### 缓存策略
- 字典数据默认缓存30分钟
- 创建、更新、删除操作会自动清除相关缓存
- 支持手动清除缓存

## 🧪 测试要点

- [ ] 字典类型CRUD功能完整性
- [ ] 字典项CRUD功能完整性
- [ ] 字典编码唯一性验证
- [ ] 字典项值唯一性验证(同一字典类型下)
- [ ] 外键约束验证
- [ ] 检查约束验证
- [ ] 权限控制有效性
- [ ] 缓存功能正常工作
- [ ] 级联删除功能正常
- [ ] 批量操作功能正常
- [ ] 字典树查询正确性
- [ ] 字典数据查询正确性

---

**文档版本**: v1.0.0
**创建日期**: 2026-01-14
**最后更新**: 2026-01-14
