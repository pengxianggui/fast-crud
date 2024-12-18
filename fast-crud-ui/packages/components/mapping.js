import {Cond, FilterComponentConfig, Opt} from "../model";
import {defaultIfEmpty, easyOptParse, isArray, isEmpty, isFunction, isString, merge, ternary} from "../util/util.js";

// TODO 支持:
//  1. query的默认值支持在table-column上定义
//  2. query组件是否独占一行, 支持在table-column上定义
//  3. query选项型组件(如select、checkbox-group)支持在table-column上定义禁用选项
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
        }
    },
    'fast-table-column-date-picker': {
        // 保证当前静态props中优先级更高的配置, 不被自定义覆盖
        highOptimizeProp: (type) => {
            return ['type']
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
        edit: (config) => {
            return {
                component: 'el-date-picker',
                opt: Opt.BTW,
                val: null, // 默认值
                props: {
                    type: "date",
                    clearable: true,
                    'value-format': 'yyyy-MM-dd'
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
                    placeholder: `请输入${config.label}`
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
                    placeholder: `请输入${config.label}`
                }
            }
        }
    },
    'fast-table-column-select': {
        query: (config, type) => {
            const {props = {}} = config;
            let val = []
            let component = 'fast-checkbox-group'; // TODO quick模式一定用checkbox-group吗? 如果是动态的业务数据呢, 会导致超多选项？
            if (type === 'easy') {
                component = 'fast-select';
                props.multiple = true;
                props.clearable = true;
            } else if (type === 'quick') {
                const {'default-val': defaultVal} = props;
                val = ternary(isArray(defaultVal) && !isEmpty(defaultVal), defaultVal, val);
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
        }
    },
    'fast-table-column-switch': {
        query: (config, type) => {
            const {props} = config;
            const {activeValue, inactiveValue, activeText, inactiveText} = props;
            let val = null;
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
                    type: 'textarea'
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
                    'value-format': 'HH:mm:ss'
                }
            }
        }
    }
    // TODO more
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
 * @param filterType 类型, 可选: quick, easy, dynamic
 */
export const buildFinalFilterComponentConfig = function (customConfig, tableColumnComponentName, filterType) {
    const defaultConfigFn = getConfigFn(tableColumnComponentName, 'query');
    if (!isFunction(defaultConfigFn)) {
        throw new Error(`未定义针对${tableColumnComponentName}的搜索控件`)
    }
    const {props: customProps, ...customConfigWithoutProps} = customConfig;
    const {props: defaultProps, ...defaultConfigWithoutProps} = defaultConfigFn(customConfig, filterType);

    const highOptimizePropFn = getConfigFn(tableColumnComponentName, 'highOptimizeProp');
    const finalProps = merge({...customProps}, defaultProps, false, false, (obj1, obj2, key, valueOfObj2) => {
        if (isFunction(highOptimizePropFn)) {
            const highOptimizeProps = highOptimizePropFn(filterType)
            if (highOptimizeProps.indexOf(key) > -1) {
                obj1[key] = valueOfObj2
            }
        }
    });

    const finalConfig = merge({...customConfigWithoutProps}, defaultConfigWithoutProps);
    finalConfig.props = finalProps;
    return new FilterComponentConfig(finalConfig); // 创建Filter对象
}