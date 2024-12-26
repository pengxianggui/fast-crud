import {isUndefined, merge, ternary} from "../../util/util";
import {Cond, Opt} from "../../model";

const defaultQueryConfig = {
    component: 'el-date-picker',
    opt: Opt.BTW,
    val: [], // 默认值
    props: {
        type: `daterange`,
        clearable: true,
        'value-format': 'yyyy-MM-dd'
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
    component: 'el-date-picker',
    opt: Opt.BTW,
    val: null, // 默认值
    props: {
        type: "date",
        clearable: true,
        'value-format': 'yyyy-MM-dd',
        class: 'fc-table-inline-edit-component',
        editable: true,
        rules: []
    }
}

export default {
    query: (config, type) => {
        let val = defaultQueryConfig.val;
        const {'default-val': defaultVal, ...validProps} = config.props;
        const {type: dateType = 'date'} = validProps;
        if (type === 'quick') {
            val = ternary(isUndefined(defaultVal), val, defaultVal);
        }
        config.val = val;
        config.props = {
            ...validProps,
            type: `${dateType}range`
        }
        return merge(config, defaultQueryConfig, true, false)
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