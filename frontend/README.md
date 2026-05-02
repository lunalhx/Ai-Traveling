# AI Traveling Frontend

前端服务目录。该目录与后端 `src/` 同级，专门存放前端工程、页面、接口封装、静态资源和构建配置。

## 目录边界

- 后端服务：根目录 `src/`、`pom.xml`、`src/main/resources/`
- 前端服务：根目录 `frontend/`
- 前端代码不要放入 `src/main/resources/static`、`src/main/java` 或后端的 `service/controller` 包中。

## 建议结构

```text
frontend
├── public
└── src
    ├── api
    ├── assets
    ├── components
    ├── config
    ├── layouts
    ├── pages
    ├── router
    ├── services
    ├── stores
    ├── styles
    ├── types
    │   ├── dto
    │   └── vo
    └── utils
```

## 分层约定

- `api`：封装 HTTP 请求，按后端控制器或业务域拆分。
- `services`：处理前端业务编排，避免页面组件直接堆业务逻辑。
- `types/dto`：维护请求参数类型，对齐后端 `dto`。
- `types/vo`：维护接口返回视图类型，对齐后端 `vo`。
- `config`：维护接口基础地址、环境变量读取、功能开关等配置。
- `pages`、`components`、`layouts`：分别管理页面、可复用组件和布局。

后续确定 Vue、React 或其他框架后，在本目录内初始化前端工程即可。
