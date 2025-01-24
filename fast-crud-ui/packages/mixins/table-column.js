import {defaultIfEmpty, isBoolean, isFunction} from "../util/util";
import {colEditable} from "../components/table/src/util";

export default {
    inject: ['openDynamicFilterForm', 'tableStyle', 'context'],
    props: {
        prop: String,
        label: String,
        filter: {
            type: Boolean,
            default: () => true
        },
        firstFilter: {
            type: Boolean,
            default: () => false
        },
        showOverflowTooltip: {
            type: Boolean,
            default: () => false
        },
        quickFilterBlock: {
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
         * @param fatRow
         * @param column element原生列配置
         * @param $index 当前行索引
         * @returns {boolean}
         */
        canEdit(fatRow, column, $index) {
            return colEditable.call(defaultIfEmpty(this.context, this), fatRow, column.property);
        },
        showLabel(fatRow, column) {
            const {row, editRow, status} = fatRow;
            const {property} = column;
            return status === 'normal' ? row[property] : editRow[property];
        },
        headCellClick(column) {
            if (this.filter) {
                this.openDynamicFilterForm(this.columnProp)
            }
        },
        // change事件上抛并触发验证
        handleChange(val, scope) {
            this.$emit('change', val, scope);
            const {column, $index, row: {config}} = scope;
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