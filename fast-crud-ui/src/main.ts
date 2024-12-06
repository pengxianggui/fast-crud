import Vue from 'vue'
import App from './App.vue'
import '@/assets/index.scss'
// import "element-ui/lib/theme-chalk/index.scss"; -- element组件样式不对放开这行试试

new Vue({
    render: (h) => h(App)
}).$mount('#app')
