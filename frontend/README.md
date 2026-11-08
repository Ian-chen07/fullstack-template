# 前端工程

你在浏览器里看到的页面，以及点击、输入后的反应，都属于前端的工作。这里放的是本项目的 Vue 工程。项目流程和 Git 规范见[根 README](../README.md)。

## 当前工程

| 项目 | 实际值 |
|---|---|
| 框架 | Vue 3.5 |
| 语言 | JavaScript（未使用 TypeScript） |
| 构建工具 | Vite 8.3 |
| 开发端口 | 5173 |
| 后端地址 | `http://localhost:8080`（通过 Vite 代理转发） |

目录约定：

- `index.html`：页面入口，挂载 `src/main.js`
- `src/App.vue`：目前唯一的页面，显示经费数字与申请列表
- `src/main.js`：创建 Vue 应用
- `vite.config.js`：含开发代理配置
- `dist/`：构建产物，已被忽略
- `node_modules/`：依赖目录，已被忽略，不提交

## 启动与验证

命令都在 `frontend/` 目录执行：

```bash
cd frontend
npm install        # 首次执行，安装依赖
npm run dev        # 启动开发服务器
```

启动后终端出现 `Local: http://localhost:5173/`，浏览器打开该地址。

页面要能显示数据，**后端必须同时在跑**（`backend/` 下 `./mvnw spring-boot:run`，监听 8080）。前端的 `/api/...` 请求由 Vite 代理转发到 8080，浏览器不会遇到跨域问题。

验证页面真的取到了数据：

1. 页面顶部显示"可用 / 冻结 / 已使用 / 总预算"四个数字；
2. 浏览器按 `F12` 打开开发者工具，切到 `Network`，刷新页面，能看到 `api/project` 与 `api/applications` 两条请求，状态均为 `200`。

## 构建

```bash
cd frontend
npm run build      # 产物输出到 dist/
npm run preview    # 本地预览构建结果
```

## 当前功能

- 显示项目的可用、冻结、已使用、总预算。
- 列出申请单：编号、用途、申请金额、批准金额、实际金额、状态。
- 按状态显示操作：草稿可"提交"；待审批填金额后"批准"；已批准填金额后"结算"。
- 操作失败时，把后端返回的原因显示在页面上（例如"可用经费不足""只有草稿可以提交"）。

## 尚未完成

- 没有登录与角色区分，所有人的操作权限一致（后端也未校验身份）。
- 没有拒绝、取消申请的操作。
- 没有分页、搜索、样式美化，页面是最朴素的表格。
- 未做构建产物的部署验证。

## 官方入口

- [Vue 入门与工程创建](https://cn.vuejs.org/guide/quick-start.html)
- [Vite 官方说明](https://vite.dev/guide/)
