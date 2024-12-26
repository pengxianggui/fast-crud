import {isArray, isUndefined, merge, ternary} from "../../util/util";
import {Opt} from "../../model";

const defaultQueryConfig = {
    component: 'fast-select',
    opt: Opt.IN,
    val: [], // 默认值
    props: {
        clearable: true,
        multiple: true,
    },
    condMapFn: (cond) => {
        if (isArray(cond.val) && cond.val.length > 0) {
            return [cond]
        }
        return []
    }
}
const defaultEditConfig = {
    component: 'fast-select',
    val: null,
    props: {
        clearable: true,
        class: 'fc-table-inline-edit-component',
        editable: true,
        rules: []
    }
}
export default {
    query: (config, type) => {
        const {'default-val': defaultVal, ...validProps} = config.props;
        let val = []
        let component = 'fast-select';

        if (type === 'quick') {
            val = ternary(isUndefined(defaultVal), defaultQueryConfig.val, defaultVal);
            if (validProps.hasOwnProperty('quick-filter-checkbox')) {
                component = 'fast-checkbox-group';
            }
        }
        config.val = val;
        config.component = component;
        config.props = validProps;
        return merge(config, defaultQueryConfig, true, false)
    },
    edit: (config, type) => {
        const {label, props} = config;
        const {'default-val': defaultVal, rules = [], ...validProps} = props;
        // 如果含有值不为false的required属性, 则将其转换为rules规则添加到props中
        if (validProps.hasOwnProperty('required') && validProps.required !== false) {
            rules.push({required: true, message: `${label}不能为空`})
        }
        config.val = ternary(isUndefined(defaultVal), defaultEditConfig.val, defaultVal);
        config.props = {
            ...validProps,
            rules: rules
        }
        return merge(config, defaultEditConfig, true, false);
    }
}