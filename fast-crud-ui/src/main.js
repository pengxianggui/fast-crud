import Vue from 'vue'
import ElementUI from 'element-ui'
import App from './App.vue'
import "element-ui/lib/theme-chalk/index.css";
import '@/assets/index.scss'
import FastCrudUI from '@/../packages/index.js'
import http from "@/http";

Vue.use(ElementUI)
Vue.use(FastCrudUI, {
    $http: http
})

new Vue({
    render: (h) => h(App)
}).$mount('#app')
