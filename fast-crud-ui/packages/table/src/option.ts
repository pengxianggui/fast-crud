// 定义分页配置类型
import type Vue from "vue";
import {Query} from 'fast-query-builder';

interface PaginationOption {
    layout: string; // 分页组件的布局
    'page-sizes': number[]; // 可选的页大小
}

// 定义表格操作的钩子函数类型: 必须返回Promise, 以便支持在客户端在钩子函数中执行异步操作。如果返回Promise.reject, 则不再后续操作
type HookFunction<T = any> = (arg: T) => Promise<T>;

// 定义 FastTableOption 的配置参数接口
interface FastTableOptionParams {
    context: Vue;
    title?: string;
    module?: string;
    pageUrl?: string;
    insertUrl?: string;
    updateUrl?: string;
    deleteUrl?: string;
    enableDblClick?: boolean;
    enableMulti?: boolean;
    columnFilter?: boolean;
    editType?: 'inline' | 'form'; // 编辑类型
    bodyRowHeight?: number;
    sortField?: string;
    sortDesc?: boolean;
    formLabelWidth?: number;
    pagination?: PaginationOption;

    beforeLoad?: (scope: { query: Query }) => Promise<Query>;
    loadSuccess?: (scope: { query: Query, data: Array<Object>, res: Response }) => Promise<Array<Object>>;
    loadFail?: (scope: { query: Query, res: Response, err: Error }) => Promise<any>;

    beforeInsert?: (scope: { row: any }) => Promise<any>;
    insertSuccess?: (scope: { row: any, res: Response }) => Promise<any>;
    insertFail?: (scope: { row: any, res: Response, err: Error }) => Promise<any>;

    beforeUpdate?: (scope: { row: any, editRow: any }) => Promise<any>;
    updateSuccess?: (scope: { row: any, res: Response }) => Promise<any>;
    updateFail?: (scope: { row: any, editRow: any, res: Response, err: Error }) => Promise<any>;

    beforeDelete?: (scope: { row: any }) => Promise<any>;
    deleteSuccess?: (scope: { row: any, res: Response }) => Promise<any>;
    deleteFail?: (scope: { row: any, res: Response, err: Error }) => Promise<any>;

    click?: (scope: { row: any; rowIndex: number }) => Promise<any>;
    dblclick?: (scope: { row: any; rowIndex: number }) => Promise<any>;

    beforeEnableCreate?: (scope: { row: any }) => Promise<any>;
    beforeEnableUpdate?: (scope: { row: any }) => Promise<any>;
    beforeEnableDelete?: (scope: { row: any }) => Promise<any>;
    beforeCancel?: (scope: { row: any, status: string }) => Promise<any>;
}

// 定义 FastTableOption 类
class FastTableOption {
    context: any;
    title?: string;
    module?: string;
    pageUrl?: string;
    insertUrl?: string;
    updateUrl?: string;
    deleteUrl?: string;
    enableDblClick: boolean;
    enableMulti: boolean;
    columnFilter: boolean;
    editType: 'inline' | 'form';
    bodyRowHeight: number;
    sortField?: string;
    sortDesc: boolean;
    formLabelWidth: number;
    pagination: PaginationOption;
    beforeLoad: (scope: { query: Query }) => Promise<Query>;
    loadSuccess: (scope: { query: Query, res: Response }) => Promise<Array<Object>>;
    loadFail: HookFunction<{ query: Query, res: Response, err: Error }>;
    beforeInsert: HookFunction<{ row: any }>;
    insertSuccess: HookFunction<{ row: any, res: Response }>;
    insertFail: HookFunction<{ row: any, res: Response, err: Error }>;
    beforeUpdate: HookFunction<{ row: any, editRow: any }>;
    updateSuccess: HookFunction<{ row: any, res: Response }>;
    updateFail: HookFunction<{ row: any, editRow: any, res: Response, err: Error }>;
    beforeDelete: HookFunction<{ row: any }>;
    deleteSuccess: HookFunction<{ row: any, res: Response }>;
    deleteFail: HookFunction<{ row: any, res: Response, err: Error }>;
    click: HookFunction<{ row: any; rowIndex: number }>;
    dblclick: HookFunction<{ row: any; rowIndex: number }>;
    beforeEnableCreate: HookFunction<{ row: any }>;
    beforeEnableUpdate: HookFunction<{ row: any }>;
    beforeEnableDelete: HookFunction<{ row: any }>;
    beforeCancel: HookFunction<{ row: any }>;

    constructor({
                    context,
                    title = '',
                    module = '',
                    pageUrl = '',
                    insertUrl = '',
                    updateUrl = '',
                    deleteUrl = '',
                    enableDblClick = false,
                    enableMulti = false,
                    columnFilter = false,
                    editType = 'inline',
                    bodyRowHeight = 40,
                    sortField = '',
                    sortDesc = false,
                    formLabelWidth = 120,
                    pagination = {
                        layout: 'prev, pager, next, ->, total',
                        'page-sizes': [10, 20, 50, 100],
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
                    beforeEnableDelete = (scope) => Promise.resolve(),
                    beforeCancel = (scope) => Promise.resolve(),
                }: FastTableOptionParams) {
        this.context = context;
        this.title = title;
        this.pageUrl = pageUrl;
        this.insertUrl = insertUrl;
        this.updateUrl = updateUrl;
        this.deleteUrl = deleteUrl;
        this.enableDblClick = enableDblClick;
        this.enableMulti = enableMulti;
        this.columnFilter = columnFilter;
        this.editType = editType;
        this.bodyRowHeight = bodyRowHeight;
        this.sortField = sortField;
        this.sortDesc = sortDesc;
        this.formLabelWidth = formLabelWidth;
        this.pagination = pagination;

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
        this.beforeEnableDelete = beforeEnableDelete;
        this.beforeCancel = beforeCancel;
    }
}

export default FastTableOption;
