/// <reference types="vitest/config" />
import { defineConfig } from 'vite';
import preact from '@preact/preset-vite';
import { paraglideVitePlugin } from '@inlang/paraglide-js';
import { VitePWA } from 'vite-plugin-pwa';

export default defineConfig({
  plugins: [
    preact(),
    paraglideVitePlugin({
      project: './project.inlang',
      outdir: './src/paraglide',
      // Remembered choice first, then the browser language, then English.
      strategy: ['localStorage', 'preferredLanguage', 'baseLocale'],
    }),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.svg'],
      manifest: {
        name: 'Mr Right',
        short_name: 'Mr Right',
        description: 'Find Mr Right among hundreds of bachelors – a logic elimination puzzle.',
        theme_color: '#6b2d5c',
        background_color: '#fbf6ee',
        display: 'standalone',
        icons: [{ src: 'favicon.svg', sizes: 'any', type: 'image/svg+xml', purpose: 'any' }],
      },
      workbox: {
        // Client-side routes work offline; API calls are never answered by the service worker.
        navigateFallback: '/index.html',
        navigateFallbackDenylist: [/^\/api\//],
      },
    }),
  ],
  server: {
    // 5174, so it can run next to avalons_final_stand (5173). strictPort: the e2e tests expect it.
    port: 5174,
    strictPort: true,
    // Same-origin relative /api URLs in dev (proxied) and production (served by Quarkus).
    proxy: {
      '/api': 'http://localhost:4720',
    },
  },
  test: {
    environment: 'jsdom',
  },
});
