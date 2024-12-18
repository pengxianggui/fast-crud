import Vue from 'vue'
import App from './App.vue'
import '@/assets/index.scss'
import FastCrudUI from '@/../packages/index.js'
import http from "@/http";

Vue.use(FastCrudUI, {
    $http: http
})

new Vue({
    render: (h) => h(App)
}).$mount('#app')
