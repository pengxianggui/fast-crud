import {fileURLToPath, URL} from 'node:url'

import path from 'path'
import {defineConfig} from 'vite'
import vue2 from '@vitejs/plugin-vue2'
import vue2Jsx from '@vitejs/plugin-vue2-jsx'
import Components from 'unplugin-vue-components/vite';
import {ElementUiResolver} from 'unplugin-vue-components/resolvers';

// https://vitejs.dev/config/
export default defineConfig(({mode}) => {
    const isLibrary = (mode === 'library')
    return {
        server: {
            host: 'localhost',
            port: 5173,
            proxy: {
                '/api': {
                    target: 'http://localhost:8080',
                    changeOrigin: true,
                    rewrite: (path) => path.replace(/^\/api/, '')
                }
            }
        },
        plugins: [
            vue2(), // vite为vue3而生, 配置此项支持vue2
            vue2Jsx(), // 支持vue2中使用jsx语法
            Components({
                resolvers: [ElementUiResolver()], // 使得无需手动引入element-ui组件，自动识别按需引入
            })
        ],
        resolve: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url)), // 配置别名,简化导入时路径书写
                // vue: 'vue/dist/vue.esm.js' // Vue 2.7 的完整 ESM 构建
            }
        },
        build: isLibrary ? {
            // build.lib 配置为库模式, 用以描述打包组件
            lib: {
                entry: path.resolve(__dirname, 'packages/index.js'), // 组件库入口
                name: 'FastCrudUI', // 全局访问库时的名称
                fileName: (format) => `fast-crud-ui.${format}.js`,
                formats: ['es', 'cjs', 'umd']  // 输出 ESM, CJS 和 UMD 格式
            },
            rollupOptions: {
                external: ['vue', 'element-ui'], // 避免打包 Vue 和 Element UI，使用 peerDependencies 来提供
                output: {
                    globals: {
                        vue: 'Vue',
                        'element-ui': 'ElementUI',
                    }
                }
            },
            outDir: 'lib', // 指定输出目录为 lib
        } : {
            outDir: mode === 'backend' ? '../demo/src/main/resources/static' : 'dist'
        },
        css: {
            preprocessorOptions: {
                scss: {
                    api: "modern-compiler",
                    // additionalData: `@use "sass:math";`, // 配置 Dart Sass 的现代写法
                    silenceDeprecations: ["legacy-js-api"] // 静默警告: Deprecation Warning: The legacy JS API is deprecated and will be removed in Dart Sass 2.0.0.
                }
            }
        }
    }
})
