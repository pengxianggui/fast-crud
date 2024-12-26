import {easyOptParse, isUndefined, merge, ternary} from "../../util/util";
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
            '!=': Opt.NE,
            '=': Opt.EQ,
            '~': Opt.NLIKE
        }
        easyOptParse(cond, operators)
        return [cond]
    }
}
const defaultEditConfig = {
    component: 'el-input',
    val: null,
    props: {
        type: 'textarea',
        rows: 1,
        class: 'fc-table-inline-edit-component',
        editable: true,
        rules: []
        // placeholder: `请输入${config.label}`
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
            rules.push({required: true, message: `${label}不能为空`})
        }
        config.val = ternary(isUndefined(defaultVal), defaultEditConfig.val, defaultVal);
        config.props = {
            ...validProps,
            rules: rules
        }
        const finalConfig = merge(config, defaultEditConfig, true, false)
        finalConfig.eventHandlers = {
            //  绑定一个change事件, 完成校验逻辑，如果校验不通过，则追加class: valid-error以便显示出来
            change: (val) => {
                colValid(val, finalConfig).catch(errors => {
                });
                return val
            }
        }
        return finalConfig;
    }

}