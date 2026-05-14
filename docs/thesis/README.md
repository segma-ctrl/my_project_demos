# 毕设论文与部署 — 分步完成指南

按下面顺序做即可；每步产出都可直接放进论文（图、表、附录）或用于答辩演示。

---

## 第一步：图表（创新点与架构）

| 序号 | 文件 | 论文建议位置 | 你要做的事 |
|------|------|----------------|------------|
| 1 | [`diagrams/HOWTO-DRAWIO.md`](diagrams/HOWTO-DRAWIO.md) | — | 先读：如何把 Mermaid 导入 draw.io / 如何截图 |
| 2 | [`diagrams/01-system-architecture.md`](diagrams/01-system-architecture.md) | 第 4 章 总体设计 | 生成图 → 插入论文「逻辑架构」 |
| 3 | [`diagrams/02-deployment-topology.md`](diagrams/02-deployment-topology.md) | 第 8 章 部署 | 按你云主机 IP/域名改字后出图 |
| 4 | [`diagrams/03-sequence-login.md`](diagrams/03-sequence-login.md) | 第 5～6 章 实现 | 登录时序 |
| 5 | [`diagrams/04-sequence-booking-submit.md`](diagrams/04-sequence-booking-submit.md) | 同上 | 预约提交时序 |
| 6 | [`diagrams/05-sequence-resource-feign.md`](diagrams/05-sequence-resource-feign.md) | **创新点**：跨服务冲突检测 | Feign 调用内部接口时序 |
| 7 | [`diagrams/06-er-core-tables.md`](diagrams/06-er-core-tables.md) | 第 5 章 数据库设计 | ER / 核心表关系 |

**完成标准**：论文章节里至少有 1 张总体架构、1 张部署拓扑、2～3 张时序图、1 张 ER；图下各写 2～4 句「图源说明」。

---

## 第二步：接口说明与 OpenAPI

| 文件 | 说明 |
|------|------|
| [`API.md`](API.md) | 网关对外路径表 + 内部 Feign 说明（论文「接口设计」或附录） |

**本地截图（答辩可用）**：

1. 启动四个微服务 + 网关后，在浏览器打开（按你本机端口）：
   - 用户：`http://localhost:9001/swagger-ui.html`
   - 资源：`http://localhost:9002/swagger-ui.html`
   - 预约：`http://localhost:9003/swagger-ui.html`
2. 论文中说明：**浏览器经网关 `9000` 访问对外 API**；Swagger 为各服务**独立文档**，与网关路由对照 [`API.md`](API.md) 中的表。

---

## 第三步：测试用例表与截图

| 文件 | 说明 |
|------|------|
| [`TEST_CASES.md`](TEST_CASES.md) | 登录 / 预约 / 审批 / 管理员 各若干条用例表 |

**你要做的事**：按表中步骤在系统里操作一遍，每个模块保留 **1～2 张界面截图**（贴论文「测试」一章，图号连续）。

---

## 第四步：单体 vs 微服务对比实验

| 文件 | 说明 |
|------|------|
| [`EXPERIMENT.md`](EXPERIMENT.md) | 实验目的、步骤、结果记录表（自己填数字即可） |

**你要做的事**：按文档用 `curl` 或浏览器开发者工具记录响应时间；并发测试可选 JMeter，没有也可只做「单接口多次平均」。

---

## 第五步：云服务器部署

| 文件 | 说明 |
|------|------|
| [`DEPLOYMENT.md`](DEPLOYMENT.md) | 环境、启动顺序、Nginx、HTTPS、`VITE_API_BASE`、常见问题 |
| [`docker-compose.infra.yml`](docker-compose.infra.yml) | 仅 MySQL + Redis（可选，减轻本机/服务器装库负担） |
| [`nginx.example.conf`](nginx.example.conf) | Nginx 反代网关 + 静态前端示例 |

**完成标准**：能用自己的域名（或 IP）打开前端并完成一次登录；论文里写「环境、步骤、防火墙/CORS 等踩坑与解决」。

---

## 建议时间分配（参考）

| 阶段 | 大致时间 |
|------|----------|
| 图表导出与插入 Word | 0.5～1 天 |
| 接口说明 + Swagger 截图 | 0.5 天 |
| 测试执行 + 截图 | 0.5～1 天 |
| 对比实验填表 | 0.5 天 |
| 云部署 + 记录问题 | 1～2 天 |

全部完成后，把本 `README.md` 的目录结构附在论文**附录「文档与工程目录说明」**中，显得工程规范。
