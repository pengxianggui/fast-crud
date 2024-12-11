import type Vue from "vue";
import {coverMerge, defaultIfBlank, isUndefined, merge} from "./util/util";
import type {AxiosInstance} from "axios";

export enum Opt {
    EQ = "=",
    NE = "!=",
    GT = ">",
    GE = ">=",
    LT = "<",
    LE = "<=",
    IN = "in",
    NIN = "nin",
    LIKE = "like",
    NLIKE = "nlike",
    NULL = "null",
    NNULL = "nnull",
    BTW = "between", // TODO 后端实现
}

export enum Rel {
    AND = "and",
    OR = "or"
}

export class Cond {
    col: String;
    opt: Opt;
    val: any;

    constructor(col: String, opt: Opt, val: any) {
        this.col = col;
        this.opt = opt;
        this.val = val;
    }

    setOpt(opt: Opt) {
        this.opt = opt;
        return this;
    }

    setVal(val: any) {
        this.val = val;
        return this;
    }
}

export class Order {
    col: String;
    asc: boolean;

    constructor(col: String, asc: boolean) {
        this.col = col;
        this.asc = asc;
    }

    setAsc(asc: boolean) {
        this.asc = asc;
        return this;
    }
}

export class Query {
    cols: Array<String> = [];
    conds: Array<Cond> = [];
    distinct: boolean = false;
    orders: Order[] = [];

    constructor() {
    }

    setCols(cols: String[]): Query {
        this.cols = cols;
        return this;
    }

    addCond(cond: Cond) {
        this.conds.push(cond);
        return this;
    }

    removeCond(col: String) {
        this.conds = this.conds.filter(cond => cond.col != col);
        return this;
    }

    setConds(conds: Array<Cond>): Query {
        this.conds = conds;
        return this;
    }

    getCond(col: String) {
        return this.conds.find(cond => cond.col == col);
    }

    addOrder(col: String, asc: boolean) {
        this.orders.push(new Order(col, asc));
        return this;
    }

    removeOrder(col: String) {
        this.orders = this.orders.filter(order => order.col != col);
        return this;
    }

    getOrder(col: String) {
        return this.orders.find(order => order.col == col);
    }

    setOrders(orders: Order[]): Query {
        this.orders = orders;
        return this;
    }
}

export class PageQuery extends Query {
    current: number = 1;
    size: number = 20;

    constructor(current: number = 1, size: number = 20) {
        super();
        this.current = current;
        this.size = size;
    }

    /**
     * 设置每页大小
     * @param size
     */
    setSize(size: number): PageQuery {
        this.size = size;
        return this;
    }

    toJson() {
        // TODO [低优先级] 防止后端序列化策略为下划线, 这里将col、conds、orders中涉及的字段全部转换为驼峰, 因为这些值接口传输给后端时不受反序影响
        //  为了保证后端能正常对应到entity中的字段, 因此转为驼峰(这里是坚信entity中属性是驼峰命名).
        return this;
    }
}

export interface ComponentConfig {
    component: string;
    col: string;
    val: any;
    opt: Opt;
    label: string;
    quick: boolean;
    props: Object;
    condMapFn: Function;
}

/**
 * 筛选数据模型
 */
export class FilterComponentConfig {
    component: string; // 渲染组件
    col: string;
    opt: Opt;
    val: any;
    label: string; // 显示中文名
    props: Object; // 组件对应的props
    defaultVal: any;
    quick: boolean;
    disabled: boolean;
    condMapFn: (cond: Cond) => [cond];

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
    constructor({component, col, opt = Opt.LIKE, val, label, quick, props, condMapFn}: ComponentConfig) {
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
        return this.val != null && this.val !== undefined && this.val !== '';
    }

    reset() {
        this.val = this.defaultVal
    }

    getConds() {
        return this.condMapFn(new Cond(this.col, this.opt, this.val));
    }
}

export interface Pager {
    current: number;
    size: number;
    total: number;
    records: Array<Object>;
}

interface PaginationOption {
    layout: string; // 分页组件的布局
    'page-sizes': number[]; // 可选的页大小
    size: number; // 已选择的页大小
}

// 定义 FastTableOption 的配置参数接口
interface FastTableOptionParams {
    context: Vue | undefined;
    // 标题
    title?: string;
    // 模块, 一般为Controller层面的path
    module?: string;
    // 分页接口地址
    pageUrl?: string;
    // 插入接口地址
    insertUrl?: string;
    // 更新接口地址
    updateUrl?: string;
    // 删除接口地址
    deleteUrl?: string;
    // 启用双击编辑
    enableDblClickEdit?: boolean;
    // 启用多选(勾选框)
    enableMulti?: boolean;
    // 启用列筛选(表头点击筛选)
    enableColumnFilter?: boolean;
    // 编辑类型: inline-行内编辑, form弹窗表单编辑
    editType?: 'inline' | 'form';
    // 表格行高
    bodyRowHeight?: number;
    // 默认排序字段
    sortField?: string;
    // 默认排序字段是否降序
    sortDesc?: boolean;
    // 表单标签宽度: 生效于搜索表单和编辑表单(editType='form')
    formLabelWidth?: number;
    // 分页配置
    pagination?: PaginationOption;

    // 分页数据加载前钩子: 可以拦截并修改请求参数
    beforeLoad?: (scope: { query: PageQuery }) => Promise<PageQuery>;
    // 分页数据加载成功后钩子: 可以拦截并修改返回数据
    loadSuccess?: (scope: { query: PageQuery, data: Pager, res: Response }) => Promise<Pager>;
    // 分页数据加载失败后钩子
    loadFail?: (scope: { query: PageQuery, res: Response, err: Error }) => Promise<any>;

    // 插入数据前: 可以拦截并修改插入数据，Promise.reject('任意值') 将取消插入操作
    beforeInsert?: (scope: { row: any }) => Promise<any>;
    // 插入数据成功后钩子: 可以拦截并修改返回数据
    insertSuccess?: (scope: { row: any, res: Response }) => Promise<any>;
    // 插入数据失败后钩子
    insertFail?: (scope: { row: any, res: Response, err: Error }) => Promise<any>;

    // 更新数据前: 可以拦截并修改更新数据，Promise.reject('任意值') 将取消更新操作
    beforeUpdate?: (scope: { row: any, editRow: any }) => Promise<any>;
    // 更新数据成功后钩子: 可以拦截并修改返回数据
    updateSuccess?: (scope: { row: any, res: Response }) => Promise<any>;
    // 更新数据失败后钩子
    updateFail?: (scope: { row: any, editRow: any, res: Response, err: Error }) => Promise<any>;

    // 删除数据前: row是已经删除的数据, Promise.reject('任意值') 将取消删除操作
    beforeDelete?: (scope: { row: any }) => Promise<any>;
    // 删除数据成功后钩子: row是已经删除的数据
    deleteSuccess?: (scope: { row: any, res: Response }) => Promise<any>;
    // 删除数据失败后钩子: row是删除失败的数据
    deleteFail?: (scope: { row: any, res: Response, err: Error }) => Promise<any>;

    // 行点击事件
    click?: (scope: { row: any; rowIndex: number }) => Promise<any>;
    // 行双击事件
    dblclick?: (scope: { row: any; rowIndex: number }) => Promise<any>;

    // 启用创建前: 点击新增按钮，进入编辑模式前(行内或表单), Promise.reject('任意值') 将取消操作
    beforeEnableCreate?: (scope: { row: any }) => Promise<any>;
    // 启用更新前: 双击进入编辑模式前(行内或表单), Promise.reject('任意值') 将取消操作
    beforeEnableUpdate?: (scope: { row: any }) => Promise<any>;
    // 删除提示框显示前: Promise.reject('任意值') 将取消删除操作
    beforeDeleteTip?: (scope: { row: any }) => Promise<any>;
    // 取消新增/编辑前: Promise.reject('任意值') 将取消取消操作
    beforeCancel?: (scope: { row: any, status: string }) => Promise<any>;
}

interface Style {
    bodyRowHeight: number; // 表格行高
    formLabelWidth: number; // 表单标签宽度
    size: string; // 按钮、表单组件等控件的大小
}

// 定义 FastTableOption 类
class FastTableOption {
    context: Vue | undefined;
    title?: string = '';
    module?: string = '';
    pageUrl?: string = '';
    insertUrl?: string = '';
    updateUrl?: string = '';
    deleteUrl?: string = '';
    enableDblClickEdit: boolean = true;
    enableMulti: boolean = true;
    enableColumnFilter: boolean = true;
    editType: 'inline' | 'form' = 'inline';
    sortField: string;
    sortDesc: boolean = true;
    pagination: PaginationOption = {
        layout: 'total, sizes, prev, pager, next, jumper',
        'page-sizes': [10, 20, 50, 100, 200],
        size: 20
    };
    style: Style = {
        bodyRowHeight: 50,
        size: 'default',
        formLabelWidth: 'auto'
    };
    beforeLoad: (scope: { query: PageQuery }) => Promise<PageQuery>;
    loadSuccess: (scope: { query: PageQuery, data: Pager, res: Response }) => Promise<Pager>;
    loadFail: (scope: { query: PageQuery, res: Response, err: Error }) => Promise<any>;
    beforeInsert: (scope: { row: any }) => Promise<any>;
    insertSuccess: (scope: { row: any, res: Response }) => Promise<any>;
    insertFail: (scope: { row: any, res: Response, err: Error }) => Promise<any>;
    beforeUpdate: (scope: { row: any, editRow: any }) => Promise<any>;
    updateSuccess: (scope: { row: any, res: Response }) => Promise<any>;
    updateFail: (scope: { row: any, editRow: any, res: Response, err: Error }) => Promise<any>;
    beforeDelete: (scope: { row: any }) => Promise<any>;
    deleteSuccess: (scope: { row: any, res: Response }) => Promise<any>;
    deleteFail: (scope: { row: any, res: Response, err: Error }) => Promise<any>;
    click: (scope: { row: any; rowIndex: number }) => Promise<any>;
    dblclick: (scope: { row: any; rowIndex: number }) => Promise<any>;
    beforeEnableCreate: (scope: { row: any }) => Promise<any>;
    beforeEnableUpdate: (scope: { row: any }) => Promise<any>;
    beforeDeleteTip: (scope: { row: any }) => Promise<any>;
    beforeCancel: (scope: { row: any, status: string }) => Promise<any>;

    // 声明一个静态的 $http 属性（可以通过 prototype 动态赋值）
    static $http: AxiosInstance;

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
                        bodyRowHeight: 50,
                        size: 'default',
                        formLabelWidth: 'auto'
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
                }: FastTableOptionParams) {
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