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
| 登录方式 | 账号密码登录，返回内存 token；后续请求带 `X-Token` 头 |

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

## 测试账号与初始数据

数据库是文件库，首次启动后需要写入初始数据（表结构自动建好）。打开 `http://localhost:8080/h2-console`（JDBC URL `jdbc:h2:file:./data/fund`，用户 `sa`，密码空），执行：

```sql
INSERT INTO project (id, name, total_amount, available_amount, frozen_amount, used_amount)
VALUES (1, '实验室经费', 1000.00, 1000.00, 0.00, 0.00);

INSERT INTO users (username, password, role, project_id) VALUES ('member1',  '123456', 'member',  1);
INSERT INTO users (username, password, role, project_id) VALUES ('teacher1', '123456', 'teacher', 1);
INSERT INTO users (username, password, role, project_id) VALUES ('finance1', '123456', 'finance', 1);
```

| 账号 | 密码 | 角色 | 能做什么 |
|---|---|---|---|
| `member1` | `123456` | 成员 | 新建申请、提交、取消自己的申请 |
| `teacher1` | `123456` | 老师 | 批准、拒绝同项目的申请（不能批自己的） |
| `finance1` | `123456` | 财务 | 结算同项目的已批准申请 |

密码是明文存储，仅用于本地演示，不能用于真实产品。

重置演示数据：停服务 → 删 `data/` 目录 → 重启 → 重新执行上面的 INSERT。

## 接口

除登录外，所有写接口都要带请求头 `X-Token`（登录接口返回的 token）。

| 方法 | 路径 | 需要登录 | 角色 | 说明 |
|---|---|---|---|---|
| POST | `/api/login` | 否 | — | 参数 `username`、`password`，返回 `{token, username, role}` |
| POST | `/api/logout` | 是 | — | 带 `X-Token`，作废该 token |
| GET | `/api/ping` | 否 | — | 连通性检查，返回 `pong` |
| GET | `/api/project` | 否 | — | 项目与三个金额 |
| GET | `/api/applications` | 否 | — | 申请列表 |
| POST | `/api/applications` | 是 | member | 新建草稿，参数 `purpose`、`amount` |
| POST | `/api/applications/{id}/submit` | 是 | member | 提交自己的草稿，冻结经费 |
| POST | `/api/applications/{id}/approve` | 是 | teacher | 批准他人申请，参数 `amount` |
| POST | `/api/applications/{id}/reject` | 是 | teacher | 拒绝他人申请，参数 `reason` |
| POST | `/api/applications/{id}/cancel` | 是 | member | 取消自己的申请，参数 `reason` |
| POST | `/api/applications/{id}/settle` | 是 | finance | 结算，参数 `actual` |

示例：

```bash
# 登录，拿到 token
curl -X POST "http://localhost:8080/api/login?username=member1&password=123456"

# 用 token 新建草稿
curl -X POST "http://localhost:8080/api/applications?purpose=买试剂&amount=200" -H "X-Token: <token>"
```

## 权限校验顺序

每个写接口按这个顺序检查，命中就返回对应错误：

1. 有没有登录 → 401“请先登录”
2. 角色对不对 → 403（如“只有老师可以批准”）
3. 是不是自己的单 / 是不是别人的单 / 是不是同项目 → 403（“只能操作自己的申请”“不能审批自己的申请”“不能操作其他项目的申请”）
4. 状态对不对 → 400（如“只有待审批的申请可以批准”）
5. 金额规则 → 400（如“批准金额不能超过申请金额”“最多两位小数”）

## 业务规则

- 提交：`DRAFT → PENDING_L1`，可用减少、冻结增加；余额不足拒绝且不改数据。
- 批准：`PENDING_L1 → APPROVED`，`差额 = 申请金额 - 批准金额` 退回可用，冻结只留批准额。
- 拒绝：`PENDING_L1 → REJECTED`，释放全部冻结，必须填原因。
- 取消：`PENDING_L1` 或 `APPROVED → CANCELED`，释放剩余冻结（待审批释放申请额，已批准释放批准额），必须填原因。
- 结算：`APPROVED → SETTLED`，冻结清零、已使用加实际花费、剩余退回可用；允许零元，不得超过批准额。
- 每个动作都标了 `@Transactional`；金额一律用 `BigDecimal`，且限制最多两位小数。

## 当前边界

- token 存在内存里，重启服务后需要重新登录。
- 只支持一个项目，`projectId` 来自登录用户的 `project_id`。
- 密码明文存储，仅演示用。
- 列表没有分页、搜索。

## 构建与测试

```bash
cd backend
./mvnw clean package   # 编译并打包，产物 target/backend-0.0.1-SNAPSHOT.jar
./mvnw test            # 运行测试
```

## 验证记录

- 2026-11-04：`./mvnw spring-boot:run` 启动成功；`curl /api/ping` 返回 `pong`（200）；`/` 返回 404。
- 2026-11-05：H2 文件库建表成功；插入数据后重启服务，数据仍在。
- 2026-11-06：`/api/project`、`/api/applications` 返回数据库数据；提交/批准/结算接口经 curl 验证，`400/600 → 500/500 → ...` 数字正确，重复提交返回 400。
- 2026-11-08：按文档例子完整走通 `1000/0/0 → 400/600/0 → 500/500/0 → 540/0/460`。
- 2026-11-21：新建接口修复（`@GeneratedValue`）；拒绝、取消、小数校验、角色 403 验证通过。
- 2026-11-23：登录与归属校验上线——未登录 401、跨用户操作 403“只能操作自己的申请”、老师批自己的单 403“不能审批自己的申请”；正常流程仍走通到 `540/0/460`。

## 官方入口

- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/index.html)
- [Spring Initializr 工程生成器](https://start.spring.io/)
