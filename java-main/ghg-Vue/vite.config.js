import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    // 必须显式绑定 IPv4 回环：默认 localhost 在本机只监听 [::1]，
    // 会导致 start.bat 打开的 http://127.0.0.1:5173 连接被拒绝
    host: '127.0.0.1',
    open: true,
    // 开发态代理：前端 /api/* 转发到本地 Spring Boot(8081)，避免跨域
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true
      }
    }
  }
})
