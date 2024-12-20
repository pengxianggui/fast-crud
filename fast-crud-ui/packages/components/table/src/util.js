import {buildFinalComponentConfig} from "../../mapping";
import {deepClone, isArray, merge} from "../../../util/util";

/**
 * 将行数据转换为table-row格式
 * @param row
 * @param columnConfig
 * @returns {{editRow: *, row: *, status: string}}
 */
export function toTableRow(row, columnConfig, status = 'normal') {
    const getInlineItemConfig = (columnConfig) => {
        const config = {}
        try {
            const keys = Object.keys(columnConfig);
            for (let i = 0; i < keys.length; i++) {
                const col = keys[i];
                const {inlineItemConfig} = columnConfig[col]
                config[col] = inlineItemConfig
            }
        } catch (err) {
            console.error(err);
        }
        return config;
    }
    const config = getInlineItemConfig(columnConfig);
    if (status === 'insert') {
        // fill row
        const cols = Object.keys(config);
        const newRow = {};
        cols.forEach(col => {
            const {props: {defaultVal}} = config[col];
            newRow[col] = deepClone(defaultVal);
        })
        merge(row, newRow, true, false)
    }
    return {
        row: row,
        editRow: {...row},
        status: status,
        config: config
    }
}

/**
 * 构建组件配置
 * @param vnodes fast-table-column-* 组成的节点数组，蕴含fast-table-column-*上的信息
 * @param tableOption 表格配置
 * @param callback 针对每个table-column解析的回调函数
 */
export function iterBuildComponentConfig(vnodes, tableOption, callback) {
    const defaultProp = { // 通过option传入配置项, 需要作用到搜索或编辑等组件内部
        size: tableOption.style.size
    }

    for (const vnode of vnodes) {
        const {
            componentInstance: {
                $attrs = {},
                _props = {} // 默认属性
            },
            componentOptions: {
                tag: tableColumnComponentName,
                propsData = {}
            } = {} // 传入属性
        } = vnode

        const {filter = true} = {...$attrs, _props, ...propsData}
        const props = {...$attrs, ..._props, ...propsData}
        const param = {};

        const {label, prop: col} = props;
        const customConfig = {
            label: label,
            col: col,
            props: {...defaultProp, ...props}
        }
        // debugger
        try {
            if (filter) {
                buildFilterComponentConfig(param, tableColumnComponentName, customConfig);
            }
            buildEditComponentConfig(param, tableColumnComponentName, customConfig);
        } catch (err) {
            console.error(err)
        } finally {
            callback({tableColumnComponentName, col, customConfig, ...param})
        }
    }

}

/**
 * 构建过滤器组件配置
 * @param param
 * @prop tableColumnComponentName
 * @param defaultProp
 * @param props
 */
function buildFilterComponentConfig(param, tableColumnComponentName, customConfig) {
    const {'quick-filter': quickFilter = false} = customConfig.props;
    // build quick filters
    if (quickFilter) {
        try {
            param.quickFilter = buildFinalComponentConfig(customConfig, tableColumnComponentName, 'query', 'quick');
        } catch (e) {
            console.error(e.message)
        }
    }
    // build easy filters
    try {
        param.easyFilter = buildFinalComponentConfig(customConfig, tableColumnComponentName, 'query', 'easy');
    } catch (e) {
        console.error(e.message)
    }
    // build easy filters
    try {
        param.dynamicFilter = buildFinalComponentConfig(customConfig, tableColumnComponentName, 'query', 'dynamic')
    } catch (e) {
        console.error(e.message)
    }

}

/**
 * 构建编辑组件配置
 * @param param
 * @param tableColumnComponentName
 * @param defaultProp
 * @param props
 */
function buildEditComponentConfig(param, tableColumnComponentName, customConfig) {
    // form表单组件配置
    try {
        param.formItemConfig = buildFinalComponentConfig(customConfig, tableColumnComponentName, 'edit', 'form');
    } catch (e) {
        console.error(e.message)
    }
    // 行内表单组件配置
    try {
        param.inlineItemConfig = buildFinalComponentConfig(customConfig, tableColumnComponentName, 'edit', 'inline');
    } catch (e) {
        console.error(e.message)
    }
}

// 需要转义值的组件
const components = ['fast-checkbox-group', 'fast-select']

/**
 * 根据组件判断将值转义为显示值
 * @param component 组件名
 * @param val 待转义的原始值, 如果是数组，返回的label也是对应的数组
 * @param config 转义依据的配置
 */
export function escapeValToLabel(component, val, config) {
    if (components.indexOf(component) === -1) {
        return val;
    }

    const {options, valKey = 'value', labelKey = 'label'} = config
    const escape = function (val) {
        return val.map(v => {
            const option = options.find(o => o[valKey] === v)
            if (option) {
                return option[labelKey]
            }
            return v
        })
    }

    try {
        if (isArray(val)) {
            return escape(val)
        } else {
            const labels = escape([val])
            return labels[0]
        }
    } catch (err) {
        console.log(err)
    }

}