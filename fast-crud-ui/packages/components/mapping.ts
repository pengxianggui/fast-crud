import {type ComponentConfig, Cond, Opt} from "../model";
import {easyOptParse} from "../util/util";

const MAPPING = {
    'fast-table-column': {
        query: (config: ComponentConfig) => {
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
                condMapFn: (cond: Cond) => {
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
        query: (config: ComponentConfig) => {
            return {
                component: 'el-date-picker',
                opt: Opt.BTW,
                val: null, // 默认值
                props: {
                    type: "daterange",
                    clearable: true,
                    'value-format': 'yyyy-MM-dd'
                },
                condMapFn: (cond: Cond) => {
                    const [start, end] = cond.val as [string, string]
                    const startCond = new Cond(cond.col, Opt.GE, start)
                    const endCond = new Cond(cond.col, Opt.LE, end)
                    return [startCond, endCond]
                }
            }
        },
        edit: (config: ComponentConfig) => {
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
        // edit: (filter: ComponentConfig) => {
        //
        // }
    },
    'fast-table-column-input': {
        query: (config: ComponentConfig) => {
            return {
                component: 'el-input',
                opt: Opt.LIKE,
                val: '', // 默认值
                props: {
                    clearable: true,
                    // placeholder: `请输入${config.label}`
                },
                condMapFn: (cond: Cond) => {
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
        edit: (config: ComponentConfig) => {
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
        query: (config: ComponentConfig) => {
            return {
                component: 'el-input',
                opt: Opt.LIKE,
                val: '', // 默认值
                props: {
                    clearable: true,
                    // placeholder: `请输入${config.label}`
                },
                condMapFn: (cond: Cond) => {
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
        edit: (config: ComponentConfig) => {
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
        query: (filter: ComponentConfig) => {
            return {
                component: 'el-select', // TODO 由于必须对内封装el-option, 所以 这里不能使用el-select， 必须封装一个组件
                opt: Opt.EQ,
                val: null, // 默认值
                props: {
                    clearable: true,
                    placeholder: `请选择${filter.label}`
                },
                condMapFn: (cond: Cond) => {
                    const {props: {multiple}} = filter
                    if (multiple) { // 多选
                        return new Cond(cond.col, Opt.IN, cond.val)
                    }
                    return [cond]
                }
            }
        }
    },
    // TODO 其它更多
}

export const getConfigFn = function (tableColumnComponentName: string, type: 'query' | 'edit') {
    if (!MAPPING.hasOwnProperty(tableColumnComponentName) || !MAPPING[tableColumnComponentName].hasOwnProperty(type)) {
        throw new Error(`未定义针对${tableColumnComponentName}的快速搜索控件`)
    }

    return MAPPING[tableColumnComponentName][type]
}