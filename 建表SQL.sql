create table admin_meeting_booking
(
    id                varchar(50)                                                                        not null comment '预定ID'
        primary key,
    title             varchar(200)                                                                       not null comment '会议主题',
    room_id           varchar(20)                                                                        not null comment '会议室ID',
    room_name         varchar(100)                                                                       null comment '会议室名称',
    start_time        datetime                                                                           not null comment '开始时间',
    end_time          datetime                                                                           not null comment '结束时间',
    booker_id         varchar(20)                                                                        not null comment '预定人ID',
    booker_name       varchar(50)                                                                        null comment '预定人姓名',
    participant_count int                                                                                null comment '参会人数',
    participant_ids   json                                                                               null comment '参会人员ID数组',
    agenda            text                                                                               null comment '会议议程',
    equipment         json                                                                               null comment '所需设备',
    status            enum ('booked', 'in_progress', 'completed', 'cancelled') default 'booked'          not null comment '状态',
    recurring_rule    varchar(200)                                                                       null comment '循环规则',
    created_at        datetime                                                 default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at        datetime                                                 default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '会议室预定表';

create index idx_booking_booker
    on admin_meeting_booking (booker_id);

create index idx_booking_room
    on admin_meeting_booking (room_id);

create index idx_booking_status
    on admin_meeting_booking (status);

create index idx_booking_time
    on admin_meeting_booking (start_time, end_time);

create table admin_meeting_room
(
    id          varchar(20)                                                             not null comment '会议室ID'
        primary key,
    name        varchar(100)                                                            not null comment '会议室名称',
    location    varchar(200)                                                            not null comment '位置',
    capacity    int                                                                     not null comment '容纳人数',
    facilities  json                                                                    null comment '设施设备',
    description text                                                                    null comment '描述',
    status      enum ('available', 'maintenance', 'disabled') default 'available'       not null comment '状态',
    created_at  datetime                                      default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  datetime                                      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '会议室表';

create table approval_expense
(
    id                       varchar(32)                           not null comment '报销单号: EXP+YYYYMMDD+序号'
        primary key,
    applicant_id             varchar(32)                           not null comment '报销人ID',
    department_id            varchar(32)                           not null comment '部门ID',
    type                     varchar(50)                           not null comment '报销类型',
    amount                   decimal(10, 2)                        not null comment '总金额',
    reason                   text                                  not null comment '报销事由',
    apply_date               date                                  not null comment '申请日期',
    expense_date             date                                  not null comment '费用发生日期',
    status                   varchar(20) default 'draft'           not null comment '状态',
    dept_approver_id         varchar(32)                           null comment '部门审批人ID',
    dept_approver_name       varchar(100)                          null comment '部门审批人姓名',
    dept_approval_status     varchar(20)                           null comment '部门审批状态',
    dept_approval_opinion    text                                  null comment '部门审批意见',
    dept_approval_time       datetime                              null comment '部门审批时间',
    finance_approver_id      varchar(32)                           null comment '财务审批人ID',
    finance_approver_name    varchar(100)                          null comment '财务审批人姓名',
    finance_approval_status  varchar(20)                           null comment '财务审批状态',
    finance_approval_opinion text                                  null comment '财务审批意见',
    finance_approval_time    datetime                              null comment '财务审批时间',
    payment_date             date                                  null comment '打款时间',
    payment_proof            varchar(500)                          null comment '打款凭证URL',
    created_at               datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at               datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted_at               datetime                              null comment '删除时间'
)
    comment '费用报销单表';

create index idx_expense_applicant
    on approval_expense (applicant_id);

create index idx_expense_apply_date
    on approval_expense (apply_date);

create index idx_expense_department
    on approval_expense (department_id);

create index idx_expense_status
    on approval_expense (status);

create table approval_expense_invoice
(
    id             bigint auto_increment comment '主键ID'
        primary key,
    expense_id     varchar(32)                          not null comment '报销单号',
    invoice_type   varchar(20)                          not null comment '发票类型',
    invoice_number varchar(50)                          not null comment '发票号码',
    amount         decimal(10, 2)                       not null comment '发票金额',
    invoice_date   date                                 not null comment '开票日期',
    image_url      varchar(500)                         null comment '发票图片URL',
    verified       tinyint(1) default 0                 null comment '是否已验真',
    created_at     datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at     datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_invoice_number
        unique (invoice_number)
)
    comment '发票表';

create index idx_invoice_expense
    on approval_expense_invoice (expense_id);

create table approval_expense_item
(
    id           bigint auto_increment comment '主键ID'
        primary key,
    expense_id   varchar(32)                        not null comment '报销单号',
    description  varchar(500)                       not null comment '费用说明',
    amount       decimal(10, 2)                     not null comment '金额',
    expense_date date                               not null comment '发生日期',
    category     varchar(100)                       null comment '费用分类',
    sort_order   int      default 0                 null comment '排序序号',
    created_at   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '费用明细表';

create index idx_expense_item_expense
    on approval_expense_item (expense_id);

create table approval_expense_payment
(
    id             bigint auto_increment comment '主键ID'
        primary key,
    expense_id     varchar(32)                           not null comment '报销单号',
    amount         decimal(10, 2)                        not null comment '打款金额',
    payment_method varchar(20) default 'bank_transfer'   not null comment '打款方式: bank_transfer/cash/check',
    payment_date   date                                  not null comment '打款日期',
    bank_account   varchar(100)                          null comment '收款账号',
    proof          varchar(500)                          null comment '打款凭证URL',
    status         varchar(20) default 'pending'         not null comment '状态: pending/completed/failed',
    remark         text                                  null comment '备注',
    created_at     datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at     datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '打款记录表';

create index idx_payment_date
    on approval_expense_payment (payment_date);

create index idx_payment_expense
    on approval_expense_payment (expense_id);

create index idx_payment_status
    on approval_expense_payment (status);

create table approval_holiday
(
    id         bigint auto_increment comment '主键ID'
        primary key,
    date       date                                            not null comment '日期',
    name       varchar(100)                                    not null comment '节假日名称',
    type       enum ('national', 'company') default 'national' not null comment '类型:国家/公司',
    year       int                                             not null comment '年份',
    is_workday tinyint(1)                   default 0          null comment '是否为工作日(调休)',
    constraint date
        unique (date)
)
    comment '节假日表';

create index idx_holiday_date
    on approval_holiday (date);

create index idx_holiday_year
    on approval_holiday (year);

create table approval_leave_approval
(
    id             bigint auto_increment
        primary key,
    request_id     varchar(50)                                                not null comment '申请ID',
    approver_id    varchar(50)                                                not null comment '审批人ID',
    approver_name  varchar(100)                                               not null comment '审批人姓名',
    approval_level int                                                        not null comment '审批层级',
    status         enum ('pending', 'approved', 'rejected') default 'pending' not null comment '审批状态',
    opinion        text                                                       null comment '审批意见',
    timestamp      datetime                                                   null comment '审批时间',
    constraint uk_leave_request_approver
        unique (request_id, approver_id)
)
    comment '请假审批记录表';

create index idx_leave_approval_status
    on approval_leave_approval (status);

create index idx_leave_approver
    on approval_leave_approval (approver_id);

create table approval_leave_balance
(
    id               bigint auto_increment
        primary key,
    employee_id      varchar(50)                             not null comment '员工ID',
    year             int                                     not null comment '年份',
    annual_total     decimal(4, 1) default 0.0               not null comment '年假总额(天)',
    annual_used      decimal(4, 1) default 0.0               not null comment '已使用(天)',
    annual_remaining decimal(4, 1) default 0.0               not null comment '剩余(天)',
    created_at       datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at       datetime      default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint employee_id
        unique (employee_id),
    constraint uk_leave_employee_year
        unique (employee_id, year)
)
    comment '年假余额表';

create index idx_leave_balance_year
    on approval_leave_balance (year);

create table approval_leave_request
(
    id                     varchar(50)                                                                                           not null comment '编号: LEAVE+YYYYMMDD+序号'
        primary key,
    applicant_id           varchar(50)                                                                                           not null comment '申请人ID',
    department_id          varchar(50)                                                                                           not null comment '部门ID',
    type                   enum ('annual', 'sick', 'personal', 'comp_time', 'marriage', 'maternity')                             not null comment '请假类型',
    start_time             datetime                                                                                              not null comment '开始时间',
    end_time               datetime                                                                                              not null comment '结束时间',
    duration               decimal(4, 1)                                                                                         not null comment '请假时长(天)',
    reason                 text                                                                                                  not null comment '请假事由',
    attachments            json                                                                                                  null comment '附件URL数组',
    status                 enum ('draft', 'pending', 'approving', 'approved', 'rejected', 'cancelled') default 'draft'           not null comment '状态',
    current_approval_level int                                                                         default 0                 null comment '当前审批层级',
    created_at             datetime                                                                    default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at             datetime                                                                    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '请假申请表';

create index idx_leave_applicant
    on approval_leave_request (applicant_id);

create index idx_leave_created
    on approval_leave_request (created_at);

create index idx_leave_department
    on approval_leave_request (department_id);

create index idx_leave_status
    on approval_leave_request (status);

create table approval_leave_usage_log
(
    id          bigint auto_increment comment '记录ID'
        primary key,
    employee_id varchar(50)                                                               not null comment '员工ID',
    request_id  varchar(50)                                                               not null comment '申请ID',
    type        enum ('annual', 'sick', 'personal', 'comp_time', 'marriage', 'maternity') not null comment '请假类型',
    duration    decimal(4, 1)                                                             not null comment '请假时长(天)',
    start_time  datetime                                                                  not null comment '开始时间',
    end_time    datetime                                                                  not null comment '结束时间',
    change_type enum ('deduct', 'rollback')                                               not null comment '变动类型:扣减/回退',
    created_at  datetime default CURRENT_TIMESTAMP                                        not null comment '记录时间'
)
    comment '年假使用记录表';

create index idx_usage_created
    on approval_leave_usage_log (created_at);

create index idx_usage_employee
    on approval_leave_usage_log (employee_id);

create index idx_usage_request
    on approval_leave_usage_log (request_id);

create index idx_usage_type
    on approval_leave_usage_log (change_type);

create table auth_login_log
(
    id             bigint auto_increment comment '日志ID'
        primary key,
    user_id        varchar(20)                        null comment '用户ID',
    username       varchar(50)                        null comment '用户名',
    login_type     enum ('web', 'mobile', 'api')      not null comment '登录类型',
    status         enum ('success', 'failed')         not null comment '登录状态',
    failure_reason varchar(200)                       null comment '失败原因',
    ip_address     varchar(50)                        null comment 'IP地址',
    location       varchar(200)                       null comment '登录地点',
    device_type    varchar(50)                        null comment '设备类型',
    user_agent     varchar(500)                       null comment '用户代理',
    created_at     datetime default CURRENT_TIMESTAMP not null comment '登录时间'
)
    comment '登录日志表';

create index idx_log_created
    on auth_login_log (created_at);

create index idx_log_status
    on auth_login_log (status);

create index idx_log_user
    on auth_login_log (user_id);

create table auth_password_history
(
    id         bigint auto_increment comment '历史ID'
        primary key,
    user_id    varchar(20)                         not null comment '用户ID',
    password   varchar(255)                        not null comment '密码(明文存储)',
    changed_at timestamp default CURRENT_TIMESTAMP not null comment '修改时间'
)
    comment '密码历史表';

create table auth_user
(
    id                    varchar(20)                                                     not null comment '用户ID(关联employees.id)'
        primary key,
    username              varchar(50)                                                     not null comment '登录用户名',
    password              varchar(255)                                                    not null comment '密码',
    email                 varchar(100)                                                    not null comment '邮箱',
    mobile                varchar(20)                                                     not null comment '手机号',
    status                enum ('active', 'locked', 'disabled') default 'active'          null comment '账号状态',
    failed_login_attempts int                                   default 0                 null comment '登录失败次数',
    locked_until          datetime                                                        null comment '锁定到期时间',
    password_changed_at   datetime                                                        null comment '密码最后修改时间',
    last_login_at         datetime                                                        null comment '最后登录时间',
    last_login_ip         varchar(50)                                                     null comment '最后登录IP',
    created_at            datetime                              default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at            datetime                              default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint email
        unique (email),
    constraint mobile
        unique (mobile),
    constraint username
        unique (username)
)
    comment '用户账号表';

create table auth_user_session
(
    id            bigint auto_increment comment '会话ID'
        primary key,
    user_id       varchar(20)                        not null comment '用户ID',
    token         varchar(500)                       not null comment 'JWT Token',
    refresh_token varchar(500)                       null comment '刷新Token',
    device_type   varchar(50)                        null comment '设备类型',
    device_name   varchar(100)                       null comment '设备名称',
    ip_address    varchar(50)                        null comment 'IP地址',
    user_agent    varchar(500)                       null comment '用户代理',
    expires_at    datetime                           not null comment '过期时间',
    created_at    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint token
        unique (token)
)
    comment '用户会话表';

create index idx_session_expires
    on auth_user_session (expires_at);

create index idx_session_token
    on auth_user_session (token);

create index idx_session_user
    on auth_user_session (user_id);

create table auth_verification_code
(
    id         bigint auto_increment comment '验证码ID'
        primary key,
    type       enum ('email', 'mobile')                      not null comment '类型: email邮箱 mobile手机',
    account    varchar(100)                                  not null comment '邮箱/手机号',
    scene      enum ('forgot_password', 'login', 'register') not null comment '场景: forgot_password找回密码 login登录 register注册',
    code       varchar(10)                                   not null comment '验证码',
    is_used    tinyint(1) default 0                          not null comment '是否已使用(0否1是)',
    used_at    timestamp                                     null comment '使用时间',
    expires_at timestamp                                     not null comment '过期时间',
    created_at timestamp  default CURRENT_TIMESTAMP          not null comment '创建时间'
)
    comment '验证码表';

create table biz_asset
(
    id                   varchar(20)                                                                               not null comment '资产编号(格式: ASSET+序号)'
        primary key,
    name                 varchar(200)                                                                              not null comment '资产名称',
    category             enum ('electronic', 'furniture', 'book', 'other')                                         not null comment '资产类别',
    brand_model          varchar(200)                                                                              null comment '品牌/型号',
    purchase_date        date                                                                                      not null comment '购置日期',
    purchase_price       decimal(10, 2)                                                                            not null comment '购置金额(元)',
    current_value        decimal(10, 2)                                                                            null comment '当前价值',
    status               enum ('stock', 'in_use', 'borrowed', 'maintenance', 'scrapped') default 'stock'           not null comment '资产状态',
    user_id              varchar(20)                                                                               null comment '使用人ID',
    location             varchar(200)                                                                              null comment '存放位置',
    borrow_date          date                                                                                      null comment '借出日期',
    expected_return_date date                                                                                      null comment '预计归还日期',
    actual_return_date   date                                                                                      null comment '实际归还日期',
    maintenance_record   json                                                                                      null comment '维护记录',
    images               json                                                                                      null comment '资产图片URL数组',
    notes                text                                                                                      null comment '备注信息',
    created_at           datetime                                                        default CURRENT_TIMESTAMP not null comment '创建时间',
    created_by           varchar(20)                                                                               null comment '创建人ID',
    updated_at           datetime                                                        default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    updated_by           varchar(20)                                                                               null comment '更新人ID',
    is_deleted           tinyint(1)                                                      default 0                 not null comment '是否删除',
    deleted_at           datetime                                                                                  null comment '删除时间',
    deleted_by           varchar(20)                                                                               null comment '删除人ID',
    version              int                                                             default 0                 not null comment '乐观锁版本号'
)
    comment '资产信息表';

create table biz_asset_borrow_record
(
    id                   bigint auto_increment comment '记录ID'
        primary key,
    asset_id             varchar(20)                                                      not null comment '资产ID',
    asset_name           varchar(200)                                                     not null comment '资产名称',
    borrower_id          varchar(20)                                                      not null comment '借用人ID',
    borrower_name        varchar(50)                                                      not null comment '借用人姓名',
    borrow_date          date                                                             not null comment '借出日期',
    expected_return_date date                                                             null comment '预计归还日期',
    actual_return_date   date                                                             null comment '实际归还日期',
    status               enum ('active', 'returned', 'overdue') default 'active'          not null comment '记录状态',
    notes                text                                                             null comment '备注',
    created_at           datetime                               default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at           datetime                               default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '资产借还记录表';

create index idx_borrow_asset
    on biz_asset_borrow_record (asset_id);

create index idx_borrow_borrower
    on biz_asset_borrow_record (borrower_id);

create index idx_borrow_status
    on biz_asset_borrow_record (status);

create table biz_asset_maintenance
(
    id            bigint auto_increment comment '记录ID'
        primary key,
    asset_id      varchar(20)                             not null comment '资产ID',
    type          enum ('repair', 'maintenance', 'check') not null comment '类型',
    description   text                                    not null comment '描述',
    cost          decimal(10, 2)                          null comment '费用',
    start_date    date                                    not null comment '开始日期',
    end_date      date                                    null comment '结束日期',
    operator_id   varchar(20)                             not null comment '操作人ID',
    operator_name varchar(50)                             not null comment '操作人姓名',
    created_at    datetime default CURRENT_TIMESTAMP      not null comment '创建时间'
)
    comment '资产维护记录表';

create index idx_maint_asset
    on biz_asset_maintenance (asset_id);

create index idx_maint_type
    on biz_asset_maintenance (type);

create table sys_department
(
    id               varchar(20)                                           not null comment '部门编号(格式: DEPT+4位序号)'
        primary key,
    name             varchar(100)                                          not null comment '部门名称',
    short_name       varchar(50)                                           null comment '部门简称',
    parent_id        varchar(20)                                           null comment '上级部门ID(NULL表示顶级)',
    leader_id        varchar(20)                                           not null comment '部门负责人ID',
    level            int                         default 1                 not null comment '部门层级(从1开始)',
    sort             int                         default 0                 not null comment '排序号',
    established_date date                                                  null comment '成立日期',
    description      text                                                  null comment '部门描述',
    icon             varchar(500)                                          null comment '部门图标URL',
    status           enum ('active', 'disabled') default 'active'          not null comment '状态',
    created_at       datetime                    default CURRENT_TIMESTAMP not null comment '创建时间',
    created_by       varchar(20)                                           null comment '创建人ID',
    updated_at       datetime                    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    updated_by       varchar(20)                                           null comment '更新人ID',
    is_deleted       tinyint(1)                  default 0                 not null comment '是否删除(0否1是)',
    deleted_at       datetime                                              null comment '删除时间',
    deleted_by       varchar(20)                                           null comment '删除人ID',
    version          int                         default 0                 not null comment '乐观锁版本号'
)
    comment '部门信息表';

create table sys_department_member
(
    id            bigint auto_increment comment '关系ID'
        primary key,
    department_id varchar(20)                          not null comment '部门ID',
    employee_id   varchar(20)                          not null comment '员工ID',
    is_leader     tinyint(1) default 0                 null comment '是否为负责人(0否1是)',
    join_date     date                                 not null comment '加入部门日期',
    leave_date    date                                 null comment '离开部门日期',
    created_at    datetime   default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at    datetime   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_dept_member
        unique (department_id, employee_id, leave_date)
)
    comment '部门成员关系表';

create table sys_dict_item
(
    id         bigint auto_increment comment '字典项ID'
        primary key,
    type_code  varchar(50)                                            not null comment '字典类型编码',
    label      varchar(100)                                           not null comment '字典标签',
    value      varchar(100)                                           not null comment '字典值',
    color      varchar(20)                                            null comment '显示颜色',
    icon       varchar(100)                                           null comment '显示图标',
    sort       int                          default 0                 null comment '排序号',
    status     enum ('enabled', 'disabled') default 'enabled'         not null comment '状态',
    created_at datetime                     default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at datetime                     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_type_value
        unique (type_code, value)
)
    comment '数据字典项表';

create table sys_dict_type
(
    id          bigint auto_increment comment '字典类型ID'
        primary key,
    code        varchar(50)                                            not null comment '字典编码',
    name        varchar(100)                                           not null comment '字典名称',
    description varchar(500)                                           null comment '字典描述',
    category    enum ('system', 'business')                            not null comment '字典类别',
    status      enum ('enabled', 'disabled') default 'enabled'         not null comment '状态',
    sort        int                          default 0                 null comment '排序号',
    created_at  datetime                     default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  datetime                     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint code
        unique (code)
)
    comment '数据字典类型表';

create table sys_employee
(
    id                 varchar(20)                                                         not null comment '员工编号(格式: EMP+YYYYMMDD+序号)'
        primary key,
    name               varchar(50)                                                         not null comment '姓名',
    english_name       varchar(50)                                                         null comment '英文名',
    gender             enum ('male', 'female')                                             not null comment '性别',
    birth_date         date                                                                null comment '出生日期',
    phone              varchar(20)                                                         not null comment '联系电话',
    email              varchar(100)                                                        not null comment '邮箱',
    avatar             varchar(500)                                                        null comment '头像URL',
    department_id      varchar(20)                                                         not null comment '部门ID',
    position           varchar(100)                                                        not null comment '职位',
    level              varchar(50)                                                         null comment '职级',
    manager_id         varchar(20)                                                         null comment '直属上级ID',
    join_date          date                                                                not null comment '入职日期',
    probation_status   enum ('probation', 'regular', 'resigned') default 'regular'         null comment '试用期状态',
    probation_end_date date                                                                null comment '试用期结束日期',
    work_years         int                                       default 0                 null comment '工龄(年)',
    status             enum ('active', 'resigned', 'suspended')  default 'active'          not null comment '员工状态',
    office_location    varchar(200)                                                        null comment '办公位置',
    emergency_contact  varchar(50)                                                         null comment '紧急联系人',
    emergency_phone    varchar(20)                                                         null comment '紧急联系电话',
    created_at         datetime                                  default CURRENT_TIMESTAMP not null comment '创建时间',
    created_by         varchar(20)                                                         null comment '创建人ID',
    updated_at         datetime                                  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    updated_by         varchar(20)                                                         null comment '更新人ID',
    is_deleted         tinyint(1)                                default 0                 not null comment '是否删除(0否1是)',
    deleted_at         datetime                                                            null comment '删除时间',
    deleted_by         varchar(20)                                                         null comment '删除人ID',
    version            int                                       default 0                 not null comment '乐观锁版本号'
)
    comment '员工信息表';

create table sys_employee_operation_log
(
    id            bigint auto_increment comment '日志ID'
        primary key,
    employee_id   varchar(20)                        not null comment '员工ID',
    operation     varchar(50)                        not null comment '操作类型',
    operator      varchar(20)                        not null comment '操作人ID',
    operator_name varchar(50)                        null comment '操作人姓名',
    timestamp     datetime default CURRENT_TIMESTAMP not null comment '操作时间',
    details       text                               null comment '详细信息',
    ip_address    varchar(50)                        null comment 'IP地址',
    user_agent    varchar(500)                       null comment '用户代理'
)
    comment '员工操作日志表';

create table sys_menu
(
    id             bigint auto_increment comment '菜单ID'
        primary key,
    menu_code      varchar(50)                                            not null comment '菜单编号(MENU+4位序号)',
    menu_name      varchar(100)                                           not null comment '菜单名称',
    menu_type      enum ('directory', 'menu', 'button')                   not null comment '菜单类型',
    parent_id      bigint                       default 0                 null comment '父级菜单ID(0表示一级菜单)',
    menu_level     int                          default 1                 not null comment '菜单层级(1-3)',
    route_path     varchar(200)                                           null comment '路由路径',
    component_path varchar(200)                                           null comment '组件路径',
    icon           varchar(100)                                           null comment '菜单图标',
    sort           int                          default 0                 null comment '排序号',
    visible        tinyint(1)                   default 1                 null comment '是否可见(0否1是)',
    status         enum ('enabled', 'disabled') default 'enabled'         null comment '状态',
    created_at     datetime                     default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at     datetime                     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint menu_code
        unique (menu_code)
)
    comment '菜单管理表';

create table sys_role_menu
(
    id         bigint auto_increment comment '关联ID'
        primary key,
    role_id    bigint                             not null comment '角色ID',
    menu_id    bigint                             not null comment '菜单ID',
    created_by bigint                             null comment '创建人ID',
    created_at datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_role_menu
        unique (role_id, menu_id)
)
    comment '角色菜单关联表';

