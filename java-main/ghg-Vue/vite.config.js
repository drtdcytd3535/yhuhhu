import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// ============================================================
// 本地局域网 IP：换网络 / 换机器时只改这一处即可
// 前端  http://192.168.68.26:5173
// 后端  http://192.168.68.26:8081
// ============================================================
const LAN_HOST = '192.168.68.26'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // 端口被占用时直接报错，不要偷偷换到 5174（start.bat 是按 5173 探测的）
    strictPort: true,
    // 0.0.0.0 = 监听全部网卡：
    //   局域网 ⇢ http://192.168.68.26:5173
    //   本机   ⇢ http://127.0.0.1:5173 （两个都通，老书签不用改）
    host: '0.0.0.0',
    open: true,
    // 从别的设备打开时，热更新的 websocket 也要连回这台机器
    hmr: { host: LAN_HOST },
    // 开发态代理：前端 /api/* 转发到 Spring Boot(8081)，避免跨域
    proxy: {
      '/api': {
        target: `http://${LAN_HOST}:8081`,
        changeOrigin: true
      }
    }
  },
  // 打包后本地预览同样走局域网地址
  preview: {
    port: 5173,
    host: '0.0.0.0'
  }
})
