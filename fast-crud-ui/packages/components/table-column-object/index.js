import FastTableColumnObject from './src/table-column-object.vue'

FastTableColumnObject.install = (Vue) => {
    Vue.component(FastTableColumnObject.name, FastTableColumnObject)
}

export default FastTableColumnObject