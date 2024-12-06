import FastTable from './src/table.vue'

FastTable.install = (Vue) => {
    Vue.component(FastTable.name, FastTable)
}

export default FastTable