import {easyOptParse, isEmpty, isString, merge, ternary} from "../../util/util";
import {Opt} from "../../model";

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
const defaultEditConfig = {
    component: 'el-input-number',
    props: {
        clearable: true,
        'controls-position': "right",
        class: 'fc-tighten',
        editable: true,
        defaultVal: null
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
        return merge(config, defaultEditConfig, true, false)
    }

}