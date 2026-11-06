import react, { reactCompilerPreset } from '@vitejs/plugin-react';
import babel from '@rolldown/plugin-babel';
import { defineConfig } from 'vite';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  base: '/catalogo-app/',
  plugins: [react(), tailwindcss(), babel({ presets: [reactCompilerPreset()] })],
  build: {
    outDir: '../src/main/resources/static/catalogo-app',
    emptyOutDir: true,
    cssCodeSplit: false,
    rollupOptions: { output: { entryFileNames: 'assets/app.js', chunkFileNames: 'assets/[name].js', assetFileNames: 'assets/[name][extname]' } },
  },
});
