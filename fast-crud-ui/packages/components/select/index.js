import FastSelect from './src/fast-select.vue'

FastSelect.install = (Vue) => {
    Vue.component(FastSelect.name, FastSelect)
}

export default FastSelect