import FastTableColumn from './src/table-column.vue'

FastTableColumn.install = (Vue) => {
    Vue.component(FastTableColumn.name, FastTableColumn)
}

export default FastTableColumn