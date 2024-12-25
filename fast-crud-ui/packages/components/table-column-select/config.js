import {defaultIfEmpty, isArray, isEmpty, merge, ternary} from "../../util/util";
import {Opt} from "../../model";
import {colValid} from "../table/src/util";

const defaultQueryConfig = {
    component: 'fast-select',
    opt: Opt.IN,
    val: null, // 默认值
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
const defaultEditConfig = (config) => {
    const {props, label} = config;
    let {multiple, rules = [], 'default-val': defaultVal = null} = props;
    // 如果含有值不为false的required属性, 则将其转换为rules规则添加到props中
    if (props.hasOwnProperty('required') && props.required !== false) {
        rules.push({required: true, message: `${label}不能为空`})
    }
    if (multiple) {
        defaultVal = ternary(isArray(defaultVal) && !isEmpty(defaultVal), defaultVal, []);
    }
    return {
        component: 'fast-select',
        val: defaultVal,
        props: {
            clearable: true,
            class: 'fc-table-inline-edit-component',
            editable: true,
            rules: rules
        },
        eventHandlers: {
            //  绑定一个change事件, 完成校验逻辑，如果校验不通过，则追加class: valid-error以便显示出来
            change: (val) => {
                colValid(val, config).catch(errors => {
                });
                return val
            }
        }
    }
}
export default {
    query: (config, type) => {
        const {props = {}} = config;
        let val = []
        let component = 'fast-select';

        if (type === 'quick') {
            const {'default-val': defaultVal} = props;
            val = ternary(isArray(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
            if (props.hasOwnProperty('quick-filter-checkbox')) {
                component = 'fast-checkbox-group';
            }
        }
        config.val = val;
        config.component = component;
        return merge(config, defaultQueryConfig, true, false)
    },
    edit: (config, type) => {
        return merge(config, defaultEditConfig(config), true, false)
    }
}