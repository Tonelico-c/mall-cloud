import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    //监听所有网卡，手机连同一局域网后可通过 http://电脑IP:5174 真机调试
    host: true,
    //端口避开 ui-admin 默认的 5173，两个前端可同时启动
    port: 5174,
    proxy: {
      '/api': { //请求路径中包含了/api
        target: 'http://localhost:9000', //要更换的源，也就是后台网关的源
        changeOrigin: true, //要不要更换源
        rewrite: (path) => path.replace(/^\/api/, '') //路径重写/api替换为''
      }
    }
  }
})
