import {isUndefined, merge, ternary} from "../../util/util";
import {Cond, Opt} from "../../model";

const defaultQueryConfig = {
    component: 'el-time-picker',
    opt: Opt.BTW,
    val: [],
    props: {
        clearable: true,
        'is-range': true,
        'value-format': 'HH:mm:ss'
    },
    condMapFn: (cond) => {
        const conds = []
        const [start, end] = cond.val
        if (start) {
            conds.push(new Cond(cond.col, Opt.GE, start))
        }
        if (end) {
            conds.push(new Cond(cond.col, Opt.LE, end))
        }
        return conds
    }
}
const defaultEditConfig = {
    component: 'el-time-picker',
    val: null,
    props: {
        clearable: true,
        'value-format': 'HH:mm:ss',
        class: 'fc-table-inline-edit-component',
        editable: true,
        rules: []
    }
}
export default {
    query: (config, type) => {
        let val = defaultQueryConfig.val;
        const {'default-val': defaultVal, ...validProps} = config.props;
        if (type === 'quick') {
            val = ternary(isUndefined(defaultVal), val, defaultVal);
        }
        config.val = val;
        config.props = validProps;
        return merge(config, defaultQueryConfig, true, false);
    },
    edit: (config, type) => {
        const {label, props: {'default-val': defaultVal, ...validProps}} = config;
        const {rules = []} = validProps;
        // 如果含有值不为false的required属性, 则将其转换为rules规则添加到props中
        if (validProps.hasOwnProperty('required') && validProps.required !== false) {
            rules.push({required: true, message: `${label}不能为空`})
        }
        config.val = ternary(isUndefined(defaultVal), defaultEditConfig.val, defaultVal);
        config.props = {
            ...validProps,
            rules: rules
        }
        return merge(config, defaultEditConfig, true, false)
    }
}