import FastTableHeadCell from './src/table-head-cell.vue'

FastTableHeadCell.install = (Vue) => {
    Vue.component(FastTableHeadCell.name, FastTableHeadCell)
}

export default FastTableHeadCell