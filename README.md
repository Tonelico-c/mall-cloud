# 分布式微服务电商商城系统（mall）

基于 Spring Cloud Alibaba 的微服务电商系统，覆盖「商品 → 购物车 → 订单 → 库存」全链路交易闭环，包含**管理后台**与**用户端**两个前端。

系统按业务域拆分为 8 个业务微服务 + 1 个 API 网关，服务间通过 OpenFeign 调用、Nacos 注册发现，客户端统一从网关进入，由网关完成 JWT 统一鉴权。

## 一、技术栈

| 分类 | 技术 |
| ---- | ---- |
| 基础框架 | Spring Boot 3.0.5、Java 21 |
| 微服务 | Spring Cloud 2022.0.2、Spring Cloud Alibaba 2022.0.0.0-RC2、Nacos（注册中心）、Spring Cloud Gateway（网关）、OpenFeign（服务调用） |
| 持久层 | MyBatis-Plus 3.5.5（分页插件、逻辑删除、字段自动填充）、MySQL 8.0.31、手写 XML 联表查询 |
| 缓存 | Spring Cache + Redis（商品详情缓存，TTL 1 小时） |
| 认证 | JWT（java-jwt 4.4.0）+ Redis 验证码 + BCrypt 密码哈希 |
| 其他 | 阿里云 OSS（文件上传）、kaptcha（图形验证码）、Lombok |
| 管理后台 | Vue 3 + Vite + Element Plus + Pinia + Axios（`ui/ui-admin`，端口 5173） |
| 用户端 | Vue 3 + Vite + Element Plus + Pinia + Axios（`ui/ui-app`，端口 5174） |

## 二、模块与端口

| 模块 | 服务名 | 端口 | 说明 |
| ---- | ------ | ---- | ---- |
| mall-gateway | gateway-service | 9000 | API 网关：路由转发、负载均衡、JWT 统一鉴权 |
| mall-admin | admin-service | 8081 | 管理员登录、管理员管理、操作日志 |
| mall-product | product-service | 8082 | 商品管理、库存扣减、商品详情缓存 |
| mall-category | category-service | 8083 | 商品分类、分类树 |
| mall-service | service-service | 8084 | 图形验证码、阿里云 OSS 文件上传 |
| mall-user | user-service | 8085 | 用户注册登录、个人信息 |
| mall-cart | cart-service | 8086 | 购物车增删改查 |
| mall-shipping | shipping-service | 8087 | 收货地址管理 |
| mall-order | order-service | 8088 | 订单与订单明细、下单主流程 |
| mall-common | — | — | 公共依赖：`Result` 统一返回、全局异常、JWT / 密码 / OSS 工具、登录上下文、操作日志切面、MyBatis-Plus 与 Redis 配置 |
| mall-api | — | — | 服务间调用契约：Feign 客户端接口（`ProductClient` / `CartClient` / `CategoryClient` / `AdminClient`）+ 跨服务 DTO / VO |

> 网关是唯一入口（9000）。前端不直接访问任何业务服务，所有请求经网关按路径前缀路由（`/product/**` → product-service，以此类推），并使用 `lb://服务名` 做客户端负载均衡。

## 三、功能模块

### 管理后台（`ui/ui-admin`）

- **管理员登录**：kaptcha 图形验证码 + BCrypt 密码校验 + 账号状态校验，登录签发 JWT
- **商品管理**：商品增删改查、分页、上下架状态、主图上传（OSS）
- **分类管理**：分类树展示与维护
- **管理员管理**：管理员列表、新增、编辑、重置密码、个人中心

### 用户端（`ui/ui-app`）

- **登录注册**：用户登录、登录态持久化（Pinia + localStorage）
- **首页与商品**：分类展示、商品列表、商品详情
- **购物车**：加入购物车（重复加购累加数量）、修改数量、勾选结算、删除
- **订单**：订单确认页（选择收货地址）、提交订单、我的订单列表
- **收货地址**：地址增删改查
- **个人中心**：个人信息、头像上传（OSS）

## 四、核心流程

### 1. 下单主流程（跨 3 个服务）

`mall-order` 的 `OrderServiceImpl#add`：

1. Feign 调用购物车服务，取出当前用户**已勾选**的购物车项（`CartClient#list`），过滤掉商品信息为空的脏数据；
2. 若没有勾选任何商品，抛 `ServiceException("购物车没有选中商品")`；
3. 遍历勾选项校验库存并累加订单应付金额（`payment`）；
4. 遍历调用商品服务的**扣减库存**接口（`ProductClient#decreaseStock`）——**先扣库存，再生成订单**；
5. 插入订单（订单号由 MyBatis-Plus 雪花算法生成并回填）与订单明细（冗余商品名称、主图、下单时单价）；
6. 清理已下单的购物车记录（`CartClient#deletedById`）。

> 顺序设计：先扣库存后生成订单。中途失败最坏结果是「少卖」（库存被扣但订单未生成，可补偿），而不是「超卖」。
>
> 注意：`@Transactional` 只覆盖本地数据库操作，**不覆盖 Feign 远程调用**（跨服务、不同连接、不同事务）。

### 2. 库存扣减防超卖

`ProductMapper#decreaseStock` 用**一条带条件的原子 UPDATE** 完成「判断 + 扣减」，而不是先查询再更新：

```xml
<update id="decreaseStock">
    UPDATE product SET stock = stock - #{count}
    WHERE id = #{id} AND stock >= #{count} AND deleted = 0
</update>
```

- 由数据库行锁保证并发原子性：库存不足时 `WHERE` 不成立，影响行数为 0；
- Mapper 返回 `int`（影响行数），Service 层判断为 0 时抛 `ServiceException("该商品库存不足")`；
- 扣减成功后通过 `@CacheEvict(value = "productCache", key = "#id")` 清除商品缓存，避免后续校验读到旧库存。

**跨服务调用的返回值陷阱**：下游服务抛出的异常会被 `GlobalExceptionHandler` 统一包装成 `Result(code=0)` 并返回 **HTTP 200**，Feign 因此不会抛异常。若不判断返回码，订单服务会误认为扣减成功而继续下单 → 超卖。因此 `OrderServiceImpl` 必须显式校验 `stockResult.getCode() != Result.OK`。

### 3. 网关统一鉴权

- 网关基于 WebFlux 响应式栈，使用 `GlobalFilter`（`AuthGlobalFilter`）而非 `HandlerInterceptor` 完成全局 JWT 校验；
- 白名单：`/service/captcha`、`/admin/login`、`/user/login`；其余请求必须在 `Authorization` 头携带有效 token（裸 token，无 `Bearer ` 前缀），校验失败直接返回 401；
- 校验通过后把用户信息写入请求头 `X-Login-Id` / `X-Login-Name` 转发给下游；
- 下游由 `LoginInterceptor` 取出并存入 `LoginContext`（ThreadLocal），业务代码零侵入获取当前登录用户，请求结束后在 `afterCompletion` 清理，避免线程复用串号；
- Feign 调用时由 `mall-api` 的 `DefaultFeignConfig` 注册 `RequestInterceptor`，把 `LoginContext` 中的用户信息继续透传给下游服务。

### 4. 缓存

- `ProductServiceImpl#selectById` 标注 `@Cacheable(value = "productCache", key = "#id")`，商品详情优先命中 Redis；
- 商品的更新、删除、库存扣减均标注 `@CacheEvict` 主动失效，保证缓存与数据库一致；
- 配置：`spring.cache.redis.time-to-live = 3600000`（1 小时）、`cache-null-values = true`（缓存空值，防缓存穿透）。

### 5. 操作日志

`@MyLog(module = "...")` 注解 + `OperLogAspect` 切面（AOP）自动记录：操作人、模块、请求 URI、请求参数（`password` 自动脱敏为 `*`）、客户端 IP、User-Agent、操作系统与浏览器、执行耗时、异常信息，统一落库 `oper_log` 表。切面内用 ThreadLocal 在 `@Before`、`@AfterReturning` / `@AfterThrowing` 之间传递日志对象。

## 五、架构约定

- **分层**：`controller` → `service/I*Service` + `impl` → `mapper`（超出 MyBatis-Plus Wrapper 能力的 SQL 写在 `src/main/resources/mapper/*.xml`）；POJO 按 `entity / dto / query / vo` 分目录。
- **统一返回**：`Result{code, msg, data}`，`code = 1` 成功 / `0` 失败；业务异常抛 `ServiceException`，由 `GlobalExceptionHandler`（`@RestControllerAdvice`）统一处理并返回友好提示，其余异常降级为「系统繁忙，请稍后重试」。
- **认证**：登录签发 JWT；网关校验并透传用户信息；密码统一使用 `PasswordUtil`（BCrypt，自带随机盐）哈希存储，登录用 `matches` 比对。
- **MyBatis-Plus**：逻辑删除字段 `deleted`（`@TableLogic`，1 已删除 / 0 未删除）；`create_time` / `update_time` 由 `MyMetaObjectHandler` 自动填充；分页使用 `PaginationInnerInterceptor` + `IPage`。
- **日期**：Jackson 全局格式 `yyyy-MM-dd HH:mm:ss`（GMT+8）；`WebConfig` 注册 `String → Date` 转换器。
- **日志**：各服务开启 `log-impl: org.apache.ibatis.logging.stdout.StdOutImpl`，控制台可直接看到执行的 SQL，便于调试。
- **前后端联调**：两个前端的 axios 实例 `baseURL` 均为 `/api`，`vite.config.js` 开发代理把 `/api` 重写后转发到网关 `http://localhost:9000`（如 `/api/product/1` → 网关 `/product/1`）。
- **数据库**：为简化部署，当前 8 个业务服务**共用同一个数据库 `mall`**（各服务连接串相同）。

## 六、快速启动

### 准备

- JDK 21、Maven、Node.js
- MySQL 8：创建数据库 `mall` 并导入表结构（见下方「数据库表」）
- Redis：默认 `127.0.0.1:6379`
- Nacos：默认 `127.0.0.1:8848`（需先启动，否则服务注册失败）

### 1. 修改配置

各服务 `src/main/resources/application.yml` 中的数据库账号密码、Redis 与 Nacos 地址按本机情况调整。

### 2. 启动后端

先启动 Nacos，再依次启动各服务（无严格先后顺序，网关最后启动也可以）：

```
GatewayApplication   # 9000
AdminApplication     # 8081
ProductApplication   # 8082
CategoryApplication  # 8083
ServiceApplication   # 8084
UserApplication      # 8085
CartApplication      # 8086
ShippingApplication  # 8087
OrderApplication     # 8088
```

### 3. 启动前端

```bash
cd ui/ui-admin && npm install && npm run dev   # 管理后台 http://localhost:5173
cd ui/ui-app   && npm install && npm run dev   # 用户端   http://localhost:5174
```

## 七、数据库表

数据库名 `mall`，核心表 9 张：

| 表 | 说明 |
| ---- | ---- |
| `admin` | 管理员 |
| `user` | 用户 |
| `product` | 商品（库存、价格、主图、上下架状态） |
| `category` | 商品分类 |
| `cart` | 购物车（用户 + 商品 + 数量 + 是否勾选） |
| `shipping` | 收货地址 |
| `orders` | 订单（订单号、金额、支付方式、状态、各时间节点） |
| `order_item` | 订单明细（冗余下单时的商品名称、图片、单价） |
| `oper_log` | 操作日志 |

## 八、项目结构

```
mall
├── mall-common          # 公共模块：Result / 全局异常 / 工具类 / 拦截器 / 日志切面 / 配置
├── mall-api             # Feign 接口 + 跨服务 DTO、VO
├── mall-gateway         # 网关：路由配置 + AuthGlobalFilter
├── mall-admin           # 8081 管理员
├── mall-product         # 8082 商品
├── mall-category        # 8083 分类
├── mall-service         # 8084 验证码 / 文件上传
├── mall-user            # 8085 用户
├── mall-cart            # 8086 购物车
├── mall-shipping        # 8087 收货地址
├── mall-order           # 8088 订单
└── ui
    ├── ui-admin         # 管理后台前端（Vue 3 + Element Plus，5173）
    └── ui-app           # 用户端前端（Vue 3 + Element Plus，5174）
```

## 九、TODO

- [ ] **接入 RabbitMQ 延迟队列，处理超时未支付订单**（重点待办）
  - 业务问题：下单即扣库存，若用户一直不付款，库存会被无效订单长期占用（少卖）
  - 方案（TTL + 死信队列）：订单创建成功后发送延迟消息（消息体为订单号，TTL 30 分钟）→ 到期成为死信并路由到死信队列 → 订单服务消费后**先查订单状态**，若仍未支付则关闭订单并调用商品服务**回补库存**；已支付 / 已取消则直接丢弃
  - 配套改动：商品服务新增与扣减库存对称的 `increaseStock` 接口（`UPDATE product SET stock = stock + #{count} WHERE id = #{id}`）并清除商品缓存
  - 可靠性要求：消费端手动 ACK；以订单状态做**幂等**判断，避免重复消费导致库存被重复回补；消息发送失败需有兜底记录
- [ ] 库存扣减接口补充参数校验（`count <= 0` 时应拒绝，避免传入负数导致库存反向增加）
- [ ] `ProductMapper#decreaseStock` 为参数补 `@Param("id")` / `@Param("count")`（当前依赖编译参数名，显式标注更稳妥）
- [ ] 订单模块补充：订单详情、取消订单、支付回调等后续状态流转（当前仅支持下单与列表查询）
- [ ] 管理后台补充订单管理页面（当前仅商品、分类、管理员管理）
- [ ] 清理网关冗余路由：`upload-service`（`/upload` → admin-service）已无使用方，实际上传接口为 `POST /service/upload`（经 `service-service` 路由）
- [ ] 分布式事务：当前采用「原子 SQL 扣减 + 失败少卖 + 后续回补」保证不超卖；若需强一致可引入 Seata AT 模式（依赖管理已锁定 `1.7.0-native-rc2`，需部署 TC 并在库中建 `undo_log` 表）
