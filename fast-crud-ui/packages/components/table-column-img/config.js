import {
    addStartWith,
    defaultIfBlank,
    defaultIfEmpty,
    easyOptParse,
    isEmpty,
    isString,
    merge,
    ternary
} from "../../util/util";
import FastTableOption, {Opt} from "../../model";

const defaultQueryConfig = {
    component: 'el-input',
    opt: Opt.LIKE,
    val: '', // 默认值
    props: {
        clearable: true
    },
    condMapFn: (cond) => {
        const operators = {
            '!=': Opt.NE,
            '=': Opt.GE,
            '~': Opt.NLIKE
        }
        easyOptParse(cond, operators)
        return [cond]
    }
}
const defaultEditConfig = (config) => {
    let {props: {multiple = false, 'default-val': defaultVal = null}} = config;
    if (multiple) {
        defaultVal = defaultIfEmpty(defaultVal, []);
    }
    return {
        component: 'fast-upload',
        val: defaultVal,
        props: {
            action: '/',
            'list-type': 'picture-card',
            class: 'fc-table-inline-edit-component',
            multiple: multiple, // 默认单选文件
            editable: true
        }
    }
}
export default {
    query: (config, type) => {
        let val = '';
        const {props} = config;
        if (type === 'quick') {
            const {'default-val': defaultVal} = props;
            val = ternary(isString(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
        }
        config.val = val;
        return merge(config, defaultQueryConfig, true, false)
    },
    edit: (config, type, tableOption) => {
        const finalConfig = merge(config, defaultEditConfig(config), true, false);
        finalConfig.props.action = addStartWith(tableOption.uploadUrl, '/');
        finalConfig.props['list-type'] = 'picture-card'; // 固定避免被自定义覆盖
        return finalConfig;
    }
}