import Vue from 'vue'
import App from './App.vue'
import '@/assets/index.scss'
import FastCrudUI from '@/../packages/index.js'
import axios from "axios";

Vue.use(FastCrudUI, {
    $http: axios.create({
        baseURL: '/api'
    })
})

new Vue({
    render: (h) => h(App)
}).$mount('#app')
