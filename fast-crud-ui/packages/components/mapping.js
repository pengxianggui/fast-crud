import {Cond, Opt} from "../model";
import {easyOptParse} from "../util/util.js";

const MAPPING = {
    'fast-table-column': {
        query: (config) => {
            const {props} = config
            // TODO props过滤: 限定查询时只支持的props属性, 以及必要的属性名转换
            return {
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
        }
    },
    'fast-table-column-date-picker': {
        query: (config) => {
            return {
                component: 'el-date-picker',
                opt: Opt.BTW,
                val: [], // 默认值
                props: {
                    type: "daterange",
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
        // TODO
    },
    'fast-table-column-input': {
        query: (config) => {
            return {
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
        query: (config) => {
            return {
                component: 'el-input',
                opt: Opt.LIKE,
                val: '', // 默认值
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
        query: (filter) => {
            return {
                component: 'fast-select', // TODO 由于必须对内封装el-option, 所以 这里不能使用el-select， 必须封装一个组件
                opt: Opt.EQ,
                val: null, // 默认值
                props: {
                    clearable: true,
                    placeholder: `请选择${filter.label}`
                },
                condMapFn: (cond) => {
                    const {props: {multiple}} = filter
                    if (multiple) { // 多选
                        return new Cond(cond.col, Opt.IN, cond.val)
                    }
                    return [cond]
                }
            }
        }
    },
    'fast-table-column-switch': {
        query: (config) => {
            const {props: {activeValue, inactiveValue, activeText, inactiveText}} = config
            const options = [
                {label: inactiveText, value: inactiveValue},
                {label: activeText, value: activeValue}
            ]
            return {
                component: 'fast-select',
                opt: Opt.EQ,
                val: null, // 默认值
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
        query: (config) => {
            return {
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
        query: (config) => {
            return {
                component: 'el-time-picker',
                opt: Opt.BTW,
                // 默认值
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
        throw new Error(`未定义针对${tableColumnComponentName}的快速搜索控件`)
    }

    return MAPPING[tableColumnComponentName][type]
}