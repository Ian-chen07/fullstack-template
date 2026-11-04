# 后端工程

页面发出请求后，后端负责接收和处理，再把结果返回给前端。后端工程放在这里，使用 Spring Boot。项目流程和 Git 规范见[根 README](../README.md)。

## 当前工程

| 项目 | 实际值 |
|---|---|
| 语言 | Java 21（OpenJDK 21.0.6 LTS） |
| 框架 | Spring Boot 4.1.2 |
| 构建工具 | Maven Wrapper（`./mvnw`，本机无需另装 Maven） |
| 数据库 | H2 + Spring Data JPA（依赖已引入，尚未使用） |
| 监听端口 | 8080 |

工程由 Spring Initializr 生成（Maven、Java 21，依赖 Spring Web / Spring Data JPA / H2），随后加入第一个接口。

目录约定：

- 源码：`src/main/java/backend/`，包名 `backend`
- 配置：`src/main/resources/application.properties`
- 测试：`src/test/java/`
- 构建产物：`target/`，已被 `.gitignore` 忽略，不提交

注意 Spring Boot 4 的依赖名与 3.x 不同：Web 用 `spring-boot-starter-webmvc`，H2 控制台用 `spring-boot-h2console`。照抄 3.x 教程的 `pom.xml` 会解析失败。

## 启动与验证

以下命令都在 `backend/` 目录执行：

```bash
cd backend
chmod +x mvnw          # 首次执行一次，给脚本执行权限
./mvnw spring-boot:run
```

首次运行会下载 Maven 和项目依赖，需要网络，耗时几分钟。启动成功后日志末尾出现：

```
Tomcat started on port 8080 (http)
Started BackendApplication in x.xxx seconds
```

另开一个终端验证接口：

```bash
curl -i http://localhost:8080/api/ping
```

预期结果：

```
HTTP/1.1 200
pong
```

访问 `http://localhost:8080/` 会返回 404 的 JSON 错误体，属正常现象：服务已启动，但没有 `/` 的路由。

停止服务：在运行服务的终端按 `Ctrl+C`。修改源码后必须重启才会生效（未引入热更新）。

## 构建与测试

```bash
cd backend
./mvnw clean package   # 编译并打包，产物在 target/
./mvnw test            # 运行测试
```

打包成功后会生成 `target/backend-0.0.1-SNAPSHOT.jar`，可用 `java -jar` 独立启动。

## 接口

| 方法 | 路径 | 说明 | 预期结果 |
|---|---|---|---|
| GET | `/api/ping` | 连通性检查 | `pong`，状态码 200 |

当前请求与响应都是纯文本，尚未接入数据库。

## 验证记录

- 2026-11-04：本机 `./mvnw spring-boot:run` 启动成功，日志出现 `Tomcat started on port 8080`；`curl /api/ping` 返回 `pong`（200）；访问 `/` 返回 404。

## 尚未完成

- 项目、成员、申请三张表与对应实体尚未建立。
- 提交、批准、结算等业务接口尚未实现，`Budget` 中的经费流转逻辑目前仍在 `学习记录/java/`。
- 权限校验、异常统一处理、前端联调均未开始。

## 版本以配置为准

JDK 版本见 `pom.xml` 的 `java.version`，Spring Boot 版本见 `spring-boot-starter-parent`。升级时同步更新本文件的表格。

## 官方入口

- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/index.html)
- [Spring Initializr 工程生成器](https://start.spring.io/)
