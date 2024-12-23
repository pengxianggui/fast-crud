import {isArray, isEmpty, merge, ternary} from "../../util/util";
import {Cond, Opt} from "../../model";
import {colValid} from "../table/src/util";

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
const defaultEditConfig = (config) => {
    const {props, label} = config;
    const {rules = []} = props;
    // 如果含有值不为false的required属性, 则将其转换为rules规则添加到props中
    if (props.hasOwnProperty('required') && props.required !== false) {
        rules.push({required: true, message: `${label}不能为空`})
    }
    return {
        component: 'el-time-picker',
        props: {
            clearable: true,
            'value-format': 'HH:mm:ss',
            class: 'fc-table-inline-edit-component',
            editable: true,
            defaultVal: null,
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
        let val = [];
        const {props = {}} = config;
        if (type === 'quick') {
            const {'default-val': defaultVal} = props;
            val = ternary(isArray(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
        }
        config.val = val;
        return merge(config, defaultQueryConfig, true, false);
    },
    edit: (config, type) => {
        return merge(config, defaultEditConfig(config), true, false)
    }
}