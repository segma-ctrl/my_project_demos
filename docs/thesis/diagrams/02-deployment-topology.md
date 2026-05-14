# 图：云服务器部署拓扑（示例）

**论文章节建议**：第 8 章「系统部署」— 部署拓扑 / 网络结构。

> 将文中 `your-domain.com` 换成你的域名；若仅用 IP，将 HTTPS 改为 HTTP 并说明用于演示。

```mermaid
flowchart LR
  subgraph internet["互联网用户"]
    U[用户浏览器]
  end

  subgraph cloud["云主机（示例）"]
    NG[Nginx\n443 / 80]
    FE[静态文件\ndemovue3 dist]
    GW[Gateway\n9000]
    S1[user 9001]
    S2[resource 9002]
    S3[booking 9003]
    MY[(MySQL 3306)]
    RD[(Redis 6379)]
  end

  U -->|HTTPS| NG
  NG -->|/ 根路径| FE
  NG -->|/api 反代| GW
  GW --> S1
  GW --> S2
  GW --> S3
  S1 --> MY
  S2 --> MY
  S3 --> MY
  S3 --> RD
```

**前端构建说明（论文一句话）**：  
生产环境前端通过 `npm run build` 生成静态资源，由 Nginx 托管；接口基地址 `VITE_API_BASE` 配置为 `https://your-domain.com`（与 Nginx 对外域名一致），由 Nginx 将 `/api` 转发至网关 `127.0.0.1:9000`。
