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

## 5. 当前项目完成度分析及后续详细计划

根据对现有 `demospringboot` 项目结构的扫描分析（包括包含了 `auth`, `common`, `course`, `reservation`, `resource`, `task` 的完整结构），当前项目的完成度与后续向 JeecgBoot 迁移的详细计划如下：

### 5.1 当前项目完成度分析：中后期高完成度
当前的纯 Spring Boot 版本应用在独立运行的视角下已经处于 **中后期状态**，核心闭环业务与辅助架构已相对成熟：
1. **基础骨架与领域建模已成型**: 拥有完整的基于 Controller-Service-Mapper 的分层架构，囊括了课程(`course`)、资产/资源(`resource`)、角色与用户(`auth`)、在线预约与审批(`reservation`)以及内部任务调度(`task`)等五大领域。
2. **多角色与鉴权体系存在**: `auth` 目录下不仅具备了 User, Teacher, Student, Manager 多个用户实体表象，也具有诸如 Excel 导入导出监听 (`UserImportListener`) 的基础周边保障设施。
3. **高阶并发特性已落地**: 在最核心的 `reservation` 模块中，为了防止超卖与重复预约，已经通过自行引入 `RedissonConfig` 实现了基于 Redis 的分布式锁机制（主要存在于 `BookingServiceImpl`）；并自行设计了定时清理脏数据的闭环逻辑（`BookingCleanUpTask`）。
4. **面临的整合冗余痛点**: 然而，目前的进度中包含的大量“造轮子”代码（例如手动维护跨域 `CorsConfig`、`GlobalExceptionHandler` 全局异常处理器以及自造的 `Result` 通用返回体）。在迈入成熟的 JeecgBoot 体系时，这些都将成为需要首批剔除的冗余点。

### 5.2 接下来要做什么：迁移实施与完善详细计划 (Roadmap)

若要将现阶段成果完美且无痛地过渡至大型框架 Jeecg-Boot 以享受其后期的低代码运维红利，请依照此线路图进行：

#### 阶段一：环境准备与包袱剥离 (预估：1-2天)
- **目标**: 构建 Jeecg 平台框架本体，精简当前项目的老旧基建。
- **具体行动**:
  1. 下载启动 Jeecg-Boot 官方工程，根据其 `db/` 下的 SQL 初始化数据库与 Redis 支撑环境，保证官方原生模块在本地先顺利 Run 起来。
  2. 开刀“做减法”：在原 `demospringboot` 源码内，**果断删除**自定义的 `common` 包中的跨域配置、统一异常拦截器、各类 Base 分页配置和内部 Result 实体；将其在编译层直接映射调用 Jeecg 的工具包。

#### 阶段二：身份模型降维与数据字典接入 (预估：2-3天)
- **目标**: 将自研的割裂账号体系，统一收归至 Jeecg 的高维账号体系中。
- **具体行动**:
  1. **放弃独立用户表**: 停止使用原本隔离的 `Manager`, `Teacher`, `Student` 用户表进行登录。
  2. **依托内置组件**: 依靠 Jeecg 控制台中的“机构/部门管理”与“角色配置”，创建管理员、教师组、学生组。将原有业务表中关联的用户字段，直接用 Jeecg 的 `sys_user` 里的系统关联 ID 替代。
  3. **数据字典平移**: 将原有代码里枚举类 (`ResourceStatus`, `ResourceType`, `BookStatus`) 的状态定义，录入至 Jeecg 平台的 **"数据字典"** 菜单中，提升后期运维对各种状态的灵活更改性。

#### 阶段三：简单业务模块的低代码推翻重来 (预估：2天)
- **目标**: 利用框架最大红利，用零代码形式重新生成诸如课程、资料的普通功能模块。
- **具体行动**:
  1. 放弃去强行合并原本 `course` 与 `resource` 现存的普通 CRUD 代码。
  2. 直接利用 Jeecg 管理台的 **"Online 表单开发"** ，依据你的原有表结构通过拖拽建表。
  3. 一键点击生成前后端代码，直接将其放入新搭建好的业务模块目录 `jeecg-boot-module-custom` 里。这样可以直接产出带有列表分页、高级查询、表单验证的完善 Vue 页面与原生接口，比直接搬运原有纯净版 SpringBoot 代码来得高阶得多。

#### 阶段四：核心防阻塞业务 (Reservation) 重构注入 (预估：3-4天)
- **目标**: 确保具备高价值的分布式锁预约和审批流安全稳健地嵌入新结构。
- **具体行动**:
  1. 手工平移：将 `reservation` 目录下极具业务价值的排队抢位防超卖接口（如含 `Redisson` 处理的部分）与 `ApprovalController` 一字不落地迁移至自定义模块中。
  2. **替换身份提取逻辑**: 原本通过 `X-Token` 或 `LoginDto` 获取操作人的代码部分，统一换作调用 Jeecg 极简框架提供的 `LoginUtils.getLoginUser()` 或由 Shiro Context 进行上下文用户解析。
  3. **定时任务上云挂载**: 把原属于 `@Scheduled` 驱动的 `BookingCleanUpTask` 与 `demoTask`代码块去除注解，只留核心逻辑。然后在 Jeecg 后台 **"系统管理 -> 定时任务"** 配置 Quartz 调度频次并托管这部分任务执行端点，实现可视化任务启停。

#### 阶段五：前后端体系联调测试验证 (预估：3-5天)
- **目标**: 针对业务主脉络进行闭环测试。
- **具体行动**:
  1. 在 Jeecg-Vue3 的前端工程里为上述生成的和手写的接口配置动态侧边菜单与页面按钮权限。
  2. 测试完整流：以某个 "Student" 测试账号登录 -> 浏览生成的 `course` 列表 -> 发起抢购点击（触发 `BookingServiceImpl` 锁处理） -> 审核流转，验证框架对这部分复杂业务流的支持是否有异常中断。
  3. 发布打包：采用单体构建方案 `jeecg-system-start` 配置好依赖后打成 fat-jar 并部署上线。
