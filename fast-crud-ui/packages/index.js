import FastTable from './components/table/index'
import FastTableColumn from './components/table-column/index'
import FastTableColumnInput from './components/table-column-input/index'
import FastTableColumnNumber from './components/table-column-number/index'
import FastTableColumnDatePicker from './components/table-column-date-picker/index'
import {Input, DatePicker} from 'element-ui'
import "element-ui/lib/theme-chalk/index.css";

const components = [
    Input,
    DatePicker,
    FastTable,
    FastTableColumn,
    FastTableColumnInput,
    FastTableColumnNumber,
    FastTableColumnDatePicker
]

const install = function (Vue, opts = {}) {
    if (opts.hasOwnProperty('$http')) {
        Vue.prototype.$http = opts.$http
    }
    components.forEach(component => {
        Vue.component(component.name, component);
    });
};

if (typeof window !== 'undefined' && window.Vue) {
    install(window.Vue);
}

export default {
    install
};