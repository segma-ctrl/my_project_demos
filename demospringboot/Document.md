# 基于微服务架构的高校教学预约管理平台设计与实现

---

## 1. 项目概述

### 1.1 项目背景

随着高校教学资源（教室、实验室、设备等）日益紧张，传统预约方式存在：

* 流程复杂
* 并发能力差
* 资源利用率低
* 无法全流程管理

本项目基于**微服务架构**，构建一个高可用、可扩展的教学预约管理平台。

---

### 1.2 项目目标

* 实现教学资源统一管理
* 支持高并发预约
* 实现预约全流程闭环（预约→审批→使用→签到）
* 提供数据统计与分析能力

---

## 2. 系统总体架构

### 2.1 架构风格

* 微服务架构（Spring Cloud Alibaba）
* 前后端分离（Vue3 + Spring Boot）
* RESTful API

---

### 2.2 技术选型

#### 后端

* Spring Boot
* Spring Cloud Alibaba（Nacos、Gateway）
* MyBatis-Plus
* Redis（缓存 + 分布式锁）
* RabbitMQ（消息队列）
* XXL-JOB（定时任务）

#### 前端

* Vue3 + Vite
* Ant Design Vue

#### 运维

* Docker
* Nginx

---

### 2.3 微服务划分

```text
用户服务（user-service）
资源服务（resource-service）
预约服务（booking-service）
审批服务（approval-service）
通知服务（notification-service）
统计服务（statistics-service）
网关服务（gateway）
```

---

## 3. 功能模块设计（核心🔥）

### 3.1 用户模块

#### 功能

* 用户注册/登录
* 角色管理（学生/教师/管理员）

#### 实现

* JWT认证
* RBAC权限控制

---

### 3.2 资源管理模块

#### 功能

* 教室/实验室/设备管理
* 时间段配置
* 使用规则配置

#### 表设计

```sql
resource(id, name, type, capacity, status)
resource_schedule(id, resource_id, time_slot, status)
```

---

### 3.3 预约模块（最核心🔥）

#### 功能

* 预约申请
* 冲突检测
* 状态跟踪

#### 核心难点

* 并发控制
* 时间冲突判断

#### 实现方案

```text
1. Redis分布式锁
2. 乐观锁（version字段）
3. 数据库唯一索引（防重复预约）
```

---

### 3.4 审批模块

#### 功能

* 管理员全局审批（学生与老师的申请统一由管理员后台处理）
* 审批历史导出

#### 状态流转

```text
待审批 → 已通过 → 已拒绝
```

---

### 3.5 通知模块

#### 功能

* 预约成功通知
* 审批结果通知
* 提醒通知

#### 实现

* RabbitMQ异步发送
* 邮件/站内信

---

### 3.6 统计模块（加分项🔥）

#### 功能

* 资源使用率统计
* 用户活跃度
* 热门资源分析

#### 技术

* ECharts可视化

---

## 4. 数据库设计（核心表）

### 用户表

```sql
user(id, username, password, role, create_time)
```

### 资源表

```sql
resource(id, name, type, capacity)
```

### 预约表

```sql
booking(id, user_id, resource_id, start_time, end_time, status)
```

### 审批表

```sql
approval(id, booking_id, approver_id, status, comment)
```

---

## 5. 后端实现设计

### 5.1 包结构（建议🔥）

```text
com.xxx
 ├── controller
 ├── service
 ├── mapper
 ├── entity
 ├── dto
 ├── config
```

---

### 5.2 核心接口示例

```java
POST /booking/create
GET  /booking/list
POST /approval/approve
```

---

## 6. 前端设计

### 页面结构

* 登录页
* 首页（资源日历）
* 预约页面
* 我的预约
* 管理后台

---

### 核心功能

* 日历预约（类似日程表）
* 状态展示（颜色区分）

---

## 7. 定时任务设计（你必须做🔥）

### 任务1：自动取消超时预约

```java
@Scheduled(cron = "0 */5 * * * ?")
```

逻辑：

* 超过时间未使用 → 自动取消

---

### 任务2：预约提醒

* 提前30分钟通知用户

---

### 任务3：统计任务

* 每天统计使用率

---

## 8. 系统难点解决方案

### 8.1 高并发预约

* Redis锁
* MQ削峰

---

### 8.2 分布式事务

* Saga模式
* MQ最终一致性

---

### 8.3 服务治理

* Nacos注册中心
* Sentinel限流

---

## 9. 开发步骤（最重要🔥）

```text
第1步：搭建环境（SpringBoot + Vue）
第2步：设计数据库
第3步：实现用户模块
第4步：实现资源模块
第5步：实现预约模块（重点）
第6步：实现审批模块
第7步：接入Redis + MQ
第8步：写定时任务
第9步：前后端联调
第10步：优化 & 测试
```

---

## 10. 加分点（建议你做2个）

* Excel导出
* 数据可视化
* 签到功能（扫码）

---

## 11. 预期成果

* 完整系统（可运行）
* 论文
* 答辩PPT

---


com.xxx

├── common                 # 公共模块（工具类、统一返回）
│
├── user                  # 用户模块（未来 user-service）
│   ├── controller
│   ├── service
│   ├── mapper
│   └── entity
│
├── course                # 课程模块（未来 course-service）
│
├── reservation           # 预约模块（未来 reservation-service）
│
├── notification          # 通知模块（未来 notification-service）
│
└── system                # 系统模块


前端（Vue3）
↓
API网关（Gateway）
↓
-------------------------
| user-service         |
| course-service       |
| reservation-service  |
| notification-service |
| system-service       |
-------------------------
        ↓
各自数据库

---

## 12. 预约管理模块深度设计（业务流程与实施路径）🔥

为了确保预约管理模块的逻辑闭环与高性能，以下是详细的业务逻辑梳理及实施计划：

### 12.1 核心业务流程图（Swimlane Flow）

| 参与方 | 关键动作流 |
| :--- | :--- |
| **预约者 (学生/老师)** | 1. **资源检索** (按类型/人数/时段) → 2. **查看排期** (看板) → 3. **提交预约** (填写用途/所属学院) → 4. **状态追踪** (待审批/已通过/已过期) |
| **系统 (Service 层)** | 1. **冲突校验** (检查该时段是否重叠) → 2. **库存锁定** (DB 事务) → 3. **状态同步** (更新 resource_schedule 状态) → 4. **超时回收** (定时任务) |
| **管理员 (审批端)** | 1. **全量审核** (处理全校师生的预约申请) → 2. **合规性判定** (审核资源用途是否合理) → 3. **结果判定** (通过/驳回 + 意见) |

---

### 12.2 具体功能实现清单

#### A. 后端接口设计 (RESTful)
1.  `GET /api/resource/available`：根据日期、类型、时段检索可用资源列表。
2.  `GET /api/booking/schedule/{resourceId}`：获取指定资源在特定日期的 24 小时占用情况。
3.  `POST /api/booking/submit`：提交预约申请。**核心逻辑**：必须在 `Transactional` 事务内，先检查是否有重叠记录，再插入。
4.  `PUT /api/approval/handle`：审批操作。更新 `booking` 状态并同步写入 `approval` 记录。

#### B. 关键技术细节
- **冲突检测算法**：
    - 已有预约段 $[S_{old}, E_{old}]$，新预约段 $[S_{new}, E_{new}]$。
    - 冲突条件：`MAX(S_old, S_new) < MIN(E_old, E_new)`。
- **并发控制**：
    - 为了防止“超卖”，在 `submit` 接口使用 **Redis 分布式锁**（Key 为 `resource_id + date + time_slot`）。

---

### 12.3 后续开发任务分解 (Todo List)

#### 第一阶段：数据库与基础类 (1-2天)
- [ ] **DB 扩展**：创建 `sys_booking`（主表）和 `sys_resource_schedule`（资源排期表，用于快速查询）。
- [ ] **Entity & Mapper**：编写 MyBatis-Plus 的 Entity 类与 Mapper 接口。
- [ ] **枚举定义**：完善 `ResourceType`, `BookingStatus`, `TimeSlot` 等枚举。

#### 第二阶段：核心业务逻辑 (2-3天)
- [ ] **查询逻辑**：实现带复杂过滤条件的资源查询 SQL。
- [ ] **预约逻辑 (Hard)**：编写 Service 层的冲突校验算法，集成 Redis 锁。
- [ ] **我的预约**：分角色（学生看自己，老师看待审批列表）的分页查询接口。

#### 第三阶段：前端功能填充 (2-3天)
- [ ] **资源预约抽屉**：填充真实的日期选择器和时段按钮组。
- [ ] **角色权限适配**：确保学生和老师都能在自己的界面看到“发起预约”按钮。
- [ ] **管理员后台**：开发管理员专用的“预约审核”管理页。

#### 第四阶段：联调与优化 (1-2天)
- [ ] **前端面包屑增强**：根据不同审批状态（待审/通过/拒绝）在详情页展示对应的 UI。
- [ ] **异常处理**：处理如“登录过期”、“并发占座失败”等边界情况的提示。