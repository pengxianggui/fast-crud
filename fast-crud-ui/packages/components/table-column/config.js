import {easyOptParse, isUndefined, merge, ternary} from "../../util/util";
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
        class: 'fc-table-inline-edit-component',
        editable: false // fast-table-column不允许编辑
    }
}
export default {
    query: (config, type) => {
        let val = defaultQueryConfig.val;
        const {'default-val': defaultVal, ...validProps} = config.props;
        if (type === 'quick') {
            val = (isUndefined(defaultVal), val, defaultVal);
        }
        config.val = val;
        config.props = validProps;
        return merge(config, defaultQueryConfig, true, false)
    },
    edit: (config, type) => {
        const {'default-val': defaultVal, ...validProps} = config.props;
        config.val = ternary(isUndefined(defaultVal), defaultEditConfig.val, defaultVal);
        config.props = validProps;
        return merge(config, defaultEditConfig, true, false)
    }
}