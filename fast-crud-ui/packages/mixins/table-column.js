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
        },
        // change事件上抛并触发验证
        handleChange(val, scope) {
            this.$emit('change', val, scope);
            const {column, $index, config} = scope;
            const {property} = column;
            const ref = this.$refs[property + $index];
            const {eventHandlers: {valid} = {}} = config[property]
            if (valid) {
                valid(val, ref);
            }
        }
    }
}