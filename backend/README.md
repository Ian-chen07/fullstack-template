# 后端工程

页面发出请求后，后端负责接收和处理，再把结果返回给前端。后端工程放在这里，使用 Spring Boot。项目流程和 Git 规范见[根 README](../README.md)。

## 当前工程

| 项目 | 实际值 |
|---|---|
| 语言 | Java 21（OpenJDK 21.0.6 LTS） |
| 框架 | Spring Boot 4.1.2 |
| 构建工具 | Maven Wrapper（`./mvnw`，本机无需另装 Maven） |
| 数据库 | H2 文件库 + Spring Data JPA，数据文件在 `data/fund.mv.db` |
| 监听端口 | 8080 |

目录约定：

- 源码：`src/main/java/backend/`，包名 `backend`
- 建表脚本：`src/main/resources/schema.sql`，启动时自动执行（`CREATE TABLE IF NOT EXISTS`）
- 配置：`src/main/resources/application.properties`
- 数据文件：`data/`，本地运行产生，不提交
- 构建产物：`target/`，不提交

注意 Spring Boot 4 的依赖名与 3.x 不同：Web 用 `spring-boot-starter-webmvc`，H2 控制台用 `spring-boot-h2console`。照抄 3.x 教程的 `pom.xml` 会解析失败。

另外 `spring.jpa.hibernate.ddl-auto=none`：表结构只由 `schema.sql` 管理，不让 Hibernate 自动建表，否则会冲掉已有数据。

## 启动与验证

命令都在 `backend/` 目录执行：

```bash
cd backend
chmod +x mvnw          # 首次执行一次
./mvnw spring-boot:run
```

启动成功后日志末尾出现：

```
Tomcat started on port 8080 (http)
Started BackendApplication in x.xxx seconds
```

验证（另开终端）：

```bash
curl http://localhost:8080/api/project
curl http://localhost:8080/api/applications
```

## 数据库控制台

启动后端后，浏览器打开 `http://localhost:8080/h2-console`，填：

- JDBC URL：`jdbc:h2:file:./data/fund`
- User Name：`sa`
- Password：留空

连上后可以查看、修改两张表，也能手工插入演示数据。

重置演示数据：停服务 → 删 `data/` 目录 → 重启（自动建表）→ 用控制台插入项目和申请。种子数据示例：

```sql
INSERT INTO project (id, name, total_amount, available_amount, frozen_amount, used_amount)
VALUES (1, '实验室经费', 1000.00, 1000.00, 0.00, 0.00);

INSERT INTO application (project_id, purpose, amount, status)
VALUES (1, '购买传感器', 600.00, 'DRAFT');
```

## 接口

角色通过请求头 `X-Role` 传入：`member`（成员）、`teacher`（老师）、`finance`（财务）。

| 方法 | 路径 | 允许角色 | 说明 |
|---|---|---|---|
| GET | `/api/ping` | 不限 | 连通性检查，返回 `pong` |
| GET | `/api/project` | 不限 | 项目与三个金额 |
| GET | `/api/applications` | 不限 | 申请列表 |
| POST | `/api/applications` | member | 新建草稿，参数 `purpose`、`amount` |
| POST | `/api/applications/{id}/submit` | member | 提交草稿，冻结经费 |
| POST | `/api/applications/{id}/approve` | teacher | 批准，参数 `amount`；差额退回可用 |
| POST | `/api/applications/{id}/reject` | teacher | 拒绝，参数 `reason`；释放冻结 |
| POST | `/api/applications/{id}/cancel` | member | 取消，参数 `reason`；释放剩余冻结 |
| POST | `/api/applications/{id}/settle` | finance | 结算，参数 `actual`；冻结转已使用 |

示例：

```bash
# 成员新建草稿
curl -X POST "http://localhost:8080/api/applications?purpose=买试剂&amount=200" -H "X-Role: member"

# 成员提交
curl -X POST http://localhost:8080/api/applications/1/submit -H "X-Role: member"

# 老师批准 500
curl -X POST "http://localhost:8080/api/applications/1/approve?amount=500" -H "X-Role: teacher"

# 财务结算 460
curl -X POST "http://localhost:8080/api/applications/1/settle?actual=460" -H "X-Role: finance"
```

角色不匹配返回 403 并附带原因，例如"只有老师可以批准"。

## 业务规则

- 提交：`DRAFT → PENDING_L1`，可用减少、冻结增加；余额不足拒绝且不改数据。
- 批准：`PENDING_L1 → APPROVED`，`差额 = 申请金额 - 批准金额` 退回可用，冻结只留批准额。
- 拒绝：`PENDING_L1 → REJECTED`，释放全部冻结，必须填原因。
- 取消：`PENDING_L1` 或 `APPROVED → CANCELED`，释放剩余冻结（待审批释放申请额，已批准释放批准额），必须填原因。
- 结算：`APPROVED → SETTLED`，冻结清零、已使用加实际花费、剩余退回可用；允许零元，不得超过批准额。
- 每个动作都标了 `@Transactional`；金额一律用 `BigDecimal`，且限制最多两位小数。

## 角色与权限的边界

- 已实现：按 `X-Role` 校验"这个角色能不能做这个动作"。
- **未实现**：登录与真实用户身份；因此"老师不能审批自己的申请""成员只能操作自己的申请"这两条**没有**做到——请求头里的角色是前端下拉框模拟的。

## 构建与测试

```bash
cd backend
./mvnw clean package   # 编译并打包，产物 target/backend-0.0.1-SNAPSHOT.jar
./mvnw test            # 运行测试
```

## 验证记录

- 2026-11-04：`./mvnw spring-boot:run` 启动成功；`curl /api/ping` 返回 `pong`（200）；`/` 返回 404。
- 2026-11-05：H2 文件库建表成功；插入项目与申请后重启服务，数据仍在。
- 2026-11-06：`/api/project`、`/api/applications` 返回数据库数据；提交/批准/结算接口经 curl 验证，`400/600 → 500/500 → ...` 数字正确，重复提交返回 400。
- 2026-11-08：按文档例子完整走通 `1000/0/0 → 400/600/0 → 500/500/0 → 540/0/460`。
- 2026-11-21：`POST /api/applications` 新建草稿（修复了 `id` 缺 `@GeneratedValue` 导致 500 的问题）；`X-Role: member` 调批准返回 403、`teacher` 返回 200；`settle` 用 `member` 返回 403、`finance` 返回 200；拒绝（缺原因 400、正常拒绝释放冻结）、取消（待审批与已批准两种情况都只释放剩余冻结）均验证通过；`approve?amount=5.001` 返回 400“批准金额最多两位小数”。

## 尚未完成

- 登录与用户归属校验（见"角色与权限的边界"）。
- 只支持一个项目（`projectId` 写死为 1），没有项目切换。
- 列表没有分页、搜索。
- 没有统一的接口文档（只有本文件的表格）。

## 版本以配置为准

JDK 版本见 `pom.xml` 的 `java.version`，Spring Boot 版本见 `spring-boot-starter-parent`。升级时同步更新本文件的表格。

## 官方入口

- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/index.html)
- [Spring Initializr 工程生成器](https://start.spring.io/)
