import {EditComponentConfig, FilterComponentConfig} from "../model";
import {isFunction, replaceKey} from "../util/util.js";
import FastTableColumnConfig from './table-column/config';
import FastTableColumnDatePickerConfig from './table-column-date-picker/config';
import FastTableColumnFileConfig from './table-column-file/config';
import FastTableColumnImgConfig from './table-column-img/config';
import FastTableColumnInputConfig from './table-column-input/config';
import FastTableColumnNumberConfig from './table-column-number/config';
import FastTableColumnObjectConfig from './table-column-object/config';
import FastTableColumnSelectConfig from './table-column-select/config';
import FastTableColumnSwitchConfig from './table-column-switch/config';
import FastTableColumnTextareaConfig from './table-column-textarea/config';
import FastTableColumnTimePickerConfig from './table-column-time-picker/config';

const MAPPING = {
    'fast-table-column': FastTableColumnConfig,
    'fast-table-column-date-picker': FastTableColumnDatePickerConfig,
    'fast-table-column-file': FastTableColumnFileConfig,
    'fast-table-column-img': FastTableColumnImgConfig,
    'fast-table-column-input': FastTableColumnInputConfig,
    'fast-table-column-number': FastTableColumnNumberConfig,
    'fast-table-column-object': FastTableColumnObjectConfig,
    'fast-table-column-select': FastTableColumnSelectConfig,
    'fast-table-column-switch': FastTableColumnSwitchConfig,
    'fast-table-column-textarea': FastTableColumnTextareaConfig,
    'fast-table-column-time-picker': FastTableColumnTimePickerConfig
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
export const buildFinalComponentConfig = function (customConfig, tableColumnComponentName, action, type, tableOption) {
    // 排除props中后缀为_e的属性, 因为这些配置项仅用于编辑控件, 并将_q后缀的属性名移除此后缀
    const customProps = replaceKey(customConfig.props, action === 'query' ? '_q' : '_e');
    const customFilterConfig = {
        label: customConfig.label,
        col: customConfig.col,
        props: {...customProps}
    }

    const finalConfigFn = getConfigFn(tableColumnComponentName, action);
    if (!isFunction(finalConfigFn)) {
        throw new Error(`未定义针对${tableColumnComponentName}的${action}控件`)
    }
    const finalConfig = finalConfigFn(customFilterConfig, type, tableOption);
    finalConfig.type = type;
    return action === 'query' ? new FilterComponentConfig(finalConfig) : new EditComponentConfig(finalConfig); // 创建Filter对象
}