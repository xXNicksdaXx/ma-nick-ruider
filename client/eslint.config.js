const js = require('@eslint/js');
const tseslint = require('typescript-eslint');

module.exports = [
    {
        ignores: [
            '**/node_modules/**',
            '**/lib/**',
            '**/.eslintrc.js',
            '**/webpack.config.js',
            '**/webpack.prod.js',
            '**/dist/**',
            '**/build/**'
        ]
    },
    js.configs.recommended,
    ...tseslint.configs.recommended,
    {
        files: ['**/*.ts', '**/*.tsx'],
        rules: {}
    }
];
