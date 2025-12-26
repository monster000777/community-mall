import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'
import prettier from 'eslint-plugin-prettier'
import prettierConfig from 'eslint-config-prettier'

export default [
    // 基础推荐配置
    js.configs.recommended,

    // Vue3 推荐配置
    ...pluginVue.configs['flat/recommended'],

    // Prettier 配置
    prettierConfig,

    {
        // 忽略文件配置（替代 .eslintignore）
        ignores: [
            'node_modules/**',
            'dist/**',
            '*.local',
            '*.config.js',
            '*.config.cjs',
            '.vscode/**',
            '.idea/**',
            '*.log'
        ]
    },

    {
        // 全局配置
        files: ['**/*.{js,mjs,cjs,vue,jsx}'],

        languageOptions: {
            ecmaVersion: 'latest',
            sourceType: 'module',
            globals: {
                // 浏览器环境 - DOM
                window: 'readonly',
                document: 'readonly',
                navigator: 'readonly',
                console: 'readonly',

                // 浏览器环境 - Storage
                localStorage: 'readonly',
                sessionStorage: 'readonly',

                // 浏览器环境 - Timers
                setTimeout: 'readonly',
                clearTimeout: 'readonly',
                setInterval: 'readonly',
                clearInterval: 'readonly',

                // 浏览器环境 - Web APIs
                FormData: 'readonly',
                XMLHttpRequest: 'readonly',
                fetch: 'readonly',
                URL: 'readonly',
                URLSearchParams: 'readonly',
                Blob: 'readonly',
                File: 'readonly',
                FileReader: 'readonly',

                // 浏览器环境 - Speech API
                SpeechSynthesisUtterance: 'readonly',
                speechSynthesis: 'readonly',

                // Node.js 环境
                process: 'readonly',
                __dirname: 'readonly',
                __filename: 'readonly',
                module: 'readonly',
                require: 'readonly',

                // Vue 3 编译器宏
                defineProps: 'readonly',
                defineEmits: 'readonly',
                defineExpose: 'readonly',
                withDefaults: 'readonly'
            }
        },

        plugins: {
            vue: pluginVue,
            prettier: prettier
        },

        rules: {
            // Vue 相关规则
            'vue/multi-word-component-names': 'off',
            'vue/no-v-html': 'warn',
            'vue/require-default-prop': 'off',
            'vue/require-prop-types': 'warn',
            'vue/no-unused-vars': 'warn',
            'vue/component-name-in-template-casing': ['error', 'PascalCase', {
                registeredComponentsOnly: false
            }],

            // JavaScript 规则
            'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
            'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',
            'no-unused-vars': ['warn', {
                argsIgnorePattern: '^_',
                varsIgnorePattern: '^_'
            }],
            'no-undef': 'error',
            'prefer-const': 'warn',
            'no-var': 'error',

            // Prettier 规则
            'prettier/prettier': ['error', {
                endOfLine: 'auto'
            }]
        }
    }
]
