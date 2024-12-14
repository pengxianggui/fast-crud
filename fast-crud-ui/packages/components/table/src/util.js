import {buildFinalFilterComponentConfig} from "../../mapping";

/**
 * 构建自定义过滤器配置
 * @param vnodes fast-table-column-* 组成的节点数组，蕴含fast-table-column-*上的信息
 * @param defaultProp 默认属性配置
 * @param callback 针对每个table-column解析的回调函数
 */
export function iterBuildFilterConfig(vnodes, defaultProp = {}, callback) {
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

        const {filter = true, 'quick-filter': quickFilter = false, label, prop} = {...$attrs, _props, ...propsData}
        const props = {...$attrs, ..._props, ...propsData}
        if (!filter) {
            continue;
        }
        // 排除props中后缀为__e的属性, 因为这些配置项仅用于编辑控件, 并将__q后缀的属性名移除此后缀
        const filteredProps = Object.keys(props).filter(key => !key.endsWith('__e'))
            .reduce((obj, key) => {
                obj[key.replace(/__q$/, '')] = props[key];
                return obj;
            }, {});
        const customConfig = {
            label: label,
            col: prop,
            quick: quickFilter,
            props: {...filteredProps, ...defaultProp}
        }

        const param = {}
        try {
            // build quick filters
            if (quickFilter) {
                param.quickFilter = buildFinalFilterComponentConfig(customConfig, tableColumnComponentName, 'quick')
            }
            // build easy filters
            param.easyFilter = buildFinalFilterComponentConfig(customConfig, tableColumnComponentName, 'easy')
        } catch (err) {
            console.error(err)
        } finally {
            callback({...param, tableColumnComponentName, prop, label, customConfig})
        }
    }

}