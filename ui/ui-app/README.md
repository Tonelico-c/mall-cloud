# ui-app 思途商城（用户端）

微服务电商项目的用户端购物应用，适配手机端，技术架构与代码风格和 `ui/ui-admin` 保持一致：
Vue 3（`<script setup>`）+ Vite + Element Plus + Pinia（持久化）+ Vue Router + Axios。

## 功能

- **登录**：用户名密码登录，token 持久化，路由守卫（未登录跳登录页，登录后跳回原页面）
- **首页**：商品搜索、一级分类宫格、新品推荐
- **分类**：左右两级分类布局，点击二级分类查看商品列表
- **商品列表**：两列宫格、触底自动加载下一页（IntersectionObserver 无限加载）
- **商品详情**：主图、价格、库存、数量选择、加入购物车（底部操作栏）
- **购物车**：勾选/全选、修改数量、删除、合计金额、去结算（底部结算栏 + TabBar 角标）
- **订单确认**：选择收货地址、商品清单、合计、提交订单
- **订单列表**：订单卡片、订单状态标签、订单商品明细、实付金额
- **个人中心**：个人信息（头像上传、资料编辑）、收货地址（增删改）、我的订单、退出登录

## 运行

```bash
npm install
npm run dev
```

- 开发端口 `5174`（避开 ui-admin 的 5173，两个前端可同时启动）
- `/api` 代理到网关 `http://localhost:9000`（启动网关及各微服务后接口即可用）
- 手机真机调试：手机与电脑连同一局域网，访问 `http://电脑IP:5174`

## 后端接口契约

返回格式统一为 `Result`：`{code: 1成功/0失败, msg, data}`。

### 已有接口（直接可用）

| 接口 | 说明 |
| --- | --- |
| `GET /category/tree` | 分类树（一级分类含 children 二级分类） |
| `GET /product?page&limit&name&categoryId` | 商品分页（按创建时间倒序） |
| `GET /product/{id}` | 商品详情（redis 缓存） |
| `POST /product/upload` | 文件上传（个人信息页头像上传使用） |

### 待实现接口（路径已按网关现有路由设计）

**user-service（`/user/**`，与 shipping 收货地址同属用户域）**

| 接口 | 说明 |
| --- | --- |
| `POST /user/login` | 登录，body `{name, password}`，返回 token（网关白名单已放行） |
| `GET /user/userInfo` | 当前登录用户信息（参考 `/admin/adminInfo`，从网关传递的登录信息取 id） |
| `PUT /user/update` | 修改个人信息，body 为 user 对象（name/email/phone/avatar） |
| `GET /user/shipping` | 当前用户收货地址列表 |
| `POST /user/shipping` | 新增收货地址 |
| `PUT /user/shipping/{id}` | 修改收货地址 |
| `DELETE /user/shipping/{id}` | 删除收货地址 |

**order-service（`/order/**`，购物车与订单同属订单域）**

| 接口 | 说明 |
| --- | --- |
| `GET /order/cart` | 当前用户购物车列表，每项 `{id, productId, count, selected, product: {...商品信息}}` |
| `POST /order/cart` | 加入购物车，body `{productId, count}` |
| `PUT /order/cart` | 修改数量/勾选状态，body `{id, count, selected}`（selected：1已勾选 0未勾选） |
| `DELETE /order/cart/{id}` | 删除购物车项 |
| `POST /order` | 根据购物车已勾选商品生成订单，body `{shippingId}`，生成 order + order_item |
| `GET /order` | 当前用户订单列表，每项含 `orderItemList`（订单商品） |

## 备注

- 网关 `AuthGlobalFilter` 对所有非白名单路径都校验 JWT，因此本应用和 ui-admin 一样，所有页面都需要登录后访问
- `order` 表的 `order_no` 是 bigint，订单号一般较长，建议后端序列化为字符串返回，避免 JS Number 精度丢失
- 数据库中部分商品的 `main_image` 带有换行等空白字符，前端已统一 trim（`src/utils/format.js`）
- 页面宽度限制为 480px 居中显示，电脑浏览器打开也是手机宽度效果
