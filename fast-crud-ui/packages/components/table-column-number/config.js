import {easyOptParse, isUndefined, merge, ternary} from "../../util/util";
import {Opt} from "../../model";

const defaultQueryConfig = {
    component: 'el-input',
    opt: Opt.LIKE,
    val: null, // 默认值
    props: {
        clearable: true,
        // placeholder: `请输入${config.label}`
    },
    condMapFn: (cond) => {
        const operators = {
            '>=': Opt.GE,
            '<=': Opt.LE,
            '!=': Opt.NE,
            '=': Opt.EQ,
            '>': Opt.GT,
            '<': Opt.LT
        }
        easyOptParse(cond, operators);
        return [cond]
    }
}
const defaultEditConfig = {
    component: 'el-input-number',
    val: undefined,
    props: {
        clearable: true,
        'controls-position': "right",
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
        return merge(config, defaultQueryConfig, true, false)
    },
    edit: (config, type) => {
        const {label, props} = config;
        const {'default-val': defaultVal, rules = [], ...validProps} = props;
        // 如果含有值不为false的required属性, 则将其转换为rules规则添加到props中
        if (validProps.hasOwnProperty('required') && validProps.required !== false) {
            rules.push({type: 'number', required: true, message: `${label}不能为空`})
        }
        config.val = ternary(isUndefined(defaultVal), defaultEditConfig.val, defaultVal);
        config.props = {
            ...validProps,
            rules: rules
        }
        return merge(config, defaultEditConfig, true, false);
    }

}