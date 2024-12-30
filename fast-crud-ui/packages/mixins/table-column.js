import {isBoolean} from "../util/util";

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
        /**
         * 是否展示编辑模式
         * @param status 表格状态
         * @param config 列配置
         * @param column element原生列配置
         * @returns {boolean}
         */
        canEdit(status, config, column) {
            if (status === 'normal') {
                return false;
            }
            const {property} = column;
            const {editable} = config[property];
            if (isBoolean(editable)) {
                return editable;
            }
            if (status === 'insert') {
                return editable === 'insert';
            }
            if (status === 'update') {
                return editable === 'update';
            }
            return false;
        },
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
            const {eventMethods: {valid} = {}, props} = config[property]
            if (valid) {
                valid(val, ref, props);
            }
        },
        handleFocus(event, scope) {
            this.$emit('focus', event, scope)
        },
        handleBlur(event, scope) {
            this.$emit('blur', event, scope)
        },
        handleInput(val, scope) {
            this.$emit('input', val, scope);
        },
        handleClear(scope) {
            this.$emit('input', scope);
        }
    }
}