export default {
    inject: ['openDynamicFilterForm'],
    props: {
        filter: {
            type: Boolean,
            default: () => true
        }
    },
    methods: {
        headCellClick(column) {
            if (this.filter) {
                this.openDynamicFilterForm(column)
            }
        }
    }
}