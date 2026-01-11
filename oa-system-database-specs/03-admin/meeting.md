# 会议室管理数据库设计

> **对应前端规范**: [meeting_Technical.md](../../../oa-system-frontend-specs/admin/meeting/meeting_Technical.md)
> **数据库**: MySQL 8.0+
> **表前缀**: `admin_`
> **版本**: v1.0.0

---

## 数据库表设计

### 1. 会议室表 (admin_meeting_room)

#### 1.1 表结构

```sql
CREATE TABLE admin_meeting_room (
  -- 主键
  id VARCHAR(20) PRIMARY KEY COMMENT '会议室ID',

  -- 基本信息
  name VARCHAR(100) NOT NULL COMMENT '会议室名称',
  location VARCHAR(200) NOT NULL COMMENT '位置',
  capacity INT NOT NULL COMMENT '容纳人数',
  facilities JSON COMMENT '设施设备(JSON数组)',
  description TEXT COMMENT '描述',

  -- 状态
  status ENUM('available', 'maintenance', 'disabled') NOT NULL DEFAULT 'available' COMMENT '状态: 可用/维护中/停用',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  -- 索引
  INDEX idx_meeting_room_status (status),
  INDEX idx_meeting_room_capacity (capacity),
  INDEX idx_meeting_room_location (location)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议室表';
```

#### 1.2 检查约束

```sql
-- 容纳人数必须大于0
ALTER TABLE admin_meeting_room
  ADD CONSTRAINT chk_room_capacity
  CHECK (capacity > 0);

-- 会议室名称不能为空
ALTER TABLE admin_meeting_room
  ADD CONSTRAINT chk_room_name
  CHECK (name IS NOT NULL AND TRIM(name) != '');
```

---

### 2. 会议室预定表 (admin_meeting_booking)

#### 2.1 表结构

```sql
CREATE TABLE admin_meeting_booking (
  -- 主键
  id VARCHAR(50) PRIMARY KEY COMMENT '预定ID',

  -- 基本信息
  title VARCHAR(200) NOT NULL COMMENT '会议主题',
  room_id VARCHAR(20) NOT NULL COMMENT '会议室ID',
  room_name VARCHAR(100) COMMENT '会议室名称(快照)',
  start_time DATETIME NOT NULL COMMENT '开始时间',
  end_time DATETIME NOT NULL COMMENT '结束时间',

  -- 人员信息
  booker_id VARCHAR(20) NOT NULL COMMENT '预定人ID',
  booker_name VARCHAR(50) COMMENT '预定人姓名(快照)',
  participant_count INT COMMENT '参会人数',
  participant_ids JSON COMMENT '参会人员ID数组',

  -- 会议详情
  agenda TEXT COMMENT '会议议程',
  equipment JSON COMMENT '所需设备(JSON数组)',

  -- 状态
  status ENUM('booked', 'in_progress', 'completed', 'cancelled') NOT NULL DEFAULT 'booked' COMMENT '状态: 已预定/进行中/已完成/已取消',
  recurring_rule VARCHAR(200) COMMENT '循环规则(如: 每周X重复)',

  -- 审计字段
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  -- 索引
  INDEX idx_booking_room (room_id),
  INDEX idx_booking_time (start_time, end_time),
  INDEX idx_booking_booker (booker_id),
  INDEX idx_booking_status (status),
  INDEX idx_booking_start_time (start_time),
  INDEX idx_booking_end_time (end_time),

  -- 外键约束
  FOREIGN KEY (room_id)
    REFERENCES admin_meeting_room(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会议室预定表';
```

#### 2.2 外键约束

```sql
-- 预定人外键
ALTER TABLE admin_meeting_booking
  ADD CONSTRAINT fk_booking_booker
  FOREIGN KEY (booker_id)
  REFERENCES sys_employee(id)
  ON DELETE RESTRICT
  ON UPDATE CASCADE;
```

#### 2.3 检查约束

```sql
-- 结束时间必须晚于开始时间
ALTER TABLE admin_meeting_booking
  ADD CONSTRAINT chk_booking_time
  CHECK (end_time > start_time);

-- 参会人数必须大于0(如果提供)
ALTER TABLE admin_meeting_booking
  ADD CONSTRAINT chk_booking_participants
  CHECK (participant_count IS NULL OR participant_count > 0);
```

---

## 3. 触发器设计

### 3.1 会议室快照触发器

```sql
DELIMITER $$

-- 创建预定时自动填充快照数据
CREATE TRIGGER trg_booking_before_insert
BEFORE INSERT ON admin_meeting_booking
FOR EACH ROW
BEGIN
  -- 自动填充会议室名称
  SELECT name INTO NEW.room_name
  FROM admin_meeting_room
  WHERE id = NEW.room_id;

  -- 自动填充预定人姓名
  SELECT name INTO NEW.booker_name
  FROM sys_employee
  WHERE id = NEW.booker_id;
END$$

-- 更新预定时更新快照数据(如果会议室或预定人变更)
CREATE TRIGGER trg_booking_before_update
BEFORE UPDATE ON admin_meeting_booking
FOR EACH ROW
BEGIN
  -- 如果会议室变更,更新名称
  IF NEW.room_id <> OLD.room_id THEN
    SELECT name INTO NEW.room_name
    FROM admin_meeting_room
    WHERE id = NEW.room_id;
  END IF;

  -- 如果预定人变更,更新姓名
  IF NEW.booker_id <> OLD.booker_id THEN
    SELECT name INTO NEW.booker_name
    FROM sys_employee
    WHERE id = NEW.booker_id;
  END IF;
END$$

DELIMITER ;
```

### 3.2 自动状态更新触发器

```sql
DELIMITER $$

-- 定时任务触发: 更新会议状态
-- 使用定时任务代替触发器更合适
CREATE PROCEDURE sp_update_meeting_status()
BEGIN
  -- 将已过期的预定标记为已完成
  UPDATE admin_meeting_booking
  SET status = 'completed'
  WHERE end_time < NOW()
    AND status IN ('booked', 'in_progress');

  -- 将正在进行的状态更新
  UPDATE admin_meeting_booking
  SET status = 'in_progress'
  WHERE start_time <= NOW()
    AND end_time > NOW()
    AND status = 'booked';
END$$

DELIMITER ;
```

---

## 4. 存储过程设计

### 4.1 检查时间冲突

```sql
DELIMITER $$

CREATE PROCEDURE sp_check_time_conflict(
  IN p_room_id VARCHAR(20),
  IN p_start_time DATETIME,
  IN p_end_time DATETIME,
  IN p_exclude_id VARCHAR(50),
  OUT p_conflict_count INT
)
BEGIN
  SELECT COUNT(*) INTO p_conflict_count
  FROM admin_meeting_booking
  WHERE room_id = p_room_id
    AND status != 'cancelled'
    AND (p_exclude_id IS NULL OR id != p_exclude_id)
    AND (
      -- 新预定的开始时间在已有预定的时间段内
      (p_start_time >= start_time AND p_start_time < end_time)
      -- 新预定的结束时间在已有预定的时间段内
      OR (p_end_time > start_time AND p_end_time <= end_time)
      -- 新预定完全包含已有预定
      OR (p_start_time <= start_time AND p_end_time >= end_time)
    );
END$$

DELIMITER ;
```

### 4.2 创建预定

```sql
DELIMITER $$

CREATE PROCEDURE sp_create_booking(
  IN p_id VARCHAR(50),
  IN p_title VARCHAR(200),
  IN p_room_id VARCHAR(20),
  IN p_start_time DATETIME,
  IN p_end_time DATETIME,
  IN p_booker_id VARCHAR(20),
  IN p_participant_count INT,
  IN p_participant_ids JSON,
  IN p_agenda TEXT,
  IN p_equipment JSON,
  OUT p_success TINYINT,
  OUT p_message VARCHAR(200)
)
BEGIN
  DECLARE v_conflict_count INT;
  DECLARE v_room_capacity INT;
  DECLARE v_room_status VARCHAR(20);

  -- 检查会议室状态
  SELECT status INTO v_room_status
  FROM admin_meeting_room
  WHERE id = p_room_id;

  IF v_room_status != 'available' THEN
    SET p_success = 0;
    SET p_message = '会议室当前不可用';
    LEAVE sp_create_booking;
  END IF;

  -- 检查时间冲突
  CALL sp_check_time_conflict(p_room_id, p_start_time, p_end_time, NULL, v_conflict_count);

  IF v_conflict_count > 0 THEN
    SET p_success = 0;
    SET p_message = '该时间段已被预定';
    LEAVE sp_create_booking;
  END IF;

  -- 检查容纳人数
  SELECT capacity INTO v_room_capacity
  FROM admin_meeting_room
  WHERE id = p_room_id;

  IF p_participant_count > v_room_capacity THEN
    SET p_success = 0;
    SET p_message = CONCAT('参会人数超过会议室容纳人数(', v_room_capacity, ')');
    LEAVE sp_create_booking;
  END IF;

  -- 创建预定
  INSERT INTO admin_meeting_booking (
    id, title, room_id, start_time, end_time,
    booker_id, participant_count, participant_ids,
    agenda, equipment, status
  ) VALUES (
    p_id, p_title, p_room_id, p_start_time, p_end_time,
    p_booker_id, p_participant_count, p_participant_ids,
    p_agenda, p_equipment, 'booked'
  );

  SET p_success = 1;
  SET p_message = '预定成功';
END$$

DELIMITER ;
```

### 4.3 取消预定

```sql
DELIMITER $$

CREATE PROCEDURE sp_cancel_booking(
  IN p_booking_id VARCHAR(50),
  IN p_booker_id VARCHAR(20),
  OUT p_success TINYINT,
  OUT p_message VARCHAR(200)
)
BEGIN
  DECLARE v_booker VARCHAR(20);
  DECLARE v_status VARCHAR(20);
  DECLARE v_start_time DATETIME;

  -- 获取预定信息
  SELECT booker_id, status, start_time
  INTO v_booker, v_status, v_start_time
  FROM admin_meeting_booking
  WHERE id = p_booking_id;

  -- 检查预定是否存在
  IF v_booker IS NULL THEN
    SET p_success = 0;
    SET p_message = '预定不存在';
    LEAVE sp_cancel_booking;
  END IF;

  -- 检查是否为预定人
  IF v_booker != p_booker_id THEN
    SET p_success = 0;
    SET p_message = '只有预定人可以取消预定';
    LEAVE sp_cancel_booking;
  END IF;

  -- 检查状态
  IF v_status = 'cancelled' THEN
    SET p_success = 0;
    SET p_message = '预定已取消';
    LEAVE sp_cancel_booking;
  END IF;

  -- 检查是否可以取消(会议开始前30分钟)
  IF TIMESTAMPDIFF(MINUTE, NOW(), v_start_time) < 30 THEN
    SET p_success = 0;
    SET p_message = '会议开始前30分钟内不能取消';
    LEAVE sp_cancel_booking;
  END IF;

  -- 更新状态
  UPDATE admin_meeting_booking
  SET status = 'cancelled',
      updated_at = NOW()
  WHERE id = p_booking_id;

  SET p_success = 1;
  SET p_message = '取消成功';
END$$

DELIMITER ;
```

### 4.4 获取会议室预定统计

```sql
DELIMITER $$

CREATE PROCEDURE sp_booking_statistics(
  IN p_room_id VARCHAR(20),
  IN p_start_date DATE,
  IN p_end_date DATE
)
BEGIN
  SELECT
    room_id,
    room_name,
    COUNT(*) AS total_bookings,
    SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) AS completed_count,
    SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) AS cancelled_count,
    SUM(TIMESTAMPDIFF(MINUTE, start_time, end_time)) / 60 AS total_hours,
    ROUND(
      SUM(CASE WHEN status != 'cancelled' THEN TIMESTAMPDIFF(MINUTE, start_time, end_time) ELSE 0 END) * 100.0 /
      SUM(CASE WHEN status = 'completed' THEN TIMESTAMPDIFF(MINUTE, start_time, end_time) ELSE 0 END),
      2
    ) AS utilization_rate
  FROM admin_meeting_booking
  WHERE (p_room_id IS NULL OR room_id = p_room_id)
    AND (p_start_date IS NULL OR DATE(start_time) >= p_start_date)
    AND (p_end_date IS NULL OR DATE(end_time) <= p_end_date)
  GROUP BY room_id, room_name;
END$$

DELIMITER ;
```

### 4.5 获取用户预定统计

```sql
DELIMITER $$

CREATE PROCEDURE sp_user_booking_statistics(
  IN p_booker_id VARCHAR(20),
  IN p_start_date DATE,
  IN p_end_date DATE
)
BEGIN
  SELECT
    booker_id,
    booker_name,
    COUNT(*) AS total_bookings,
    SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) AS attended_count,
    SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) AS cancelled_count,
    SUM(CASE WHEN status != 'cancelled' THEN TIMESTAMPDIFF(MINUTE, start_time, end_time) ELSE 0 END) / 60 AS total_hours
  FROM admin_meeting_booking
  WHERE booker_id = p_booker_id
    AND (p_start_date IS NULL OR DATE(start_time) >= p_start_date)
    AND (p_end_date IS NULL OR DATE(end_time) <= p_end_date)
  GROUP BY booker_id, booker_name;
END$$

DELIMITER ;
```

---

## 5. 视图设计

### 5.1 预定详情视图

```sql
CREATE VIEW v_booking_detail AS
SELECT
  b.id,
  b.title,
  b.room_id,
  b.room_name,
  b.start_time,
  b.end_time,
  TIMESTAMPDIFF(MINUTE, b.start_time, b.end_time) / 60 AS duration_hours,
  b.booker_id,
  b.booker_name,
  e.position AS booker_position,
  e.phone AS booker_phone,
  e.email AS booker_email,
  d.name AS booker_department,
  b.participant_count,
  b.participant_ids,
  b.agenda,
  b.equipment,
  b.status,
  b.recurring_rule,
  b.created_at,
  b.updated_at,

  -- 会议室信息
  r.location AS room_location,
  r.capacity AS room_capacity,
  r.facilities AS room_facilities,

  -- 状态描述
  CASE b.status
    WHEN 'booked' THEN '已预定'
    WHEN 'in_progress' THEN '进行中'
    WHEN 'completed' THEN '已完成'
    WHEN 'cancelled' THEN '已取消'
  END AS status_description
FROM admin_meeting_booking b
LEFT JOIN admin_meeting_room r ON b.room_id = r.id
LEFT JOIN sys_employee e ON b.booker_id = e.id
LEFT JOIN sys_department d ON e.department_id = d.id;
```

### 5.2 会议室使用情况视图

```sql
CREATE VIEW v_room_usage_today AS
SELECT
  r.id AS room_id,
  r.name AS room_name,
  r.location,
  r.capacity,
  r.facilities,
  r.status AS room_status,
  COUNT(b.id) AS booking_count_today,
  SUM(CASE WHEN b.status = 'booked' THEN 1 ELSE 0 END) AS booked_count,
  SUM(CASE WHEN b.status = 'in_progress' THEN 1 ELSE 0 END) AS in_progress_count,
  SUM(CASE WHEN b.status = 'completed' THEN 1 ELSE 0 END) AS completed_count
FROM admin_meeting_room r
LEFT JOIN admin_meeting_booking b
  ON r.id = b.room_id
  AND DATE(b.start_time) = CURDATE()
  AND b.status != 'cancelled'
WHERE r.status = 'available'
GROUP BY r.id, r.name, r.location, r.capacity, r.facilities, r.status
ORDER BY r.id;
```

### 5.3 预定冲突视图

```sql
CREATE VIEW v_booking_conflicts AS
SELECT
  b1.id AS booking_id,
  b1.title,
  b1.room_id,
  b1.room_name,
  b1.start_time AS booking_start,
  b1.end_time AS booking_end,
  b2.id AS conflict_id,
  b2.title AS conflict_title,
  b2.start_time AS conflict_start,
  b2.end_time AS conflict_end
FROM admin_meeting_booking b1
INNER JOIN admin_meeting_booking b2
  ON b1.room_id = b2.room_id
  AND b1.id != b2.id
  AND b1.status != 'cancelled'
  AND b2.status != 'cancelled'
  AND (
    -- b1的开始时间在b2的时间段内
    (b1.start_time >= b2.start_time AND b1.start_time < b2.end_time)
    -- b1的结束时间在b2的时间段内
    OR (b1.end_time > b2.start_time AND b1.end_time <= b2.end_time)
    -- b1完全包含b2
    OR (b1.start_time <= b2.start_time AND b1.end_time >= b2.end_time)
  )
WHERE b1.start_time >= CURDATE();  -- 只显示今天及以后的冲突
```

---

## 6. 数据字典映射

### 6.1 会议室状态字典 (meeting_room_status)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('meeting_room_status', '会议室状态', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('meeting_room_status', '可用', 'available', '#67C23A', 1, 'enabled'),
('meeting_room_status', '维护中', 'maintenance', '#E6A23C', 2, 'enabled'),
('meeting_room_status', '停用', 'disabled', '#909399', 3, 'enabled');
```

### 6.2 预定状态字典 (meeting_booking_status)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('meeting_booking_status', '预定状态', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, color, sort, status) VALUES
('meeting_booking_status', '已预定', 'booked', '#409EFF', 1, 'enabled'),
('meeting_booking_status', '进行中', 'in_progress', '#E6A23C', 2, 'enabled'),
('meeting_booking_status', '已完成', 'completed', '#67C23A', 3, 'enabled'),
('meeting_booking_status', '已取消', 'cancelled', '#909399', 4, 'enabled');
```

### 6.3 会议室设施字典 (meeting_facility)

```sql
INSERT INTO sys_dict_type (code, name, category, status) VALUES
('meeting_facility', '会议室设施', 'business', 'enabled');

INSERT INTO sys_dict_item (type_code, label, value, icon, sort, status) VALUES
('meeting_facility', '投影仪', 'projector', 'icon-projector', 1, 'enabled'),
('meeting_facility', '音响系统', 'audio', 'icon-audio', 2, 'enabled'),
('meeting_facility', '白板', 'whiteboard', 'icon-whiteboard', 3, 'enabled'),
('meeting_facility', '视频会议', 'video_conf', 'icon-video', 4, 'enabled'),
('meeting_facility', '麦克风', 'microphone', 'icon-mic', 5, 'enabled'),
('meeting_facility', '网络', 'wifi', 'icon-wifi', 6, 'enabled'),
('meeting_facility', '空调', 'air_conditioner', 'icon-ac', 7, 'enabled');
```

---

## 7. 初始化数据

```sql
-- 插入示例会议室数据
INSERT INTO admin_meeting_room (
  id, name, location, capacity, facilities, status
) VALUES
('ROOM001', '大会议室', 'A座5楼', 50,
 '["投影仪", "音响系统", "白板", "视频会议设备"]', 'available'),
('ROOM002', '中会议室A', 'A座4楼', 20,
 '["投影仪", "白板"]', 'available'),
('ROOM003', '中会议室B', 'A座4楼', 20,
 '["投影仪", "白板", "视频会议设备"]', 'available'),
('ROOM004', '小会议室A', 'A座3楼', 10,
 '["白板"]', 'available'),
('ROOM005', '小会议室B', 'A座3楼', 10,
 '["白板", "视频会议设备"]', 'available');

-- 插入示例预定数据
INSERT INTO admin_meeting_booking (
  id, title, room_id, start_time, end_time,
  booker_id, participant_count, agenda, status
) VALUES
('BK20240101001', '周例会', 'ROOM001',
 '2024-01-15 09:00:00', '2024-01-15 11:00:00',
 'EMP20230115001', 10, '讨论本周工作计划', 'completed'),
('BK20240102001', '产品评审', 'ROOM002',
 '2024-01-20 14:00:00', '2024-01-20 17:00:00',
 'EMP20230115002', 8, '新功能评审', 'completed'),
('BK20240103001', '技术分享', 'ROOM003',
 DATE_ADD(NOW(), INTERVAL 2 DAY),
 DATE_ADD(NOW(), INTERVAL 2 DAY + 3 HOUR),
 'EMP20230115003', 15, '前端技术分享', 'booked');
```

---

## 8. 定时任务设计

### 8.1 自动更新会议状态

```sql
-- 创建定时事件(每5分钟执行一次)
SET GLOBAL event_scheduler = ON;

CREATE EVENT evt_update_meeting_status
ON SCHEDULE EVERY 5 MINUTE
DO CALL sp_update_meeting_status();
```

---

## 9. API对应SQL查询

### 9.1 获取会议室列表

```sql
-- 对应前端: GET /api/meeting/rooms
SELECT
  r.*,
  (SELECT COUNT(*) FROM admin_meeting_booking
   WHERE room_id = r.id
     AND status != 'cancelled'
     AND start_time >= NOW()
     AND start_time < DATE_ADD(NOW(), INTERVAL 7 DAY)
  ) AS booking_count_7days
FROM admin_meeting_room r
WHERE (? IS NULL OR r.status = ?)
ORDER BY r.id;
```

### 9.2 获取预定列表

```sql
-- 对应前端: GET /api/meeting/bookings
SELECT
  b.*,
  r.name AS room_name,
  r.location AS room_location,
  r.capacity AS room_capacity,
  e.position AS booker_position
FROM admin_meeting_booking b
LEFT JOIN admin_meeting_room r ON b.room_id = r.id
LEFT JOIN sys_employee e ON b.booker_id = e.id
WHERE (? IS NULL OR b.room_id = ?)
  AND (? IS NULL OR b.booker_id = ?)
  AND (? IS NULL OR b.status = ?)
  AND (? IS NULL OR b.start_time >= ?)
  AND (? IS NULL OR b.end_time <= ?)
ORDER BY b.start_time ASC;
```

### 9.3 检查时间可用性

```sql
-- 对应前端: GET /api/meeting/rooms/:id/availability
SELECT
  start_time,
  end_time,
  title,
  status
FROM admin_meeting_booking
WHERE room_id = ?
  AND status != 'cancelled'
  AND (
    -- 今天及以后的预定
    start_time >= CURDATE()
  )
ORDER BY start_time ASC;
```

### 9.4 获取会议室使用统计

```sql
-- 对应前端: GET /api/meeting/statistics
CALL sp_booking_statistics(NULL, NULL, NULL);
```

---

## 10. 业务规则说明

### 10.1 预定规则

- 预定时间: 会议开始前30分钟内不能预定
- 最短预定: 15分钟
- 最长预定: 8小时
- 跨天预定: 需要特殊审批

### 10.2 取消规则

- 取消时间: 会议开始前30分钟内不能取消
- 取消权限: 只有预定人可以取消
- 管理员: 可以取消任何预定

### 10.3 时间冲突检查

- 同一会议室同一时间段不能有多个预定
- 预定时间不能完全或部分重叠

### 10.4 容量限制

- 参会人数不能超过会议室容量
- 容量包括会议室内的所有座位

### 10.5 循环预定

- 支持每天、每周、每月循环
- 循环预定最多3个月
- 循环预定可以手动取消单次会议

---

**文档版本**: v1.0.0
**创建人**: AI开发助手
**最后更新**: 2026-01-11
