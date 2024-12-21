import {coverMerge, defaultIfBlank, isUndefined} from "./util/util.js";

export const Opt = Object.freeze({
    EQ: "=",
    NE: "!=",
    GT: ">",
    GE: ">=",
    LT: "<",
    LE: "<=",
    IN: "in",
    NIN: "nin",
    LIKE: "like",
    NLIKE: "nlike",
    NULL: "null",
    NNULL: "nnull",
    BTW: "between",
})

export const Rel = Object.freeze({
    AND: "and", OR: "or"
})

export class Cond {
    col;
    opt;
    val;

    constructor(col, opt, val) {
        this.col = col;
        this.opt = opt;
        this.val = val;
    }

    setOpt(opt) {
        this.opt = opt;
        return this;
    }

    setVal(val) {
        this.val = val;
        return this;
    }
}

export class Order {
    col;
    asc;

    constructor(col, asc = false) {
        this.col = col;
        this.asc = asc;
    }

    setAsc(asc) {
        this.asc = asc;
        return this;
    }
}

export class Query {
    cols = [];
    conds = [];
    distinct = false;
    orders = [];

    constructor() {
    }

    setCols(cols) {
        this.cols = cols;
        return this;
    }

    addCond(cond) {
        this.conds.push(cond);
        return this;
    }

    removeCond(col) {
        this.conds = this.conds.filter(cond => cond.col !== col);
        return this;
    }

    setConds(conds) {
        this.conds = conds;
        return this;
    }

    getCond(col) {
        return this.conds.find(cond => cond.col === col);
    }

    setDistinct() {
        this.distinct = true;
        return this;
    }

    addOrder(col, asc) {
        this.removeOrder(col)
        this.orders.push(new Order(col, asc));
        return this;
    }

    removeOrder(col) {
        const idx = this.orders.findIndex(o => o.col === col);
        if (idx > -1) {
            this.orders.splice(idx, 1)
        }
        return this;
    }

    getOrder(col) {
        return this.orders.find(order => order.col === col);
    }

    setOrders(orders) {
        this.orders = orders;
        return this;
    }

    toJson() {
        // TODO 1.0 [低优先级] 防止后端序列化策略为下划线, 这里将col、conds、orders中涉及的字段全部转换为驼峰, 因为这些值接口传输给后端时不受反序影响
        //  为了保证后端能正常对应到entity中的字段, 因此转为驼峰(这里是坚信entity中属性是驼峰命名).
        return this;
    }
}

export class PageQuery extends Query {
    current = 1;
    size = 20;

    constructor(current = 1, size = 20) {
        super();
        this.current = current;
        this.size = size;
    }

    /**
     * 设置每页大小
     * @param size
     */
    setSize(size) {
        this.size = size;
        return this;
    }
}

/**
 * 筛选组件配置
 */
export class FilterComponentConfig {
    component; // 渲染组件
    col; // 字段名
    opt; // 操作符
    val; // 值
    label; // 显示中文名
    props; // 组件对应的props
    defaultVal; // 默认值
    disabled; // 是否禁用
    condMapFn = (cond) => [cond];

    /**
     * 构造函数
     * @param component 组件
     * @param col 字段名
     * @param opt 操作符
     * @param val 值
     * @param label 中文名
     * @param props 组件对应的props
     * @param condMapFn 条件获取过滤函数
     */
    constructor({component, col, opt = Opt.LIKE, val, label, props, condMapFn = (cond) => [cond]}) {
        this.component = component;
        this.col = col;
        this.opt = opt;
        this.val = val;
        this.label = label;
        this.props = props;
        if (!isUndefined(condMapFn)) {
            this.condMapFn = condMapFn;
        }

        this.defaultVal = val;
        this.disabled = false;
    }

    hasVal() {
        return this.val !== null && this.val !== undefined && this.val !== '' && this.val.length !== 0;
    }

    reset() {
        this.val = this.defaultVal
    }

    getConds() {
        return this.condMapFn(new Cond(this.col, this.opt, this.val));
    }
}

/**
 * 编辑组件配置
 */
export class EditComponentConfig {
    component;
    col;
    label;
    props;
    defaultVal;
    disabled;

    constructor({component, col, label, props, defaultVal, disabled}) {
        this.component = component;
        this.col = col;
        this.label = label;
        this.props = props;
        this.defaultVal = defaultVal;
        this.disabled = disabled;
    }
}

// 定义 FastTableOption 类
class FastTableOption {
    context;
    title = '';
    module = '';
    pageUrl = '';
    listUrl = '';
    insertUrl = '';
    batchInsertUrl = '';
    updateUrl = '';
    batchUpdateUrl = '';
    deleteUrl = '';
    batchDeleteUrl = '';
    enableDblClickEdit = true;
    enableMulti = true; // 启用多选
    enableColumnFilter = true; // 启用列过滤：即动筛。TODO 1.0 关了以后，排序也用不了了: 需要在表头外面加排序按钮
    lazyLoad = false; // 不立即加载数据
    editType = 'inline'; // inline/form
    sortField;
    sortDesc = true;
    pagination = {
        layout: 'total, sizes, prev, pager, next, jumper', 'page-sizes': [10, 20, 50, 100, 200], size: 10
    };
    style = {
        bodyRowHeight: '50px', size: 'default', formLabelWidth: 'auto'
    };
    beforeLoad;
    loadSuccess;
    loadFail;
    beforeInsert;
    insertSuccess;
    insertFail;
    beforeUpdate;
    updateSuccess;
    updateFail;
    beforeDelete;
    deleteSuccess;
    deleteFail;
    click;
    dblclick;
    beforeEnableCreate;
    beforeEnableUpdate;
    beforeDeleteTip;
    beforeCancel;

    constructor({
                    context,
                    title = '',
                    module = '',
                    pageUrl = '',
                    listUrl = '',
                    insertUrl = '',
                    batchInsertUrl = '',
                    updateUrl = '',
                    batchUpdateUrl = '',
                    deleteUrl = '',
                    batchDeleteUrl = '',
                    enableDblClickEdit = true,
                    enableMulti = true,
                    enableColumnFilter = true,
                    lazyLoad = false,
                    editType = 'inline',
                    sortField = '',
                    sortDesc = true,
                    pagination = {
                        layout: 'total, sizes, prev, pager, next, jumper',
                        'page-sizes': [10, 20, 50, 100, 200],
                        size: 10
                    },
                    style = {
                        bodyRowHeight: '50px', size: 'default', formLabelWidth: 'auto'
                    },
                    beforeLoad = ({query}) => Promise.resolve(),
                    loadSuccess = ({query, data, res}) => Promise.resolve(data),
                    loadFail = ({query, error}) => Promise.resolve(),
                    beforeInsert = ({fatRows}) => Promise.resolve(fatRows),
                    insertSuccess = ({fatRows, rows, res}) => Promise.resolve(),
                    insertFail = ({fatRows, rows, error}) => Promise.resolve(),
                    beforeUpdate = ({fatRows}) => Promise.resolve(fatRows),
                    updateSuccess = ({fatRows, rows, res}) => Promise.resolve(),
                    updateFail = ({fatRows, rows, error}) => Promise.resolve(),
                    beforeDelete = ({rows}) => Promise.resolve(rows),
                    deleteSuccess = ({rows, res}) => Promise.resolve(),
                    deleteFail = ({rows, error}) => Promise.resolve(),
                    click = (scope) => Promise.resolve(),
                    dblclick = (scope) => Promise.resolve(),
                    beforeEnableCreate = (scope) => Promise.resolve(),
                    beforeEnableUpdate = (scope) => Promise.resolve(),
                    beforeDeleteTip = ({rows}) => Promise.resolve(),
                    beforeCancel = (scope) => Promise.resolve(),
                }) {
        this.context = context;
        this.title = title;
        this.pageUrl = defaultIfBlank(pageUrl, module + '/page');
        this.listUrl = defaultIfBlank(listUrl, module + '/list');
        this.insertUrl = defaultIfBlank(insertUrl, module + '/insert');
        this.batchInsertUrl = defaultIfBlank(batchInsertUrl, module + '/insert/batch');
        this.updateUrl = defaultIfBlank(updateUrl, module + '/update');
        this.batchUpdateUrl = defaultIfBlank(batchUpdateUrl, module + '/update/batch');
        this.deleteUrl = defaultIfBlank(deleteUrl, module + '/delete');
        this.batchDeleteUrl = defaultIfBlank(batchDeleteUrl, module + '/delete/batch');
        this.enableDblClickEdit = enableDblClickEdit;
        this.enableMulti = enableMulti;
        this.enableColumnFilter = enableColumnFilter;
        this.lazyLoad = lazyLoad;
        this.editType = editType;
        this.sortField = sortField;
        this.sortDesc = sortDesc;
        coverMerge(this.pagination, pagination, true, true)
        coverMerge(this.style, style, true, true)

        this.beforeLoad = beforeLoad;
        this.loadSuccess = loadSuccess;
        this.loadFail = loadFail;

        this.beforeInsert = beforeInsert;
        this.insertSuccess = insertSuccess;
        this.insertFail = insertFail;

        this.beforeUpdate = beforeUpdate;
        this.updateSuccess = updateSuccess;
        this.updateFail = updateFail;

        this.beforeDelete = beforeDelete;
        this.deleteSuccess = deleteSuccess;
        this.deleteFail = deleteFail;

        this.click = click;
        this.dblclick = dblclick;

        this.beforeEnableCreate = beforeEnableCreate;
        this.beforeEnableUpdate = beforeEnableUpdate;
        this.beforeDeleteTip = beforeDeleteTip;
        this.beforeCancel = beforeCancel;
    }
}

export default FastTableOption;