import {isArray, isEmpty, merge, ternary} from "../../util/util";
import {Cond, Opt} from "../../model";

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
const defaultEditConfig = {
    component: 'el-time-picker',
    props: {
        clearable: true,
        'value-format': 'HH:mm:ss',
        class: 'fc-tighten',
        editable: true,
        defaultVal: null
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
        return merge(config, defaultEditConfig, true, false)
    }
}