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
            '=': Opt.GE,
            '~': Opt.NLIKE
        }
        easyOptParse(cond, operators)
        return [cond]
    }
}
const defaultEditConfig = {
    component: 'el-input',
    props: {
        type: 'textarea',
        rows: 1,
        class: 'fc-tighten',
        editable: true,
        defaultVal: ''
        // placeholder: `请输入${config.label}`
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
        return merge(config, defaultEditConfig, true, false)
    }

}