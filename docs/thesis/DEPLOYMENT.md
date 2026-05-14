# 云服务器部署说明（毕设实践 + 论文第 8 章）

以下步骤假设：**一台 Linux 云主机**（Ubuntu 22.04 等）、已有 **域名**（可选 HTTPS）、本仓库已在本地能跑通微服务。

---

## 1 服务器环境准备

| 软件 | 建议版本 | 用途 |
|------|----------|------|
| JDK | 17 | 运行各 Spring Boot jar |
| MySQL | 8.x | 库 `wwq_bs`，导入与本地一致的结构与数据 |
| Redis | 6.x+ | booking-service |
| Nginx | 稳定版 | HTTPS、静态前端、反代 `/api` |
| Maven | 3.8+（仅在服务器上构建时需要） | `mvn package` |

**防火墙 / 安全组**：开放 **80、443**；**不要**对公网开放 3306、6379、9000～9003（仅本机或内网访问）。

---

## 2 构建产物（在开发机或 CI 上执行）

```bash
cd platform
mvn clean package -DskipTests
```

得到 jar 路径示例：

- `gateway-service/target/gateway-service-1.0.0-SNAPSHOT.jar`
- `user-service/target/user-service-1.0.0-SNAPSHOT.jar`
- `resource-service/target/resource-service-1.0.0-SNAPSHOT.jar`
- `booking-service/target/booking-service-1.0.0-SNAPSHOT.jar`

将四个 jar 与 `nginx.example.conf` 修改版一并上传到服务器目录，例如 `/opt/mybs/`。

---

## 3 配置要点（生产）

1. **各服务 `application.yml`（或外部配置）**  
   - 将 `spring.datasource.url` 中主机改为本机 MySQL 或云 RDS 地址。  
   - `booking.service.url`（resource-service）应为 **`http://127.0.0.1:9003`**（同机部署时）。

2. **网关路由中的 `uri`**  
   当前为 `http://127.0.0.1:9001` 等；服务与网关**同机**时保持不变即可。

3. **不要在公网仓库提交真实密码**；用环境变量或服务器本地 `application-prod.yml`（加入 `.gitignore`）。

---

## 4 启动顺序（systemd 或脚本）

与本地一致：**user → booking → resource → gateway**。

示例（前台调试，每服务一个 SSH 会话）：

```bash
java -jar user-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
java -jar booking-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
java -jar resource-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
java -jar gateway-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

生产建议使用 **systemd** 四个 service 单元，设置 `After=` 依赖与 `Restart=on-failure`。

---

## 5 前端构建与 `VITE_API_BASE`

在开发机：

```bash
cd demovue3
# 将下面域名换成你的 Nginx 对外域名（HTTPS 则写 https://）
set VITE_API_BASE=https://你的域名
npm run build
```

Linux/macOS：

```bash
VITE_API_BASE=https://你的域名 npm run build
```

将 `dist/` 目录上传到服务器，例如 `/var/www/mybs/`。

---

## 6 Nginx

复制本目录下 [`nginx.example.conf`](nginx.example.conf) 为站点配置，修改：

- `server_name`
- `ssl_certificate` / `ssl_certificate_key`（若用 HTTPS）
- `root` 指向前端 `dist`
- `proxy_pass http://127.0.0.1:9000` 保持不变（网关）

```bash
sudo nginx -t && sudo systemctl reload nginx
```

---

## 7 常见问题（论文「踩坑与解决」素材）

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 前端能开但登录失败、控制台 CORS | 网关 `allowedOrigins` 未含你的前端域名 | 在 `application.yml` 的 `globalcors` 中增加 `https://你的域名` 并重启网关 |
| 502 Bad Gateway | 网关后某服务未启动 | 按顺序检查 9001～9003 与日志 |
| 静态页空白、资源 404 | `root` 或 `try_files` 配置错误 | 对照 `nginx.example.conf` |
| Feign 调用失败 | `booking.service.url` 不可达 | 同机用 `127.0.0.1:9003`；跨机改为内网 IP |

**曾遇问题**：本地开发时网关与 user-service **重复配置 CORS** 导致响应头重复、浏览器报 `Failed to fetch`；生产环境**仅在网关配置 CORS**（见仓库当前代码状态）。

---

## 8 可选：仅用 Docker 跑 MySQL + Redis

见同目录 [`docker-compose.infra.yml`](docker-compose.infra.yml)，减轻在服务器上手动安装数据库的负担；应用仍用 `java -jar` 运行，论文中可写「数据层容器化与应用进程分离」。
