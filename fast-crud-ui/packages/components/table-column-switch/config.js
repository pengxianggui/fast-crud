import {isUndefined, merge, ternary} from "../../util/util";
import {Opt} from "../../model";

const defaultQueryConfig = {
    component: 'fast-select',
    opt: Opt.EQ,
    val: null, // 默认值
    props: {
        clearable: true,
        options: [],
        // placeholder: `请输入${config.label}`
    }
}
const defaultEditConfig = {
    component: 'el-switch',
    val: null,
    props: {
        clearable: true,
        options: [],
        class: 'fc-table-inline-edit-component',
        editable: true,
        defaultVal: null
        // placeholder: `请输入${config.label}`
    }
}
export default {
    query: (config, type) => {
        const {props} = config;
        const {activeValue, inactiveValue, activeText, inactiveText} = props;
        let val = '';
        if (type === 'quick') {
            const {'default-val': defaultVal} = props;
            val = ternary(defaultVal === inactiveValue || defaultVal === activeValue, defaultVal, val);
        }
        const options = [
            {label: inactiveText, value: inactiveValue},
            {label: activeText, value: activeValue}
        ]
        config.val = val;
        config.props.options = options;
        return merge(config, defaultQueryConfig, true, false)
    },
    edit: (config, type) => {
        const {props: {activeValue, inactiveValue, activeText, inactiveText, 'default-val': defaultVal}} = config
        const options = [
            {label: inactiveText, value: inactiveValue},
            {label: activeText, value: activeValue}
        ]
        config.val = ternary(isUndefined(defaultVal), inactiveValue, defaultVal);
        config.props.options = options;
        return merge(config, defaultEditConfig, true, false)
    }

}