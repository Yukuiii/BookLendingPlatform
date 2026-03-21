import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const proxyTarget = (
    env.VITE_PROXY_TARGET || "http://127.0.0.1:8080"
  ).replace(/\/$/, "");
  const devPort = Number(env.VITE_DEV_PORT || 5174);

  return {
    plugins: [vue()],
    server: {
      host: "0.0.0.0",
      port: Number.isNaN(devPort) ? 5173 : devPort,
      allowedHosts: true,
      proxy: {
        "/api": {
          target: proxyTarget,
          changeOrigin: true,
          secure: false,
          rewrite: (path) => path.replace(/^\/api/, ""),
        },
      },
    },
  };
});
