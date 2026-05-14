# 图：系统总体逻辑架构（微服务 + 网关）

**论文章节建议**：第 4 章「系统总体设计」— 逻辑架构 / 分层结构。

```mermaid
flowchart TB
  subgraph client["客户端层"]
    B[浏览器]
    V[Vue3 前端\nVite 5173]
  end

  subgraph edge["接入层"]
    GW[Spring Cloud Gateway\n端口 9000\n统一路由 / CORS]
  end

  subgraph services["业务微服务层"]
    U[user-service\n9001\n认证 / 用户管理]
    R[resource-service\n9002\n资源 / 管理端资源接口]
    BK[booking-service\n9003\n预约 / 审批 / Redisson]
  end

  subgraph data["数据与中间件层"]
    DB[(MySQL\n库 wwq_bs)]
    RD[(Redis\nRedisson)]
  end

  B --> V
  V -->|HTTPS 可选\n/api/**| GW
  GW -->|/api/login 等| U
  GW -->|/api/resource\n/api/admin/list 等| R
  GW -->|/api/booking/**\n/api/approval/**| BK
  U --> DB
  R --> DB
  BK --> DB
  BK --> RD
  R -->|Feign 内部\n不经网关| BK
```

**正文可写 2～3 句（示例）**：  
浏览器仅访问网关端口；用户与权限相关请求转发至 user-service；资源展示与管理部分转发至 resource-service；预约与审批转发至 booking-service。resource-service 在聚合可用资源时通过 OpenFeign 调用 booking-service 的内部接口，实现跨服务冲突检测。
