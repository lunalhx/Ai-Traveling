# AI Traveling

数字文旅教育平台后端，基于“黑马点评”项目思路改造，定位为 Spring Boot 单体应用。项目面向 Java 后端实习面试展示，重点体现分层结构、统一返回、全局异常处理、Redis 缓存、LBS 推荐与限量抢购等后端能力。

## 技术栈

- Spring Boot 2.7.18
- MyBatis-Plus
- MySQL
- Redis
- RabbitMQ
- Lombok
- Validation
- Nginx、阿里云 OSS 作为部署和对象存储扩展方向

## 核心模块

- 用户与登录：手机号验证码登录，验证码和 token 存 Redis，用户资料存 MySQL。
- 景点浏览：热门景点详情为高频读场景，使用 Redis 缓存，并通过空值缓存、互斥锁、随机 TTL 处理缓存穿透、击穿、雪崩。
- LBS 推荐与打卡：景点经纬度存 MySQL 并同步 Redis GEO，用户上报位置后查询附近景点，并判断是否进入打卡范围。
- 限量周边抢购：Redis + Lua 进行库存校验、一人一单、预扣库存，RabbitMQ 异步下单削峰，MySQL 做最终落库和兜底。

## 分层结构

```text
com.example.travel
├── common
├── config
├── constant
├── controller
├── service
│   └── impl
├── mapper
├── entity
├── dto
├── vo
├── utils
└── interceptor
```

## 数据表规划

- `tb_user`：用户表
- `tb_spot_category`：景点分类表
- `tb_spot`：景点表
- `tb_checkin_rule`：打卡规则表
- `tb_checkin_record`：打卡记录表
- `tb_goods`：商品表
- `tb_seckill_activity`：抢购活动表
- `tb_goods_order`：订单表

## 启动方式

1. 创建 MySQL 数据库：`Ai_Travel`
2. 启动 MySQL、Redis、RabbitMQ
3. 默认启用 `dev` 环境，本地连接信息在 `src/main/resources/application-dev.yml`
4. 启动项目：

```bash
mvn spring-boot:run
```

指定生产环境启动：

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

也可以在运行 jar 时指定：

```bash
java -jar target/ai-traveling-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

健康检查接口：

```text
GET http://localhost:8080/api/health
```

## 当前工程状态

- 已配置 Maven 依赖：Web、MyBatis-Plus、MySQL、Redis、RabbitMQ、Lombok、Validation。
- 已配置 `application.yml`：端口、MySQL、Redis、RabbitMQ、MyBatis-Plus、日志。
- 已提供统一返回结果 `Result`。
- 已提供全局异常处理 `GlobalExceptionHandler`。
- 已初始化 Git 仓库。
