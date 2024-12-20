import {Cond, FilterComponentConfig, Opt} from "../model";
import {easyOptParse, isArray, isEmpty, isFunction, isString, merge, ternary} from "../util/util.js";

// TODO 1.0 支持:
//  4. props过滤: 限定查询时只支持的props属性, 以及必要的属性名转换
const MAPPING = {
    'fast-table-column': {
        query: (config, type) => {
            let val = '';
            const {props} = config
            if (type === 'quick') {
                const {'default-val': defaultVal} = props;
                val = ternary(isString(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
            }
            return {
                component: 'el-input',
                opt: Opt.LIKE,
                val: val, // 默认值
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
        },
        edit: (config, type) => {
            return {
                component: 'el-input',
                props: {
                    clearable: false,
                    class: 'fc-tighten',
                    editable: false
                }
            }
        }
    },
    'fast-table-column-date-picker': {
        // 保证当前静态props中优先级更高的配置, 不被自定义覆盖
        highOptimizeProp: (action, type) => {
            const fields = [];
            if (action === 'query') {
                fields.push('type');
            }
            return fields;
        },
        query: (config, type) => {
            let val = [];
            const {props = {}} = config;
            const {type: propType = 'date'} = props;
            if (type === 'quick') {
                const {'default-val': defaultVal} = props;
                val = ternary(isArray(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
            }
            return {
                component: 'el-date-picker',
                opt: Opt.BTW,
                val: val, // 默认值
                props: {
                    type: `${propType}range`,
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
        },
        edit: (config, type) => {
            return {
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
        }
    },
    'fast-table-column-img': {
        query: (config, type) => {
            let val = '';
            const {props} = config;
            if (type === 'quick') {
                const {'default-val': defaultVal} = props;
                val = ternary(isString(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
            }
            return {
                component: 'el-input',
                opt: Opt.LIKE,
                val: val, // 默认值
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
        },
        edit: (config, type) => {
            return {
                component: 'el-upload',
                props: {
                    class: 'fc-tighten',
                    editable: true,
                    defaultVal: null
                }
            }
        }
    },
    'fast-table-column-input': {
        query: (config, type) => {
            let val = '';
            const {props} = config;
            if (type === 'quick') {
                const {'default-val': defaultVal} = props;
                val = ternary(isString(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
            }
            return {
                component: 'el-input',
                opt: Opt.LIKE,
                val: val, // 默认值
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
        },
        edit: (config) => {
            return {
                component: 'el-input',
                props: {
                    clearable: true,
                    class: 'fc-tighten',
                    editable: true,
                    defaultVal: null
                }
            }
        }
    },
    'fast-table-column-number': {
        query: (config, type) => {
            let val = '';
            const {props} = config;
            if (type === 'quick') {
                const {'default-val': defaultVal} = props;
                val = ternary(isString(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
            }
            return {
                component: 'el-input',
                opt: Opt.LIKE,
                val: val, // 默认值
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
        },
        edit: (config) => {
            return {
                component: 'el-input-number',
                props: {
                    clearable: true,
                    'controls-position': "right",
                    class: 'fc-tighten',
                    editable: true,
                    defaultVal: null
                }
            }
        }
    },
    'fast-table-column-select': {
        query: (config, type) => {
            const {props = {}} = config;
            let val = []
            let component = 'fast-select';

            if (type === 'easy' || type === 'dynamic') {
                props.multiple = true;
                props.clearable = true;
            }
            if (type === 'quick') {
                props.multiple = true;
                props.clearable = true;
                const {'default-val': defaultVal} = props;
                val = ternary(isArray(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
                if (props.hasOwnProperty('quick-filter-checkbox')) {
                    component = 'fast-checkbox-group';
                }
            }
            return {
                component: component,
                opt: Opt.IN,
                val: val, // 默认值
                props: props,
                condMapFn: (cond) => {
                    if (isArray(cond.val) && cond.val.length > 0) {
                        return [cond]
                    }
                    return []
                }
            }
        },
        edit: (config, type) => {
            const {props: {multiple}} = config;
            let defaultVal = null;
            if (multiple === true) {
                defaultVal = [];
            }
            return {
                component: 'fast-select',
                props: {
                    clearable: true,
                    class: 'fc-tighten',
                    editable: true,
                    defaultVal: defaultVal
                }
            }
        }
    },
    'fast-table-column-switch': {
        query: (config, type) => {
            const {props} = config;
            const {activeValue, inactiveValue, activeText, inactiveText} = props;
            let val = '';
            if (type === 'quick') {
                const {'default-val': defaultVal} = props;
                val = ternary(defaultVal === inactiveValue || defaultVal === activeValue, defaultVal, val);
            }
            const options = [
                {label: inactiveText, value: inactiveValue},
                {label: activeText, value: activeValue}
            ]
            return {
                component: 'fast-select',
                opt: Opt.EQ,
                val: val, // 默认值
                props: {
                    clearable: true,
                    options: options,
                    // placeholder: `请输入${config.label}`
                }
            }
        },
        edit: (config) => {
            const {props: {activeValue, inactiveValue, activeText, inactiveText}} = config
            const options = [
                {label: inactiveText, value: inactiveValue},
                {label: activeText, value: activeValue}
            ]
            return {
                component: 'el-switch',
                props: {
                    clearable: true,
                    options: options,
                    class: 'fc-tighten',
                    editable: true,
                    defaultVal: inactiveValue
                    // placeholder: `请输入${config.label}`
                }
            }
        }
    },
    'fast-table-column-textarea': {
        query: (config, type) => {
            let val = '';
            const {props} = config
            if (type === 'quick') {
                const {'default-val': defaultVal} = props;
                val = ternary(isString(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
            }
            return {
                component: 'el-input',
                opt: Opt.LIKE,
                val: val, // 默认值
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
        },
        edit: (config) => {
            return {
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
        }
    },
    'fast-table-column-time-picker': {
        query: (config, type) => {
            let val = [];
            const {props = {}} = config;
            if (type === 'quick') {
                const {'default-val': defaultVal} = props;
                val = ternary(isArray(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
            }
            return {
                component: 'el-time-picker',
                opt: Opt.BTW,
                val: val,
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
        },
        edit: (config) => {
            return {
                component: 'el-time-picker',
                props: {
                    clearable: true,
                    'value-format': 'HH:mm:ss',
                    class: 'fc-tighten',
                    editable: true,
                    defaultVal: null
                }
            }
        }
    }
    // TODO 1.0 more
    //  1. FastTableColumnUpload: 替换FastTableColumnImg
    //  2. FastTableColumnRadio
}

export const getConfigFn = function (tableColumnComponentName, type) {
    if (!MAPPING.hasOwnProperty(tableColumnComponentName) || !MAPPING[tableColumnComponentName].hasOwnProperty(type)) {
        // console.error(`未定义针对${tableColumnComponentName}的快速搜索控件`)
        return null;
    }

    return MAPPING[tableColumnComponentName][type]
}

/**
 * 构建最终的过滤组件的配置
 * @param customConfig 用户自定义配置。方法内不会改变此值
 * @param tableColumnComponentName table-column组件名
 * @param action 行为: 可选: query, edit
 * @param type 类型, 当action为query时, 可选: quick, easy, dynamic; 当action为edit时, 可选: inline, form
 */
export const buildFinalComponentConfig = function (customConfig, tableColumnComponentName, action, type) {
    // 排除props中后缀为__e的属性, 因为这些配置项仅用于编辑控件, 并将__q后缀的属性名移除此后缀
    const filteredProps = Object.keys(customConfig.props).filter(key => !key.endsWith(action === 'query' ? '__e' : '__q'))
        .reduce((obj, key) => {
            obj[key.replace(action === 'query' ? /__q$/ : /__e$/, '')] = customConfig.props[key];
            return obj;
        }, {});
    const customFilterConfig = {
        label: customConfig.label,
        col: customConfig.col,
        props: {...filteredProps}
    }

    const defaultConfigFn = getConfigFn(tableColumnComponentName, action);
    if (!isFunction(defaultConfigFn)) {
        throw new Error(`未定义针对${tableColumnComponentName}的${action}控件`)
    }
    const {props: customProps, ...customConfigWithoutProps} = customFilterConfig;
    const {props: defaultProps, ...defaultConfigWithoutProps} = defaultConfigFn(customFilterConfig, type);

    const highOptimizePropFn = getConfigFn(tableColumnComponentName, 'highOptimizeProp');
    const finalProps = merge({...customProps}, defaultProps, false, false, (obj1, obj2, key, valueOfObj2) => {
        if (isFunction(highOptimizePropFn)) {
            const highOptimizeProps = highOptimizePropFn(action, type)
            if (highOptimizeProps.indexOf(key) > -1) {
                obj1[key] = valueOfObj2
            }
        }
    });

    const finalConfig = merge({...customConfigWithoutProps}, defaultConfigWithoutProps);
    finalConfig.props = finalProps;
    return new FilterComponentConfig(finalConfig); // 创建Filter对象
}