import {easyOptParse, isEmpty, isString, merge, ternary} from "../../util/util";
import {Opt} from "../../model";
import {colValid} from "../table/src/util";

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
const defaultEditConfig = (config) => {
    const {props, label} = config;
    const {rules = [], 'default-val': defaultVal = undefined} = props;
    // 如果含有值不为false的required属性, 则将其转换为rules规则添加到props中
    if (props.hasOwnProperty('required') && props.required !== false) {
        rules.push({type: 'number', required: true, message: `${label}不能为空`})
    }
    return {
        component: 'el-input-number',
        val: defaultVal,
        props: {
            clearable: true,
            'controls-position': "right",
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
        let val = null;
        const {props} = config;
        if (type === 'quick') {
            const {'default-val': defaultVal} = props;
            val = ternary(isString(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
        }
        config.val = val;
        return merge(config, defaultQueryConfig, true, false)
    },
    edit: (config, type) => {
        return merge(config, defaultEditConfig(config), true, false)
    }

}