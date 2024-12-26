import {merge, ternary} from "../../util/util";
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
        // placeholder: `请输入${config.label}`
    }
}
export default {
    query: (config, type) => {
        const {'default-val': defaultVal, ...validProps} = config.props;
        const {activeValue, inactiveValue, activeText, inactiveText} = validProps;
        let val = defaultQueryConfig.val;
        if (type === 'quick') {
            val = ternary(defaultVal === inactiveValue || defaultVal === activeValue, defaultVal, val);
        }
        const options = [
            {label: inactiveText, value: inactiveValue},
            {label: activeText, value: activeValue}
        ]
        config.val = val;
        config.props = {
            ...validProps,
            options: options
        }
        return merge(config, defaultQueryConfig, true, false)
    },
    edit: (config, type) => {
        const {'default-val': defaultVal, ...validProps} = config.props
        const {activeValue, inactiveValue, activeText, inactiveText} = validProps
        const options = [
            {label: inactiveText, value: inactiveValue},
            {label: activeText, value: activeValue}
        ]
        config.val = ternary(defaultVal === inactiveValue || defaultVal === activeValue, defaultVal, inactiveValue);
        config.props = {
            ...validProps,
            options: options
        };
        return merge(config, defaultEditConfig, true, false);
    }

}