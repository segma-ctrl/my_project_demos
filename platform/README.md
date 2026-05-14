# 高校教学预约 — 微服务第一版（platform）

本目录为 **Maven 多模块** 工程：`gateway-service`、`user-service`、`resource-service`、`booking-service` 与 `common-lib`。

## 前置条件

- JDK 17、Maven 3.8+
- MySQL（库名与单体一致，默认 `wwq_bs`，见各服务 `application.yml`）
- Redis（仅 **booking-service** 使用 Redisson）
- 先启动 **MySQL + Redis**，再启动各服务

## 端口

| 服务 | 端口 |
|------|------|
| gateway-service | 9000 |
| user-service | 9001 |
| resource-service | 9002 |
| booking-service | 9003 |

## 启动顺序（本地静态路由，未接 Nacos）

1. `user-service` → 9001  
2. `booking-service` → 9003（**resource-service** 会通过 Feign 直连本端口）  
3. `resource-service` → 9002  
4. `gateway-service` → 9000  

```bash
cd platform
mvn -pl user-service spring-boot:run
# 新终端
mvn -pl booking-service spring-boot:run
mvn -pl resource-service spring-boot:run
mvn -pl gateway-service spring-boot:run
```

## 前端（demovue3）

将请求基地址改为网关，例如：`http://localhost:9000`（可用环境变量 `VITE_API_BASE` 统一配置）。

网关已配置 **CORS**，允许 `http://localhost:5173` 等来源。

## 毕设论文与部署素材

仓库内 **`docs/thesis/`** 提供：图表（Mermaid → draw.io）、接口说明、测试用例表、对比实验模板、云部署与 Nginx 示例。按该目录下 **`README.md`** 分步完成即可。

## 与单体的关系

- 原 **`demospringboot`** 仍保留，可作为对照；新业务以 **`platform`** 为准迭代。
- **JWT** 尚未接入网关，后续在 `gateway-service` + `user-service` 登录链路中补充。

## 内部接口

- `GET /internal/bookings/conflicting-resource-ids` 仅用于 **resource-service → booking-service** Feign 调用，**未挂到网关**；生产环境应对内网或 mTLS 做访问控制。

## 构建

```bash
cd platform
mvn clean package -DskipTests
```
