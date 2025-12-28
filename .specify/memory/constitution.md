# Op-Stack Gateway Constitution

## Core Principles

### I. DDD Architecture
项目采用领域驱动设计(DDD)多模块架构，模块职责清晰分离：
- bootstrap: 应用入口、配置
- application: 应用服务、DTO
- domain: 领域模型、领域服务
- infrastructure: 网关过滤器、HTTP客户端
- interface: 管理端点
- common: 共享常量、工具类

### II. Gateway-Only Responsibility
网关只负责请求路由转发，不实现业务逻辑。认证、授权等功能委托给后端服务处理。

### III. Configuration-Driven
路由配置、服务地址等通过配置文件管理，支持多环境配置（local、prod）。

## Port Configuration (NON-NEGOTIABLE)

### 开发环境端口规范
**开发环境(local profile)端口配置固定，禁止修改：**

| 服务 | 端口 | 说明 |
|------|------|------|
| op-stack-gateway | 8080 | API网关 |
| op-stack-service | 8081 | 主服务 |
| op-stack-executor | 8082 | 执行器服务 |
| op-stack-tools | 8083 | 工具服务 |
| op-stack-auth | 8084 | 认证服务 |

**注意**: 其他环境（如prod）端口配置不受此约束，可根据部署需求调整。

## Swagger Documentation

网关聚合所有后端服务的Swagger文档，通过代理路由避免CORS问题，并重写servers配置确保请求通过网关转发。

## Governance

- 宪法规则优先于其他实践
- 修改宪法需要充分的理由和文档记录
- 所有代码变更必须符合宪法规定

**Version**: 1.0.0 | **Ratified**: 2025-12-28 | **Last Amended**: 2025-12-28
