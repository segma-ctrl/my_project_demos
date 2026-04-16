import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'

// 1. 引入 ant-design-vue 库和全局重置样式
import Antd from 'ant-design-vue';
import 'ant-design-vue/dist/reset.css';

const app = createApp(App);

// 2. 注入 AntD 插件
app.use(Antd);
app.use(router);
app.mount('#app');
