const webpack = require('webpack');

const CURRENT_API_URL = "http://localhost:8080";

module.exports = {
    publicPath: './',
    assetsDir: 'static',
    productionSourceMap: false,
    configureWebpack: {
        plugins: [
            new webpack.ProvidePlugin({
                $: "jquery",
                jQuery: "jquery",
                "windows.jQuery": "jquery"
            })
        ]
    },
    devServer: {
        port: 10090,
        proxy: {
            '/api': {
                target: CURRENT_API_URL,
                changeOrigin: true,
                pathRewrite: {
                    '/api': ''
                }
            }
        }
    }
};