'use strict';

const path = require('path');
const CopyPlugin = require('copy-webpack-plugin');
const serverModule = path.resolve(__dirname, '..', '..', '..', 'server', 'uvl.glsp');

/**@type {import('webpack').Configuration}*/
const config = {
    target: 'node',

    entry: path.resolve(__dirname, 'src/uvl-extension.ts'),
    output: {
        path: path.resolve(__dirname, 'dist'),
        filename: 'uvl-extension.js',
        libraryTarget: 'commonjs2'
    },
    devtool: 'source-map',
    externals: {
        vscode: 'commonjs vscode'
    },
    mode: 'development',
    resolve: {
        extensions: ['.ts', '.js']
    },
    module: {
        rules: [
            {
                test: /\.ts$/,
                exclude: /node_modules/,
                use: [
                    {
                        loader: 'ts-loader'
                    }
                ]
            },
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            }
        ]
    },
    plugins: [
        new CopyPlugin({
            patterns: [
                {
                    from: path.resolve(__dirname, '..', 'webview', 'dist'),
                    to: path.resolve(__dirname, 'dist')
                },
                {
                    from: path.resolve(serverModule, 'target', 'uvl.glsp-0.0.1-glsp.jar'),
                    to: path.resolve(__dirname, 'dist', 'uvl-0.0.1-glsp.jar')
                }
            ]
        })
    ],
    ignoreWarnings: [/Can't resolve .* in '.*ws\/lib'/],
    performance: {
        hints: false
    }
};

module.exports = config;
