import {Input, InputNumber, DatePicker, Switch, TimePicker, Upload, Loading, Row, Col} from 'element-ui'
import FastCheckboxGroup from "./components/checkbox-group";
import FastSelect from "./components/select";
import FastUpload from "./components/upload";
import FastTable from './components/table'
import FastTableColumn from './components/table-column'
import FastTableColumnDatePicker from './components/table-column-date-picker'
import FastTableColumnImg from './components/table-column-img'
import FastTableColumnFile from './components/table-column-file'
import FastTableColumnInput from './components/table-column-input'
import FastTableColumnNumber from './components/table-column-number'
import FastTableColumnSelect from './components/table-column-select'
import FastTableColumnSwitch from './components/table-column-switch'
import FastTableColumnTextarea from './components/table-column-textarea'
import FastTableColumnTimePicker from './components/table-column-time-picker'
import {openDialog} from "./util/dialog";
import "element-ui/lib/theme-chalk/index.css";
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
    Input,
    InputNumber,
    DatePicker,
    Switch,
    TimePicker,
    Upload,
    Row,
    Col,
    FastCheckboxGroup,
    FastSelect,
    FastUpload,
    FastTable,
    FastTableColumn,
    FastTableColumnDatePicker,
    FastTableColumnFile,
    FastTableColumnImg,
    FastTableColumnInput,
    FastTableColumnNumber,
    FastTableColumnSelect,
    FastTableColumnSwitch,
    FastTableColumnTextarea,
    FastTableColumnTimePicker
];

const directives = [
    Loading
]

const install = function (Vue, opts = {}) {
    if (opts.hasOwnProperty('$http')) {
        Vue.prototype.$http = opts.$http;
        FastTableOption.$http = opts.$http;
    }

    components.forEach(component => {
        Vue.component(component.name, component);
    });

    directives.forEach(directive => {
        Vue.use(directive)
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
    openDialog
}

const filters = {
    ellipsis
}

export {
    FastTableOption,
    Opt,
    PageQuery,
    Query,
    Order,
    Cond,
    filters,
    util
}

export default {
    install
};