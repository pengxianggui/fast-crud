export default {
    inject: ['openDynamicFilterForm', 'tableStyle'],
    props: {
        prop: String,
        label: String,
        filter: {
            type: Boolean,
            default: () => true
        },
        showOverflowToolTip: {
            type: Boolean,
            default: () => false
        }
    },
    data() {
        return {
            columnProp: {
                ...this.$attrs,
                prop: this.prop,
                label: this.label,
                filter: this.filter,
                order: '' // '', 'asc', 'desc'
            }
        }
    },
    methods: {
        headCellClick(column) {
            if (this.filter) {
                this.openDynamicFilterForm(this.columnProp)
            }
        }
    }
}