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
                $attrs: {filter = true, 'quick-filter': quickFilter = false, label = '', prop: col = '', ...attrs} = {},
                _props = {} // 默认属性
            },
            componentOptions: {tag: tableColumnComponentName, propsData = {}} = {} // 传入属性
        } = vnode
        // debugger
        const props = {...attrs, ..._props, ...propsData}
        if (!filter) {
            continue;
        }
        // 排除props中后缀为__e的属性, 因为这些配置项仅用于编辑控件, 并将__q后缀的属性名移除此后缀
        const filteredProps = Object.keys(props).filter(key => !key.endsWith('__e'))
            .reduce((obj, key) => {
                obj[key.replace(/__q$/, '')] = props[key];
                return obj;
            }, {});
        const filterConfig = {
            label: label,
            col: col,
            quick: quickFilter,
            props: {...filteredProps, ...defaultProp}
        }

        let quickFilterComponentConfig = null;
        let easyFilterComponentConfig = null;
        try {
            // build quick filters
            if (quickFilter) {
                quickFilterComponentConfig = buildFinalFilterComponentConfig(filterConfig, tableColumnComponentName, 'quick')
            }
            // build easy filters
            easyFilterComponentConfig = buildFinalFilterComponentConfig(filterConfig, tableColumnComponentName, 'easy')
        } catch (err) {
            console.error(err)
        } finally {
            callback(quickFilterComponentConfig, easyFilterComponentConfig)
        }
    }

}