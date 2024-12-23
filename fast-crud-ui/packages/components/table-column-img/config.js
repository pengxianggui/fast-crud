import {easyOptParse, isEmpty, isString, merge, ternary} from "../../util/util";
import {Opt} from "../../model";

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
const defaultEditConfig = {
    component: 'el-upload',
    props: {
        class: 'fc-tighten',
        editable: true,
        defaultVal: null
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
    edit: (config, type) => {
        return merge(config, defaultEditConfig, true, false)
    }
}