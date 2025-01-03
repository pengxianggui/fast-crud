import {Message, MessageBox} from 'element-ui';
import {
    assert,
    caseToCamel,
    coverMerge,
    defaultIfBlank,
    isBoolean,
    isEmpty, isFunction,
    isString,
    isUndefined
} from "./util/util.js";

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
    EMPTY: "empty",
    NEMPTY: "nempty",
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
        if (isEmpty(col) || !isBoolean(asc)) {
            return;
        }
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

    toJson() {
        // 防止后端序列化策略为下划线, 这里将col、conds、orders中涉及的字段全部转换为驼峰, 因为这些值接口传输给后端时不受反序影响
        //  为了保证后端能正常对应到entity中的字段, 因此转为驼峰(这里是坚信entity中属性是驼峰命名).
        const cols = this.cols.map(col => caseToCamel(col, '_'));
        const conds = this.conds.map(cond => new Cond(caseToCamel(cond.col, '_'), cond.opt, cond.val));
        const orders = this.orders.map(order => new Order(caseToCamel(order.col, '_'), order.asc));
        return {
            cols: cols,
            conds: conds,
            orders: orders,
            distinct: this.distinct
        };
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
        const json = super.toJson();
        return {
            ...json,
            current: this.current,
            size: this.size
        }
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
    disabled; // 是否禁用: 表示该筛选项是否可用
    type; // quick, easy, dynamic
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
    constructor({component, col, opt = Opt.LIKE, val, label, props, condMapFn = (cond) => [cond], type}) {
        this.component = component;
        this.col = col;
        this.opt = opt;
        this.val = val;
        this.label = label;
        const {disabled, ...validProps} = props; // 移除props中的disabled属性,disabled属性对查询时无效
        this.props = validProps;
        if (!isUndefined(condMapFn)) {
            this.condMapFn = condMapFn;
        }

        this.defaultVal = val;
        this.disabled = false;
        this.type = type;
    }

    /**
     * 此筛选项是否有效
     * @returns {boolean}
     */
    isEffective() {
        if (this.opt === Opt.NULL || this.opt === Opt.NNULL || this.opt === Opt.EMPTY || this.opt === Opt.NEMPTY) {
            return true;
        }
        return this.val !== null && this.val !== undefined && this.val !== '' && this.val.length !== 0;
    }

    reset() {
        this.val = this.defaultVal
    }

    getConds() {
        if (this.opt === Opt.NULL || this.opt === Opt.NNULL || this.opt === Opt.EMPTY || this.opt === Opt.NEMPTY) {
            return [new Cond(this.col, this.opt, null)]
        }
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
    val;
    editable; // 是否可编辑, true 表示可编辑, false表示不可编辑, insert-表示新增时可编辑, update-表示更新时可编辑. 默认为true. TODO 1.0兑现: 从prop中移出来
    type; // inline, form
    eventMethods; // 组件事件触发时调用其中的方法，例如参数验证

    constructor({component, col, label, props, val, eventMethods, type}) {
        this.component = component;
        this.col = col;
        this.label = label;
        const {editable, ...validProps} = props; // 移除props中的editable属性,避免editable对组件影响
        this.props = validProps;
        this.val = val;
        this.editable = editable;
        this.eventMethods = eventMethods;
        this.type = type;
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
    uploadUrl = ''; // 文件上传接口
    exportUrl = ''; // 数据导出接口 TODO 2.0 兑现
    enableDblClickEdit = true;
    enableMulti = true; // 启用多选
    enableColumnFilter = true; // 启用列过滤：即动筛
    lazyLoad = false; // 不立即加载数据
    editType = 'inline'; // inline/form
    insertable = true; // 是否支持内置新建
    updatable = true; // 是否支持内置编辑
    deletable = true; // 是否支持内置删除
    sortField;
    sortDesc = true;
    pagination = {
        layout: 'total, sizes, prev, pager, next, jumper', 'page-sizes': [10, 20, 50, 100, 200], size: 10
    };
    style = {
        flexHeight: false, // 表格是否使用弹性高度: 自适应高度, 撑满全屏
        bodyRowHeight: '50px', // 行高
        size: 'medium',  // 尺寸
        formLabelWidth: 'auto', // 表单标签宽度:
        formLayout: null // 表单布局: 只作用于form表单, 对快筛和行内编辑无效
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
    beforeToInsert; // 进入新建前(行内编辑新建前，或新建表单弹窗前)
    beforeToUpdate; // 进入更新前(行内编辑更新前，或更新表单弹窗前)
    beforeDeleteTip;
    beforeCancel; // 工能区中取消按钮点击前

    static $http;

    /**
     * 新增行, 返回promise
     * @param fatRows
     * @returns {Promise<void>|Promise<unknown>}
     * @private
     */
    _insertRows(fatRows) {
        if (fatRows.length === 0) {
            return Promise.resolve();
        }
        return new Promise((resolve, reject) => {
            const {context, beforeInsert} = this;
            const rows = fatRows.map(r => r.row);
            const editRows = fatRows.map(r => r.editRow);
            beforeInsert.call(context, {
                fatRows: fatRows,
                rows: rows,
                editRows: editRows
            }).then(() => {
                const {insertUrl, batchInsertUrl, insertSuccess, insertFail} = this;
                const postPromise = (editRows.length === 1 ? FastTableOption.$http.post(insertUrl, editRows[0]) : FastTableOption.$http.post(batchInsertUrl, editRows))
                postPromise.then(res => {
                    resolve();
                    insertSuccess.call(context, {
                        fatRows: fatRows,
                        rows: rows,
                        editRows: editRows,
                        res: res
                    }).then(() => {
                        Message.success(`成功新增${editRows.length}条记录`);
                    });
                }).catch(err => {
                    reject(err);
                    insertFail.call(context, {
                        fatRows: fatRows,
                        rows: rows,
                        editRows: editRows,
                        error: err
                    }).then(() => {
                        Message.success('新增失败:' + JSON.stringify(err));
                    });
                })
            }).catch(err => {
                reject(err);
            })
        });
    }

    /**
     * 批量删除: 删除当前勾选的行记录
     */
    _deleteRows(fatRows) {
        if (this.deletable === false) {
            return Promise.reject('当前表格不允许删除');
        }
        if (isEmpty(fatRows)) {
            Message.warning('请先选中一条记录');
            return Promise.reject('请先选中一条记录');
        }

        return new Promise((resolve, reject) => {
            const rows = fatRows.map(r => r.row);
            const editRows = fatRows.map(r => r.editRow);
            const {context, beforeDeleteTip} = this;
            beforeDeleteTip.call(context, {
                fatRows: fatRows,
                rows: rows,
                editRows: editRows
            }).then(() => {
                MessageBox.confirm(`确定删除这${rows.length}条记录吗？`, '删除确认', {}).then(() => {
                    const {beforeDelete} = this;
                    beforeDelete.call(context, {fatRows: fatRows, rows: rows, editRows: editRows}).then(() => {
                        const {deleteUrl, batchDeleteUrl, deleteSuccess, deleteFail} = this;
                        const postPromise = (rows.length === 1 ? FastTableOption.$http.post(deleteUrl, rows[0]) : FastTableOption.$http.post(batchDeleteUrl, rows))
                        postPromise.then(res => {
                            resolve(); // 始终刷新
                            deleteSuccess.call(context, {
                                fatRows: fatRows,
                                rows: rows,
                                editRows: editRows,
                                res: res
                            }).then(() => {
                                Message.success('删除成功');
                            })
                        }).catch(err => {
                            reject(err);
                            deleteFail.call(context, {fatRows, rows: rows, editRows: editRows, error: err}).then(() => {
                                Message.success('删除失败:' + JSON.stringify(err));
                            })
                        })
                    }).catch((err) => {
                        // 取消删除
                        reject(err);
                    })
                });
            }).catch((err) => {
                // 取消删除提示和删除
                reject(err);
            })
        })
    }

    /**
     * 更新行
     * @param fatRows
     * @return 返回promise, 若成功更新则resolve; 若失败或取消, 则返回reject err或用户自定义的内容
     */
    _updateRows(fatRows) {
        if (fatRows.length === 0) {
            return Promise.resolve();
        }
        return new Promise((resolve, reject) => {
            const {context, beforeUpdate} = this;
            const rows = fatRows.map(r => r.row);
            const editRows = fatRows.map(r => r.editRow);
            beforeUpdate.call(context, {
                fatRows: fatRows,
                rows: rows,
                editRows: editRows
            }).then(() => {
                const {updateUrl, batchUpdateUrl, updateSuccess, updateFail} = this;
                const postPromise = (editRows.length === 1 ? FastTableOption.$http.post(updateUrl, editRows[0]) : FastTableOption.$http.post(batchUpdateUrl, editRows))
                postPromise.then(res => {
                    resolve();
                    updateSuccess.call(context, {
                        fatRows: fatRows,
                        rows: rows,
                        editRows: editRows,
                        res: res
                    }).then(() => {
                        Message.success(`成功更新${editRows.length}条记录`);
                    });
                }).catch(err => {
                    reject(err);
                    updateFail.call(context, {
                        fatRows: fatRows,
                        rows: rows,
                        editRows: editRows,
                        error: err
                    }).then(() => {
                        Message.success('更新失败:' + JSON.stringify(err));
                    });
                })
            }).catch(err => {
                reject(err);
            })
        });
    }

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
                    uploadUrl = '',
                    exportUrl = '',
                    enableDblClickEdit = true,
                    enableMulti = true,
                    enableColumnFilter = true,
                    lazyLoad = false,
                    editType = 'inline',
                    insertable = true,
                    updatable = true,
                    deletable = true,
                    sortField = '',
                    sortDesc = true,
                    pagination = {
                        layout: 'total, sizes, prev, pager, next, jumper',
                        'page-sizes': [10, 20, 50, 100, 200],
                        size: 10
                    },
                    style = {
                        flexHeight: false,
                        bodyRowHeight: '50px',
                        size: 'medium',
                        formLabelWidth: 'auto',
                        formLayout: null
                    },
                    beforeLoad = ({query}) => Promise.resolve(),
                    loadSuccess = ({query, data, res}) => Promise.resolve(data),
                    loadFail = ({query, error}) => Promise.resolve(),
                    beforeToInsert = () => Promise.resolve(),
                    beforeInsert = ({fatRows, rows, editRows}) => Promise.resolve(),
                    insertSuccess = ({fatRows, rows, editRows, res}) => Promise.resolve(),
                    insertFail = ({fatRows, rows, editRows, error}) => Promise.resolve(),
                    beforeToUpdate = ({fatRows, rows}) => Promise.resolve(),
                    beforeUpdate = ({fatRows, rows, editRows}) => Promise.resolve(),
                    updateSuccess = ({fatRows, rows, editRows, res}) => Promise.resolve(),
                    updateFail = ({fatRows, rows, editRows, error}) => Promise.resolve(),
                    beforeDeleteTip = ({fatRows, rows}) => Promise.resolve(),
                    beforeDelete = ({fatRows, rows}) => Promise.resolve(),
                    deleteSuccess = ({fatRows, rows, res}) => Promise.resolve(),
                    deleteFail = ({fatRows, rows, error}) => Promise.resolve(),
                    beforeCancel = ({fatRows, rows, status}) => Promise.resolve(),
                }) {
        assert(isString(title), 'title必须为字符串')
        assert(isString(module), 'module必须为字符串')
        assert(isBoolean(enableDblClickEdit), 'enableDblClickEdit必须为布尔值')
        assert(isBoolean(enableMulti), 'enableMulti必须为布尔值')
        assert(isBoolean(enableColumnFilter), 'enableColumnFilter必须为布尔值')
        assert(isBoolean(lazyLoad), 'lazyLoad必须为布尔值')
        assert(['inline', 'form'].includes(editType), 'editType必须为inline或form')
        assert(isBoolean(insertable), 'insertable必须为布尔值')
        assert(isBoolean(updatable), 'updatable必须为布尔值')
        assert(isBoolean(deletable), 'deletable必须为布尔值')
        assert(isString(sortField), 'sortField必须为字符串')
        assert(isBoolean(sortDesc), 'sortDesc必须为布尔值')
        assert(isFunction(beforeLoad), 'beforeLoad必须为函数')
        assert(isFunction(loadSuccess), 'loadSuccess必须为函数')
        assert(isFunction(loadFail), 'loadFail必须为函数')
        assert(isFunction(beforeToInsert), 'beforeToInsert必须为函数')
        assert(isFunction(beforeInsert), 'beforeInsert必须为函数')
        assert(isFunction(insertSuccess), 'insertSuccess必须为函数')
        assert(isFunction(insertFail), 'insertFail必须为函数')
        assert(isFunction(beforeToUpdate), 'beforeToUpdate必须为函数')
        assert(isFunction(beforeUpdate), 'beforeUpdate必须为函数')
        assert(isFunction(updateSuccess), 'updateSuccess必须为函数')
        assert(isFunction(updateFail), 'updateFail必须为函数')
        assert(isFunction(beforeDeleteTip), 'beforeDeleteTip必须为函数')
        assert(isFunction(beforeDelete), 'beforeDelete必须为函数')
        assert(isFunction(deleteSuccess), 'deleteSuccess必须为函数')
        assert(isFunction(deleteFail), 'deleteFail必须为函数')
        assert(isFunction(beforeCancel), 'beforeCancel必须为函数')

        this.context = context;
        this.title = title;
        this.module = module;
        this.pageUrl = defaultIfBlank(pageUrl, module + '/page');
        this.listUrl = defaultIfBlank(listUrl, module + '/list');
        this.insertUrl = defaultIfBlank(insertUrl, module + '/insert');
        this.batchInsertUrl = defaultIfBlank(batchInsertUrl, module + '/insert/batch');
        this.updateUrl = defaultIfBlank(updateUrl, module + '/update');
        this.batchUpdateUrl = defaultIfBlank(batchUpdateUrl, module + '/update/batch');
        this.deleteUrl = defaultIfBlank(deleteUrl, module + '/delete');
        this.batchDeleteUrl = defaultIfBlank(batchDeleteUrl, module + '/delete/batch');
        this.uploadUrl = defaultIfBlank(uploadUrl, module + '/upload');
        this.exportUrl = defaultIfBlank(exportUrl, module + '/export');
        this.enableDblClickEdit = enableDblClickEdit;
        this.enableMulti = enableMulti;
        this.enableColumnFilter = enableColumnFilter;
        this.lazyLoad = lazyLoad;
        this.editType = editType;
        this.insertable = insertable;
        this.updatable = updatable;
        this.deletable = deletable;
        this.sortField = sortField;
        this.sortDesc = sortDesc;
        coverMerge(this.pagination, pagination, true, true)
        coverMerge(this.style, style, true, true)

        this.beforeLoad = beforeLoad;
        this.loadSuccess = loadSuccess;
        this.loadFail = loadFail;

        this.beforeToInsert = beforeToInsert;
        this.beforeInsert = beforeInsert;
        this.insertSuccess = insertSuccess;
        this.insertFail = insertFail;

        this.beforeToUpdate = beforeToUpdate;
        this.beforeUpdate = beforeUpdate;
        this.updateSuccess = updateSuccess;
        this.updateFail = updateFail;

        this.beforeDeleteTip = beforeDeleteTip;
        this.beforeDelete = beforeDelete;
        this.deleteSuccess = deleteSuccess;
        this.deleteFail = deleteFail;

        this.beforeCancel = beforeCancel;
    }
}

export default FastTableOption;