import {isArray, isEmpty, merge, ternary} from "../../util/util";
import {Cond, Opt} from "../../model";

const defaultQueryConfig = {
    component: 'el-date-picker',
    opt: Opt.BTW,
    val: [], // 默认值
    props: {
        type: `daterange`,
        clearable: true,
        'value-format': 'yyyy-MM-dd'
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
    component: 'el-date-picker',
    opt: Opt.BTW,
    val: null, // 默认值
    props: {
        type: "date",
        clearable: true,
        'value-format': 'yyyy-MM-dd',
        class: 'fc-tighten',
        editable: true,
        defaultVal: null
    }
}
export default {
    query: (config, type) => {
        let val = [];
        const {props = {}} = config;
        const {type: propType = 'date'} = props;
        if (type === 'quick') {
            const {'default-val': defaultVal} = props;
            val = ternary(isArray(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
        }
        config.val = val;
        config.props.type = `${propType}range`;
        return merge(config, defaultQueryConfig, true, false)
    },
    edit: (config, type) => {
        return merge(config, defaultEditConfig, true, false)
    }
}