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
    BTW: "between", // TODO 后端实现
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

    addOrder(col, asc) {
        this.orders.push(new Order(col, asc));
        return this;
    }

    removeOrder(col) {
        this.orders = this.orders.filter(order => order.col !== col);
        return this;
    }

    getOrder(col) {
        return this.orders.find(order => order.col === col);
    }

    setOrders(orders) {
        this.orders = orders;
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

    toJson() {
        // TODO [低优先级] 防止后端序列化策略为下划线, 这里将col、conds、orders中涉及的字段全部转换为驼峰, 因为这些值接口传输给后端时不受反序影响
        //  为了保证后端能正常对应到entity中的字段, 因此转为驼峰(这里是坚信entity中属性是驼峰命名).
        return this;
    }
}

/**
 * 筛选数据模型
 */
export class FilterComponentConfig {
    component; // 渲染组件
    col;
    opt;
    val;
    label; // 显示中文名
    props; // 组件对应的props
    defaultVal;
    quick;
    disabled;
    condMapFn = (cond) => [cond];

    /**
     * 构造函数
     * @param component 组件
     * @param col 字段名
     * @param opt 操作符
     * @param val 值
     * @param label 中文名
     * @param quick 是否是快速筛选项
     * @param props 组件对应的props
     * @param condMapFn 条件获取过滤函数
     */
    constructor({component, col, opt = Opt.LIKE, val, label, quick, props, condMapFn = (cond) => [cond]}) {
        this.component = component;
        this.col = col;
        this.opt = opt;
        this.val = val;
        this.label = label;
        this.quick = quick;
        this.props = props;
        if (!isUndefined(condMapFn)) {
            this.condMapFn = condMapFn;
        }

        this.defaultVal = val;
        this.disabled = false;
    }

    hasVal() {
        return this.val !== null && this.val !== undefined && this.val !== '';
    }

    reset() {
        this.val = this.defaultVal
    }

    getConds() {
        return this.condMapFn(new Cond(this.col, this.opt, this.val));
    }
}

// 定义 FastTableOption 类
class FastTableOption {
    context;
    title = '';
    module = '';
    pageUrl = '';
    insertUrl = '';
    updateUrl = '';
    deleteUrl = '';
    enableDblClickEdit = true;
    enableMulti = true;
    enableColumnFilter = true;
    editType = 'inline';
    sortField;
    sortDesc = true;
    pagination = {
        layout: 'total, sizes, prev, pager, next, jumper', 'page-sizes': [10, 20, 50, 100, 200], size: 20
    };
    style = {
        bodyRowHeight: 50, size: 'default', formLabelWidth: 'auto'
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

    // TODO 暂时没有用上，是采用挂载到Vue上，还是放到这里？
    static $http;

    constructor({
                    context,
                    title = '',
                    module = '',
                    pageUrl = '',
                    insertUrl = '',
                    updateUrl = '',
                    deleteUrl = '',
                    enableDblClickEdit = true,
                    enableMulti = true,
                    enableColumnFilter = true,
                    editType = 'inline',
                    sortField = '',
                    sortDesc = true,
                    pagination = {
                        layout: 'total, sizes, prev, pager, next, jumper',
                        'page-sizes': [10, 20, 50, 100, 200],
                        size: 20
                    },
                    style = {
                        bodyRowHeight: 50, size: 'default', formLabelWidth: 'auto'
                    },
                    beforeLoad = (scope) => Promise.resolve(scope.query),
                    loadSuccess = (scope) => Promise.resolve(scope.data),
                    loadFail = (scope) => Promise.resolve(),
                    beforeInsert = (scope) => Promise.resolve(scope),
                    insertSuccess = (scope) => Promise.resolve(),
                    insertFail = (scope) => Promise.resolve(),
                    beforeUpdate = (scope) => Promise.resolve(),
                    updateSuccess = (scope) => Promise.resolve(),
                    updateFail = (scope) => Promise.resolve(),
                    beforeDelete = (scope) => Promise.resolve(),
                    deleteSuccess = (scope) => Promise.resolve(),
                    deleteFail = (scope) => Promise.resolve(),
                    click = (scope) => Promise.resolve(),
                    dblclick = (scope) => Promise.resolve(),
                    beforeEnableCreate = (scope) => Promise.resolve(),
                    beforeEnableUpdate = (scope) => Promise.resolve(),
                    beforeDeleteTip = (scope) => Promise.resolve(),
                    beforeCancel = (scope) => Promise.resolve(),
                }) {
        // TODO 入参merge,
        this.context = context;
        this.title = title;
        this.pageUrl = defaultIfBlank(pageUrl, module + '/page');
        this.insertUrl = defaultIfBlank(insertUrl, module + '/insert');
        this.updateUrl = defaultIfBlank(updateUrl, module + '/update');
        this.deleteUrl = defaultIfBlank(deleteUrl, module + '/delete');
        this.enableDblClickEdit = enableDblClickEdit;
        this.enableMulti = enableMulti;
        this.enableColumnFilter = enableColumnFilter;
        this.editType = editType;
        this.sortField = sortField;
        this.sortDesc = sortDesc;
        // this.pagination = pagination;
        coverMerge(this.pagination, pagination, true, true)
        // this.style = style;
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