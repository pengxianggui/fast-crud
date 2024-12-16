export default {
    inject: ['openDynamicFilterForm'],
    props: {
        prop: String,
        label: String,
        filter: {
            type: Boolean,
            default: () => true
        },
        showOverflowToolTip: {
            type: Boolean,
            default: () => true
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