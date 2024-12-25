import {easyOptParse, isEmpty, isString, merge, ternary} from "../../util/util";
import {Opt} from "../../model";

const defaultQueryConfig = {
    component: 'el-input',
    opt: Opt.LIKE,
    val: '', // 默认值
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
const defaultEditConfig = (config) => {
    const {props: {'default-val': defaultVal = null} = {}} = config;
    return {
        component: 'el-input',
        val: defaultVal,
        props: {
            clearable: false,
            class: 'fc-table-inline-edit-component',
            editable: false
        }
    }
}
export default {
    query: (config, type) => {
        let val = '';
        const {props} = config
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