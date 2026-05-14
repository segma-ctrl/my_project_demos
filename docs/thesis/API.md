# 接口说明（网关对外 + 内部 Feign）

本文档与仓库 `platform/gateway-service/src/main/resources/application.yml` 路由一致，便于论文「接口设计」章节与附录。

---

## 一、网关统一入口

| 项目 | 值 |
|------|-----|
| 开发环境网关基地址 | `http://localhost:9000` |
| 生产环境 | 由 Nginx 反代至网关（见 `DEPLOYMENT.md`） |

浏览器与 Vue 前端**只应**访问网关端口；**不**应直连 9001～9003（除非开发调试并在论文中注明为例外）。

---

## 二、经网关转发的对外 API 一览

以下路径均由 **Gateway (9000)** 接收，再转发到对应服务（第二列）。

| HTTP | 路径模式 | 转发至 | 说明 |
|------|----------|--------|------|
| POST | `/api/login` | user-service | 登录，请求体 JSON：`username, password, role` |
| POST | `/api/register` | user-service | 注册 |
| POST | `/api/changePassword` | user-service | 修改密码 |
| GET | `/api/admin/user/list` | user-service | 管理员：用户列表 |
| POST | `/api/admin/user/add` | user-service | 管理员：新增用户 |
| POST | `/api/admin/user/delete/{id}` | user-service | 管理员：删除用户 |
| GET | `/api/admin/user/downloadTemplate` | user-service | 下载导入模板 |
| POST | `/api/admin/user/import` | user-service | Excel 批量导入 |
| GET | `/api/admin/list` | resource-service | 管理员：资源列表 |
| POST | `/api/admin/add` | resource-service | 管理员：新增资源 |
| POST | `/api/admin/disable` | resource-service | 停用资源 |
| POST | `/api/admin/enable` | resource-service | 启用资源 |
| POST | `/api/resource` | resource-service | 用户侧：可用资源查询（含时段） |
| POST | `/api/booking/list` | booking-service | 我的预约列表 |
| POST | `/api/booking/add` | booking-service | 提交预约 |
| POST | `/api/booking/cancel` | booking-service | 取消预约 |
| GET | `/api/approval/list` | booking-service | 审批列表 |
| POST | `/api/approval/approve` | booking-service | 通过审批 |
| POST | `/api/approval/reject` | booking-service | 驳回审批 |

**健康检查（运维 / 实验）**：

| GET | `/actuator/health` | gateway 自身 |

---

## 三、不经过网关的内部接口（OpenFeign）

resource-service 在聚合「某时段可用资源」时，需向 booking-service 查询已占用资源 ID，该调用**不暴露在网关路由**中。

| 项目 | 值 |
|------|-----|
| 调用方 | `resource-service`（`BookingFeignClient`） |
| 被调用方 | `booking-service`，默认 `http://127.0.0.1:9003`（配置项 `booking.service.url`） |
| HTTP | `GET` |
| 路径 | `/internal/bookings/conflicting-resource-ids` |
| 查询参数 | `date`（ISO 日期）、`startTime`、`endTime`（ISO 时间） |
| 响应 | `List<String>`，为与已存在预约冲突的资源 ID 列表 |

**论文表述建议**：说明该接口为**服务间协作**使用，生产环境应通过内网、防火墙或 mTLS 限制访问，避免公网扫描。

---

## 四、OpenAPI（Swagger）截图位置

各服务已配置 `springdoc`，本地启动后可打开：

| 服务 | Swagger UI（示例 URL） |
|------|-------------------------|
| user-service | `http://localhost:9001/swagger-ui.html` |
| resource-service | `http://localhost:9002/swagger-ui.html` |
| booking-service | `http://localhost:9003/swagger-ui.html` |

论文中建议：**截 1～2 张** booking 或 resource 的接口列表图，并在正文中写「对外路径以网关路由表为准，Swagger 为各服务实现级文档」。

---

## 五、与单体 `demospringboot` 的关系（可选段落）

若论文对比单体与微服务：单体阶段路径多为 `/api/...` 单进程；微服务阶段路径形式可保持一致，由网关转发到不同进程，利于前端迁移与对照实验。
