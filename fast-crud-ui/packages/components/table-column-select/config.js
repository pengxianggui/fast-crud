import {isArray, isEmpty, merge, ternary} from "../../util/util";
import {Opt} from "../../model";

const defaultQueryConfig = {
    component: 'fast-select',
    opt: Opt.IN,
    val: null, // 默认值
    props: {
        clearable: true,
        multiple: true,
    },
    condMapFn: (cond) => {
        if (isArray(cond.val) && cond.val.length > 0) {
            return [cond]
        }
        return []
    }
}
const defaultEditConfig = {
    component: 'fast-select',
    props: {
        clearable: true,
        class: 'fc-tighten',
        editable: true,
        defaultVal: null
    }
}
export default {
    query: (config, type) => {
        const {props = {}} = config;
        let val = []
        let component = 'fast-select';

        if (type === 'quick') {
            const {'default-val': defaultVal} = props;
            val = ternary(isArray(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
            if (props.hasOwnProperty('quick-filter-checkbox')) {
                component = 'fast-checkbox-group';
            }
        }
        config.val = val;
        config.component = component;
        return merge(config, defaultQueryConfig, true, false)
    },
    edit: (config, type) => {
        const {props: {multiple} = {}} = config;
        let defaultVal = null;
        if (multiple === true) {
            defaultVal = [];
        }
        config.props.defaultVal = defaultVal;
        return merge(config, defaultEditConfig, true, false)
    }
}