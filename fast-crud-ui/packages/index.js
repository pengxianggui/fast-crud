import {Loading} from 'element-ui'
import FastCheckboxGroup from "./components/checkbox-group";
import FastSelect from "./components/select";
import FastUpload from "./components/upload";
import FastObjectPicker from './components/object-picker'
import FastTable from './components/table'
import FastTableColumn from './components/table-column'
import FastTableColumnDatePicker from './components/table-column-date-picker'
import FastTableColumnImg from './components/table-column-img'
import FastTableColumnFile from './components/table-column-file'
import FastTableColumnInput from './components/table-column-input'
import FastTableColumnNumber from './components/table-column-number'
import FastTableColumnObject from './components/table-column-object'
import FastTableColumnSelect from './components/table-column-select'
import FastTableColumnSwitch from './components/table-column-switch'
import FastTableColumnTextarea from './components/table-column-textarea'
import FastTableColumnTimePicker from './components/table-column-time-picker'
import {openDialog} from "./util/dialog";
import {pick} from "./util/pick";
import "./style.scss"
import FastTableOption from "./model";
import {PageQuery, Query, Order, Cond, Opt} from "./model";
import {ellipsis} from "./filters";
import {
    isEmpty,
    isString,
    isNumber,
    isArray,
    ifBlank,
    isFunction,
    isObject,
    isBoolean,
    isNull,
    isUndefined,
    defaultIfEmpty,
    defaultIfBlank,
    camelCaseTo,
    caseToCamel,
    clear,
    deepClone,
    merge,
    coverMerge
} from "./util/util";

const components = [
    FastCheckboxGroup,
    FastSelect,
    FastUpload,
    FastObjectPicker,
    FastTable,
    FastTableColumn,
    FastTableColumnDatePicker,
    FastTableColumnFile,
    FastTableColumnImg,
    FastTableColumnInput,
    FastTableColumnNumber,
    FastTableColumnObject,
    FastTableColumnSelect,
    FastTableColumnSwitch,
    FastTableColumnTextarea,
    FastTableColumnTimePicker
];

const directives = [
    Loading
]

const filters = [
    ellipsis
]

const install = function (Vue, opts = {}) {
    if (opts.hasOwnProperty('$http')) {
        FastTableOption.$http = opts.$http;
    }
    components.forEach(component => {
        Vue.component(component.name, component);
    });
    directives.forEach(directive => {
        Vue.use(directive)
    })
    filters.forEach(filter => {
        Vue.filter(`fc${filter.name.charAt(0).toUpperCase() + filter.name.slice(1)}`, filter);
    })
};
if (typeof window !== 'undefined' && window.Vue) {
    install(window.Vue);

}

const util = {
    isEmpty,
    isString,
    isNumber,
    isArray,
    ifBlank,
    isFunction,
    isObject,
    isBoolean,
    isNull,
    isUndefined,
    defaultIfEmpty,
    defaultIfBlank,
    camelCaseTo,
    caseToCamel,
    clear,
    deepClone,
    merge,
    coverMerge,
    openDialog,
    pick
}

export {
    FastTableOption,
    Opt,
    PageQuery,
    Query,
    Order,
    Cond,
    util
}

export default {
    install
};