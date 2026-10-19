# ghg-Vue 项目速查手册（AI 助手必读）

> **给 AI 助手：请先读完本文件再动手，不要重复探索目录结构、不要重复确认端口/密码/启动方式。**
> 本文件是这个项目的"记忆"，机器每次重置后它依然在，用它替代重新摸索。
> 最后更新：2026-10-16（新增首页展示与分级分类搜索后端 + 首页接入真实接口）

---

## 0. 一句话概览

仙逆 / 国风主题的**登录注册 Demo**。前端 Vue 3 + Vite，后端 Spring Boot 3.4.5 + MyBatis-Plus 3.5.9 + MySQL 8.0。

## 1. 机器特性（最重要，先看这里）

- 这台电脑**每次开机都会重置 C 盘**：`node_modules`、`target`、`C:\Users\Administrator\.m2`、npm 缓存都会消失。
- **D 盘不会丢**，所有依赖缓存都放在 `D:\ghg-cache\`，开机后由 `start.bat` **自动恢复**。
- 所以：**不要重新联网下载依赖，先运行 `start.bat`**。恢复 node_modules 只需几秒（robocopy 快照），不需要 npm install。
- 绝对不要删除 `D:\ghg-cache\`，它不是缓存垃圾，是项目的依赖仓库。

## 2. 目录与端口

| 项目 | 路径 / 值 |
| --- | --- |
| 项目根 | `C:\Users\Administrator\Desktop\java-main` |
| 前端源码 | `ghg-Vue/src/`（`App.vue`、`components/LoginCard.vue`、`components/RegisterCard.vue`、`components/StarField.vue`、`components/ArtBackground.vue`、`components/HomePage.vue`） |
| 页面切换 | 未用 vue-router，`App.vue` 用 `logged` 状态在「登录/注册」与「首页」之间切换；登录成功由 `LoginCard` emit `login-success` |
| 前端调试 | 开发态访问 `http://127.0.0.1:5173/?preview=home` 直达首页、`?preview=register` 直达注册页 |
| 外链入口 | 首页导航「动漫」= `https://v.qq.com/x/cover/mzc00200aaogpgh.html`（腾讯视频《仙逆》动漫专辑页），`window.open` 新标签打开，常量在 `HomePage.vue` 的 `ANIME_URL` |
| 前端构建 | `ghg-Vue/vite.config.js`（`/api` 代理到 `http://127.0.0.1:8081`） |
| 后端工程 | `springBootTest/` |
| 后端端口 | **8081** |
| 前端开发端口 | **5173** |

后端接口（前缀 `/api`）：

认证（`AuthController`）：

- `GET  /api/captcha` — 取图形验证码，正确答案存服务端 session
- `POST /api/register` — 注册：校验验证码 + 密码强度（≥8 位且含数字和字母）→ BCrypt 加密落库
- `POST /api/login` — 登录：查库 + BCrypt 比对，成功写 session（前端登录页已接真实接口）

首页展示与检索（`HomeController` / `SearchController` / `ContentController`）：

- `GET /api/home` — 首页聚合：概览统计 + 分级分类树（含数量）+ 荐读 6 条 + 阅览榜 5 条 + 公告
- `GET /api/categories` — 分级分类树
- `GET /api/search` — 分级分类搜索。参数：`keyword`（命中标题/简介/标签/作者）、`type`（大类名=顶级分类名）、`categoryId`（传顶级 ID 连带检索全部子分类，优先于 type）、`page`/`size`（默认 1/12，上限 60）、`sort`（hot 热度 / new 最新 / view 查阅）
- `GET /api/search/hots` — 热搜词（按热度取内容标题）
- `GET /api/content/{id}` — 内容详情（含正文），命中后浏览量 +1
- 分页说明：本地仓库无 `mybatis-plus-jsqlparser`（3.5.9 起分页插件独立成模块），故 `/api/search` 手写 `LIMIT/OFFSET`，`MybatisPlusConfig` 里刻意没有注册分页插件

## 3. 数据库

- MySQL 8.0.39，服务名 `MySQL80`（开机自动启动，无需手动拉起）
- **账号 root / 密码 `123456`**（注意：不是 123456az，那是错的）
- 库名 `spring_boot_test`，共 4 张表（均由 `src/main/resources/sql/schema.sql` 自动建表 + 幂等种子数据）：
  - `t_user` — 用户（id / username / password / created_at）
  - `t_category` — 分级分类（id / name / **parent_id 自关联** / level / icon / description / sort_no；parent_id=0 为顶级；种子含 5 个顶级 + 15 个子分类）
  - `t_content` — 内容（id / title / summary / content 正文 / category_id / tags / author / hot / views / status；种子含 32 条）
  - `t_notice` — 宗门公告（id / content / date_label / sort_no；种子含 4 条）
  - 种子数据用 `INSERT IGNORE + 显式主键`，重复启动不会产生脏数据
- 客户端路径：`C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`
- **后端启动会自动建库建表**（JDBC 参数 `createDatabaseIfNotExist=true` + 执行 `src/main/resources/sql/schema.sql`），无需手动跑 SQL
- 手动初始化脚本：`springBootTest/sql/init.sql`（D 盘另有一份 `D:\ghg-Vue-backup\init.sql`）
- ⚠️ MySQL 的 data 目录在 C 盘，开机重置后**已注册的账号会丢失**；表和种子内容会自动重建，重新注册即可

## 4. 启动方式（用脚本，别手工拼命令）

| 脚本（项目根目录） | 作用 |
| --- | --- |
| `start.bat` | **一键启动**：自检环境 → 恢复依赖 → 起后端 → 等 8081 就绪 → 起前端 |
| `stop.bat` | 一键停止：按端口结束 5173 / 8081 进程并关闭窗口 |
| `save-cache.bat` | 依赖变化后，把 node_modules / target / maven 仓库快照存回 D 盘 |
| `run-backend.bat` | 只启动后端（独立可跑，也被 start.bat 调用） |
| `run-frontend.bat` | 只启动前端（独立可跑，也被 start.bat 调用） |
| `start-all.bat` | 旧入口，已改为转发到 `start.bat` |

- 所有脚本 **GBK 编码**（cmd 中文不乱码），项目内路径统一用 `%~dp0` 相对定位。
- 唯一硬编码的绝对路径是缓存盘 `set "CACHE=D:\ghg-cache"`；换电脑只改这一行。

## 5. D 盘持久缓存结构

```
D:\ghg-cache\
  m2repo\         maven 本地仓库（约 800MB，开机后直接复用，不重新下载）
  maven-home\     maven wrapper 目录（Maven 3.9.9 本体，靠 MAVEN_USER_HOME 指向）
  node_modules\   前端依赖快照（39MB，robocopy 秒级恢复）
  target\         后端编译产物快照（maven 增量编译近乎 0 耗时）
  npm-cache\      npm 包缓存
  settings.xml    maven 配置：本地仓库指向 D 盘 + 阿里云镜像（重置后由 start.bat 恢复到 ~/.m2）
D:\ghg-Vue-backup\
  init.sql        SQL 初始化脚本备份
```

## 6. AI 助手工作约定（避免无谓思考）

1. **先读本文件**，不要 `ls` 逐层扫目录，不要猜测端口/密码/库名。
2. 前端改动由 Vite HMR 热更新；后端改动由 devtools 自动重启，通常无需手动重启项目。
3. 启停项目一律用 `start.bat` / `stop.bat`。
4. 需要建表时依赖后端自动建表即可，不要手动执行 SQL（除非要改表结构）。
5. 不要删除或"清理" `D:\ghg-cache`、`D:\ghg-Vue-backup`。
6. 依赖有变化（改 `package.json` / `pom.xml`）后，运行一次 `save-cache.bat` 更新快照。
7. 输出编码约定：`.md` / `.vue` / `.java` 用 UTF-8；`.bat` / `.cmd` 必须 GBK。
8. 项目运行环境：JDK 21（Zulu，pom 目标版本 17）、Node 22.22.2 / npm 10.9.7。

## 7. 已知问题 / 可选优化

- **MySQL 数据目录在 C 盘** → 开机重置会丢注册用户。彻底解决需把 datadir 迁到 D 盘（要改 my.ini 并以管理员重启服务），暂未做。
- `dist/` 是旧构建产物，改源码后需 `npm run build` 才会更新；开发时看 5173 端口即可。
- `log/xx.log` 是后端运行日志，排查启动问题看它最直接。
