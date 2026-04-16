# Jeecg-Boot 项目后端结构分析

本项目是一个基于 **Spring Boot 3** 和 **Spring Cloud / Spring Cloud Alibaba** 的大型多模块 Maven 项目（目前配置主要使用的是 JDK 17）。它的项目架构设计既支持单体运行 (Monolithic)，也支持微服务 (Microservices) 部署模式。

以下是主要的模块和目录结构分析：

## 1. 根目录关键文件和目录
- **`pom.xml`**: Maven 的根父级 POM 文件，用于全局的依赖版本管理（如 `spring-boot`, `spring-cloud-alibaba`, `mybatis-plus`, 各类数据库驱动等）。
- **`docker-compose.yml`**: Docker 容器化编排文件，主要用于快速启动各种中间件或服务。
- **`db/`**: 通常存放项目初始化所需的数据库 SQL 脚本（结构和基础数据）。
- **`README.md` / `LICENSE`**: 项目说明和开源协议文件。

## 2. 核心模块分析
项目按功能和架构层面划分为以下几个核心子模块：

### `jeecg-boot-base-core` (核心基础模块)
该模块是所有业务模块的基石，封装了系统最底层、最通用的代码：
- **公共工具和配置**: 包含诸如全局异常处理、Redis 配置、安全框架 (Shiro/JWT 等) 集成配置、各类 Util 工具类等，被项目内其他所有的业务模块所依赖。

### `jeecg-module-system` (系统权限核心模块)
负责系统本身的基础核心业务逻辑（如组织架构、用户、角色、菜单、数据字典管理）。进一步向下拆分为多个子包：
- **`jeecg-system-api`**: 系统内部的 API 定义模块。在微服务架构下，通常提供 Feign Client 接口和相关的 DTO 给其他服务或模块进行内部远程调用。
- **`jeecg-system-biz`**: 系统核心模块的具体业务逻辑实现层（Service / Mapper / Controller）。
- **`jeecg-system-start`**: 系统的单体应用程序启动入口（Main Class 所在处），如果在单体模式下运行，通常会打包该模块。

### `jeecg-boot-module` (扩展业务聚合模块)
这是一个起聚合作用 (Aggregator) 的模块目录，主要存放除系统核心外的**附加业务或演示模块**。
- 支持将各种独立的功能做成可插拔的 Maven 模块。例如此目录下包含了 `jeecg-module-demo`（官方演示示例模块）和 `jeecg-boot-module-airag` 等具体的自定义扩展业务模块。

### `jeecg-server-cloud` (微服务与云原生环境模块)
该目录包含了 Spring Cloud 微服务架构相关的服务端点和依赖项。在 SpringCloud 配置 Profile 激活时启用：
- **`jeecg-cloud-gateway`**: 微服务网关模块，基于 Spring Cloud Gateway，负责外部请求的路由转发、统一鉴权拦截等。
- **`jeecg-cloud-nacos`**: 包含 Nacos 服务注册中心及配置中心的特定适配或集成。
- **`jeecg-system-cloud-start` / `jeecg-demo-cloud-start`**: 微服务模式下的独立启动模块。将 System 系统模块或 Demo 演示模块作为完全隔离的微服务启动起来的 Bootstrap 项目。
- **`jeecg-visual`**: 通常包含独立的运维可视化和服务监控模块程序。

## 3. 纯文件结构树 (核心层级)

```text
jeecg-boot/
├── db/
├── jeecg-boot-base-core/
│   ├── src/
│   └── pom.xml
├── jeecg-boot-module/
│   ├── jeecg-boot-module-airag/
│   ├── jeecg-module-demo/
│   └── pom.xml
├── jeecg-module-system/
│   ├── jeecg-system-api/
│   ├── jeecg-system-biz/
│   ├── jeecg-system-start/
│   └── pom.xml
├── jeecg-server-cloud/
│   ├── docker-compose.yml
│   ├── jeecg-cloud-gateway/
│   ├── jeecg-cloud-nacos/
│   ├── jeecg-demo-cloud-start/
│   ├── jeecg-system-cloud-start/
│   ├── jeecg-visual/
│   └── pom.xml
├── .gitignore
├── docker-compose.yml
├── LICENSE
├── pom.xml
└── README.md
```

## 4. 将现有项目整合到 Jeecg-Boot 框架下的建议实施方案

如果需要将当前项目的业务逻辑（如 `reservation` 预约、`course` 课程、`task` 任务调度等）重构并迁移至 Jeecg-Boot 体系，建议不要破坏框架底层结构，应采取以下架构整合策略：

### 4.1 创建独立的自定义业务模块
Jeecg-Boot 设计为良好的多模块架构以支持功能解耦和热插拔。
1. **新建模块目录**: 在项目约定的扩展聚合层 `jeecg-boot-module/` 目录下，新建一个 Maven 模块（例如：`jeecg-boot-module-custom` 或按业务拆分为 `jeecg-module-reservation` 等）。
2. **引入核心依赖**: 在该新模块的 `pom.xml` 中，声明对基石模块的依赖：
   - 依赖 `jeecg-boot-base-core` 以获取公共封装（Redis配置、分布式锁组件、拦截器、安全注解等）。
   - 如需使用到通用系统字典、用户、角色等数据查询服务，引入 `jeecg-system-api` 用于在微服务或服务内部实现调用接口互通。

### 4.2 业务代码迁移与组件对齐
现有项目各组件职责需要重新适配 Jeecg-Boot 的体系：
- **业务领域代码 (Controller/Service/Mapper)**: 将原 `demospringboot` 项目中的非公共业务模块（`reservation`, `course`, `task`）彻底平移至新建模块的相应 package 下。建议借此机会将数据层整体改造对齐 MyBatis-Plus 的 `BaseMapper` 和 `IService`，降低单表数据操作 SQL 的手写复杂度。
- **系统及权限 (Auth/System)**: 废弃原始的鉴权框架与用户权限体系，完全剥离。改用 Jeecg-Boot 原生内置的基于 Shiro+JWT/Redis 授权认证功能，以及统一的权限菜单管理。业务逻辑中若需要提取当前登录用户，可通过 Jeecg 标准登录提取工具快速从上下文中获取。
- **公共组件包 (Common/Resource)**: 原项目的 `common` 层中的统一返回体、全局拦截器需逐步抛弃不用，应使用框架核心所提供的 `org.jeecg.common.api.vo.Result` 等通用类来进行业务重构，实现与框架的前后端接口规范统一。

### 4.3 充分利用代码生成器优势
对于后台强管理属性或仅涉及到基础数据增删改查的简单类业务：
- 推荐直接在 Jeecg 的 UI 页面中通过 `Online 表单开发` 功能可视化快速建表，无需再手动书写前后端代码。
- 然后利用强大的代码生成器直接产出全套代码，并将其放进你的自定义模块（如 `jeecg-boot-module-custom`）中生效即可，大幅降低开发成本。
- 只有对于那些高度复杂的自定义业务流（例如原系统涉及多线程异步操作调度、涉及 Redis 并发控制的 `BookingServiceImpl` 逻辑处理），再纯手工按 Spring Boot Service 逻辑形式保留在模块内深度开发精细化维护。

### 4.4 编译启动与打包部署汇入
为了让大系统识别到你独立拆分的自定义模块的业务代码，需要配置相应的应用挂载：
1. **单体应用起停集成**: 寻找到主启动入口模块 `jeecg-module-system/jeecg-system-start` ，修改其内的 `pom.xml`，将其作为强依赖将其引入 `<artifactId>jeecg-boot-module-custom</artifactId>`。这使得在打包启动时，Spring Boot 会自动扫描到包含在你模块内的 Controller API 与 Service 并注入暴露出去。
2. **微服务架构下扩展**: 项目如果按微服务群组集群部署，则参考现有的 `jeecg-demo-cloud-start` 例子，进入微服务集群启动层目录 `jeecg-server-cloud/` 下新建立一个属于这个业务域的服务启动工程即可。
