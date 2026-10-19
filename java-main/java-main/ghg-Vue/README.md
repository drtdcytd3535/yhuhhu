# 仙逆 · 登录页（Vue 3 重构版）

将原始单文件 `ftyfu.html` 改造为标准 **Vue 3 + Vite** 项目。

## 目录结构

```
仙逆登录-Vue/
├── index.html              # 入口 HTML（含 Google 字体引入）
├── package.json            # 依赖与脚本
├── vite.config.js          # Vite 配置
└── src/
    ├── main.js             # 应用入口，挂载 App，引入全局样式
    ├── styles/global.css   # 全局变量、reset、body 背景
    ├── App.vue             # 页面骨架：粒子背景 + 远山雾气 + 品牌区 + 登录卡
    └── components/
        ├── StarField.vue   # 灵气粒子 Canvas 动画（自适应窗口、卸载清理）
        └── LoginCard.vue   # 登录表单 + 顶部提示 Toast（含「铭记此身」本地记忆）
```

## 功能对照

- 灵气粒子背景（Canvas 动画，随窗口自适应）
- 远山水墨 + 漂移雾气
- 国风品牌区（竖排印章 + 标题 + 副标题）
- 浮动标签的登录输入框、聚焦光晕
- 登录校验与顶部 Toast 提示
- 「铭记此身」勾选后把道号存入 `localStorage`，下次自动回填

## 本地运行

```bash
# 安装依赖
npm install

# 启动开发服务器（默认 http://localhost:5173，自动打开浏览器）
npm run dev

# 打包生产版本到 dist/
npm run build

# 预览生产构建
npm run preview
```
