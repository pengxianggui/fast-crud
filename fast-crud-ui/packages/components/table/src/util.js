import {buildFinalComponentConfig} from "../../mapping";
import {deepClone, defaultIfBlank, defaultIfEmpty, isArray, isEmpty, isUndefined, merge} from "../../../util/util";

import Schema from 'async-validator'

/**
 * 单个col值校验, 校验失败会添加class: valid-error并reject(errors), 成功则会移除可能存在的valid-error，并resolve
 * @param val object数据
 * @param config col完整的config配置
 * @returns {Promise<unknown>} 若校验通过resolve, 否则reject(errors)
 */
export function colValid(val, config) {
    const {col, props} = config;
    return new Promise((resolve, reject) => {
        const validator = new Schema({
            [col]: defaultIfEmpty(props.rules, [])
        });
        validator.validate({[col]: val}, (errors, fields) => {
            if (isEmpty(errors)) {
                props.class = defaultIfBlank(props.class, '').replace('valid-error', '');
                resolve();
            } else {
                props.class = defaultIfBlank(props.class, '') + ' valid-error';
                reject(errors);
            }
        })
    })
}

/**
 * 多行(整行)校验, 校验失败会为每个col配置添加class: valid-error并reject(errors), 成功则会移除每个col可能存在的valid-error，并resolve。
 * @param fatRows 完整的行记录
 */
export function rowValid(fatRows) {
    const validPromises = [];
    for (let i = 0; i < fatRows.length; i++) {
        const {editRow, config} = fatRows[i];
        Object.keys(config).map(col => {
            validPromises.push(colValid(editRow[col], config[col]));
        });
    }
    return Promise.all(validPromises);
}

/**
 * 获取编辑配置
 * @param columnConfig 列配置
 * @param editType 编辑类型(inline, form)
 * @returns {{}}
 */
export const getEditConfig = function (columnConfig, editType) {
    const config = {}
    try {
        const keys = Object.keys(columnConfig);
        for (let i = 0; i < keys.length; i++) {
            const col = keys[i];
            const {inlineItemConfig, formItemConfig} = columnConfig[col]
            config[col] = (editType === 'form' ? deepClone(formItemConfig) : deepClone(inlineItemConfig))
        }
    } catch (err) {
        console.error(err);
    }
    return config;
}

/**
 * 将行数据转换为table-row格式
 * @param row
 * @param columnConfig
 * @param status 表格状态
 * @prop editType
 * @returns {{editRow: *, row: *, status: string}}
 */
export function toTableRow(row, columnConfig, status = 'normal', editType) {
    const config = getEditConfig(columnConfig, editType);
    if (status === 'insert') {
        // fill row
        const cols = Object.keys(config);
        const newRow = {};
        cols.forEach(col => {
            const {val} = config[col];
            newRow[col] = deepClone(val);
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
        if (isUndefined(col)) {
            continue;
        }
        const customConfig = {
            label: label,
            col: col,
            props: {...defaultProp, ...props}
        }
        try {
            if (filter) {
                buildFilterComponentConfig(param, tableColumnComponentName, customConfig);
            }
            buildEditComponentConfig(param, tableColumnComponentName, customConfig, tableOption);
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
            console.error(e)
        }
    }
    // build easy filters
    try {
        param.easyFilter = buildFinalComponentConfig(customConfig, tableColumnComponentName, 'query', 'easy');
    } catch (e) {
        console.error(e)
    }
    // build easy filters
    try {
        param.dynamicFilter = buildFinalComponentConfig(customConfig, tableColumnComponentName, 'query', 'dynamic')
    } catch (e) {
        console.error(e)
    }

}

/**
 * 构建编辑组件配置
 * @param param
 * @param tableColumnComponentName
 * @param defaultProp
 * @param props
 */
function buildEditComponentConfig(param, tableColumnComponentName, customConfig, tableOption) {
    // form表单组件配置
    try {
        param.formItemConfig = buildFinalComponentConfig(customConfig, tableColumnComponentName, 'edit', 'form', tableOption);
    } catch (e) {
        console.error(e)
    }
    // 行内表单组件配置
    try {
        param.inlineItemConfig = buildFinalComponentConfig(customConfig, tableColumnComponentName, 'edit', 'inline', tableOption);
        param.inlineItemConfig.eventHandlers = {
            //  绑定一个valid事件, 完成校验逻辑，如果校验不通过，则追加class: valid-error以便显示出来
            valid: (val, ref, props) => {
                colValid(val, param.inlineItemConfig).then(() => {
                    // ref.$el.classList.remove('valid-error') // !!! 这里不用ref是因为当el-table中存在fixed的列时,会渲染两个表格, 然后这个ref拿到的是另一个fixed的那个，导致无法正确显示/移除valid-error
                    props.class = defaultIfBlank(props.class, '').replace('valid-error', '');
                }).catch(errors => {
                    // ref.$el.classList.add('valid-error');
                    props.class = defaultIfBlank(props.class, '') + ' valid-error';
                });
                return val
            }
        }
    } catch (e) {
        console.error(e)
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