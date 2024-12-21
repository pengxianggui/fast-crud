<template>
  <div class="fc-fast-table">
    <div class="fc-fast-table-title" v-if="option.title">{{ option.title }}</div>
    <div class="fc-quick-filter-wrapper" v-if="quickFilters.length > 0">
      <!-- 快筛 -->
      <quick-filter-form :filters="quickFilters"
                         :form-label-width="option.style.formLabelWidth"
                         :size="option.style.size"
                         @search="pageLoad"/>
    </div>
    <el-divider class="fc-fast-table-divider"></el-divider>
    <div class="fc-fast-table-operation-bar">
      <div class="fc-operation-filter">
        <!-- 简筛区 -->
        <easy-filter :filters="easyFilters" :size="option.style.size" @search="pageLoad"
                     v-if="easyFilters.length > 0"></easy-filter>
        <!-- TODO 2.0 存筛区 -->
      </div>
      <!-- 按钮功能区 -->
      <div class="fc-fast-table-operation-btn">
        <template v-if="status === 'normal'">
          <el-button :size="option.style.size" icon="el-icon-plus" @click="addRow">新建</el-button>
          <el-button type="danger" plain :size="option.style.size" @click="deleteRow"
                     v-if="checkedRows.length === 0">删除
          </el-button>
          <el-button type="danger" :size="option.style.size" @click="deleteRows(checkedRows)"
                     v-if="checkedRows.length > 0">删除
          </el-button>
        </template>
        <template v-if="status === 'update' || status === 'insert'">
          <el-button :size="option.style.size" icon="el-icon-plus" @click="addRow" v-if="status === 'insert'">继续新建</el-button>
          <el-button type="primary" :size="option.style.size" @click="saveEditRows">保存</el-button>
          <el-button :size="option.style.size" @click="cancelEditStatus">取消</el-button>
        </template>
        <!-- 下拉按钮-更多 -->
        <el-dropdown class="fc-fast-table-operation-more">
          <el-button type="primary" plain :size="option.style.size">
            更多<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="activeBatchEdit">批量编辑</el-dropdown-item>
            <!-- TODO 2.0 批量编辑、导出和自定义表格 -->
<!--            <el-dropdown-item @click.native="activeBatchUpdate">批量修改</el-dropdown-item>-->
<!--            <el-dropdown-item @click.native="exportData">导出</el-dropdown-item>-->
<!--            <el-dropdown-item @click.native="customTable">自定义表格</el-dropdown-item>-->
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
    <div class="fc-dynamic-filter-wrapper">
      <!-- 动筛列表 -->
      <dynamic-filter-list :filters="dynamicFilters" :size="option.style.size"
                           @search="pageLoad"></dynamic-filter-list>
    </div>
    <div class="fc-fast-table-wrapper">
      <el-table border :data="list"
                :row-style="rowStyle"
                highlight-current-row
                @current-change="handleChosedChange"
                @selection-change="handleCheckedChange"
                @row-dblclick="handleRowDblclick"
                v-loading="loading"
                :key="tableKey">
        <el-table-column type="selection" width="55" v-if="option.enableMulti"></el-table-column>
        <slot></slot>
      </el-table>
    </div>
    <div class="fc-pagination-wrapper">
      <el-pagination :page-size.sync="pageQuery.size"
                     :current-page.sync="pageQuery.current"
                     :page-sizes="option.pagination['page-sizes']"
                     :total="total"
                     @current-change="pageLoad"
                     @size-change="pageLoad"
                     :layout="option.pagination.layout"></el-pagination>
    </div>
  </div>
</template>

<script>
import {MessageBox, Message} from 'element-ui';
import {remove} from 'lodash-es';
import QuickFilterForm from "./quick-filter-form.vue";
import EasyFilter from "./easy-filter.vue";
import DynamicFilterForm from "./dynamic-filter-form.vue";
import DynamicFilterList from "./dynamic-filter-list.vue";
import {Order, PageQuery} from '../../../model';
import FastTableOption from "../../../model";
import {ifBlank, isBoolean, isEmpty, noRepeatAdd} from "../../../util/util";
import {iterBuildComponentConfig, toTableRow} from "./util";
import {openDialog} from "../../../util/dialog";
import {buildFinalComponentConfig} from "../../mapping";

export default {
  name: "FastTable",
  components: {QuickFilterForm, EasyFilter, DynamicFilterList},
  props: {
    option: {
      type: FastTableOption,
      required: true
    }
  },
  computed: {
    // 状态: normal-常规状态; insert-新增状态; update-编辑状态
    status() {
      const {editRows} = this;
      if (editRows.length === 0) {
        return 'normal';
      }
      if (editRows.every(r => r.status === 'update')) {
        return 'update';
      } else if (editRows.every(r => r.status === 'insert')) {
        return 'insert';
      } else {
        return 'normal';
      }
    },
    rowStyle() {
      return {
        height: this.option.style.bodyRowHeight
      }
    }
  },
  data() {
    const size = this.option.pagination.size;
    const pageQuery = new PageQuery(1, size);
    if (!ifBlank(this.option.sortField)) {
      pageQuery.setOrders([new Order(this.option.sortField, !this.option.sortDesc)])
    }

    return {
      tableKey: 0, // 用于前端刷新表格
      loading: false, // 表格数据是否正加载中
      choseRow: null, // 当前选中的行记录
      checkedRows: [], // 代表多选时勾选的行记录
      editRows: [], // 处于编辑状态的行, 包括新增和更新
      pageQuery: pageQuery, // 分页查询构造参数
      columnConfig: {}, // 列对应的配置。key: column prop属性名, value为通过fast-table-column*定义的属性(外加tableColumnComponentName属性)
      quickFilters: [], // 快筛配置
      easyFilters: [], // 简筛配置
      dynamicFilters: [], // 动筛配置
      list: [], // 表格当前页的数据, 不单纯有业务数据, 还有配置数据(用于实现行内、弹窗表单)
      total: 0 // 表格总数
    }
  },
  provide() {
    return {
      openDynamicFilterForm: this.openDynamicFilterForm // 提供给fast-table-column* 触发创建动筛的能力
    }
  },
  mounted() {
    this.buildComponentConfig();
    if (!this.option.lazyLoad) {
      this.pageLoad();
    }
  },
  methods: {
    buildComponentConfig() {
      const children = this.$slots.default ? this.$slots.default : [];
      iterBuildComponentConfig(children, this.option, ({
                                                         tableColumnComponentName,
                                                         col,
                                                         customConfig,
                                                         quickFilter,
                                                         easyFilter,
                                                         formItemConfig,
                                                         inlineItemConfig
                                                       }) => {
        if (quickFilter) {
          const {props = {}} = quickFilter;
          noRepeatAdd(this.quickFilters, quickFilter,
              (ele, item) => ele.col === item.col,
              props.hasOwnProperty('first-filter'))
        }
        if (easyFilter) {
          const {props = {}} = easyFilter;
          noRepeatAdd(this.easyFilters, easyFilter,
              (ele, item) => ele.col === item.col,
              props.hasOwnProperty('first-filter'))
        }
        this.columnConfig[col] = {
          tableColumnComponentName: tableColumnComponentName,
          customConfig: customConfig,
          formItemConfig: formItemConfig,
          inlineItemConfig: inlineItemConfig
        };
      })
    },
    /**
     * 暂只支持单列排序, 原因: 1.通过option指定的默认排序不好回显在表头; 2.多字段排序会导致操作比较繁琐
     * @param col
     * @param asc
     */
    buildOrder(col, asc) {
      if (isBoolean(asc)) {
        // 用户指定排序前, 当只有默认排序时, 移除默认排序
        if (!isEmpty(this.option.sortField) && this.pageQuery.orders.length === 1 && this.pageQuery.orders[0].col === this.option.sortField) {
          this.pageQuery.removeOrder(this.option.sortField);
        }
        this.pageQuery.addOrder(col, asc);
        return;
      }

      this.pageQuery.removeOrder(col);
      if (this.pageQuery.orders.length === 0) {
        this.pageQuery.addOrder(this.option.sortField, !this.option.sortDesc)
      }
    },
    /**
     * 分页加载请求
     */
    pageLoad() {
      const conds = []
      // 添加快筛条件
      const quickConds = this.quickFilters.filter(f => !f.disabled && f.hasVal()).map(f => f.getConds()).flat();
      conds.push(...quickConds)
      // 添加简筛条件
      const easyConds = this.easyFilters.filter(f => !f.disabled && f.hasVal()).map(f => f.getConds()).flat();
      conds.push(...easyConds)
      // 添加动筛条件
      const dynamicConds = this.dynamicFilters.filter(f => !f.disabled && f.hasVal()).map(f => f.getConds()).flat();
      conds.push(...dynamicConds)

      this.pageQuery.setConds(conds);
      const context = this.option.context;
      const beforeLoad = this.option.beforeLoad;
      return new Promise((resolve, reject) => {
        beforeLoad.call(context, {query: this.pageQuery}).then(() => {
          this.loading = true;
          this.$http.post(this.option.pageUrl, this.pageQuery.toJson()).then(res => {
            this.cancelEditStatus();
            const loadSuccess = this.option.loadSuccess;
            loadSuccess.call(context, {query: this.pageQuery, data: res.data, res}).then(({records, total}) => {
              this.list = records.map(r => toTableRow(r, this.columnConfig));
              this.total = total;
            }).finally(() => {
              resolve();
            })
          }).catch(err => {
            const loadFail = this.option.loadFail;
            loadFail.call(context, {query: this.pageQuery, error: err}).then(() => {
              Message.success('加载失败:' + JSON.stringify(err));
            })
            reject(err);
          }).finally(() => {
            this.loading = false;
          })
        }).catch(err => {
          reject(err);
        })
      })
    },
    addRow() {
      const {editType} = this.option;
      if (this.status !== 'normal' && this.status !== 'insert') {
        console.warn(`当前FastTable处于${this.status}状态, 不允许新增`);
        return;
      }
      if (editType === 'form') {
        // TODO 1.0 表单编辑
        console.error("暂未支持")
      } else {
        // 行内编辑: 增加一个编辑状态的空行, status为insert, 属性和值取自columnConfig.inlineItemConfig(col和defaultVal)
        const newRow = toTableRow({}, this.columnConfig, 'insert');
        this.list.unshift(newRow);
        this.editRows.push(newRow);
      }
    },
    /**
     * 删除: 删除当前选中行记录
     */
    deleteRow() {
      const {choseRow} = this;
      const rows = [];
      if (!isEmpty(choseRow)) {
        rows.push(choseRow);
      }
      this.deleteRows(rows);
    },
    /**
     * 批量删除: 删除当前勾选的行记录
     */
    deleteRows(list) {
      if (isEmpty(list)) {
        Message.warning('请先选中一条记录');
        return;
      }
      const rows = list.map(r => r.row)
      const {context, beforeDeleteTip} = this.option;
      beforeDeleteTip.call(context, {rows: rows}).then(() => {
        MessageBox.confirm(`确定删除这${rows.length}条记录吗？`, '删除确认', {}).then(() => {
          const {beforeDelete} = this.option;
          beforeDelete.call(context, {rows: rows}).then(() => {
            const {deleteUrl, batchDeleteUrl, deleteSuccess, deleteFail} = this.option;
            const postPromise = (rows.length === 1 ? this.$http.post(deleteUrl, rows[0]) : this.$http.post(batchDeleteUrl, rows))
            postPromise.then(res => {
              this.pageLoad(); // 始终刷新
              deleteSuccess.call(context, {rows: rows, res: res}).then(() => {
                Message.success('删除成功');
              })
            }).catch(err => {
              deleteFail.call(context, {rows: rows, error: err}).then(() => {
                Message.success('删除失败:' + JSON.stringify(err));
              })
            })
          }).catch((err) => {
            // 取消删除
          })
        });
      }).catch((err) => {
        // 取消删除提示和删除
      })
    },
    /**
     * 打开动筛面板: 构造动筛组件配置, 动态创建面板并弹出。由于动筛是动态的，不能在mounted阶段构造好。
     * @param column
     */
    openDynamicFilterForm(column) {
      if (!this.option.enableColumnFilter) {
        return;
      }
      const {prop, label, order} = column
      const {tableColumnComponentName, customConfig} = this.columnConfig[prop]
      const dynamicFilter = buildFinalComponentConfig(customConfig, tableColumnComponentName, 'query', 'dynamic')
      debugger
      openDialog({
        component: DynamicFilterForm,
        props: {
          filter: dynamicFilter,
          order: order,
          listUrl: this.option.listUrl,
          conds: this.pageQuery.conds
        },
        dialogProps: {
          width: '480px',
          title: `数据筛选及排序: ${label}`,
        }
      }).then(({filter: dynamicFilter, order}) => {
        if (dynamicFilter.hasVal()) {
          this.dynamicFilters.push(dynamicFilter);
        }

        if (isBoolean(order.asc)) {
          this.buildOrder(prop, order.asc)
          column.order = order.asc ? 'asc' : 'desc'
        } else {
          this.buildOrder(prop, order.asc)
          column.order = '';
        }
        this.pageLoad();
      }).catch(msg => {
        console.log(msg)
      })
    },
    handleChosedChange(row) {
      this.choseRow = row;
    },
    handleCheckedChange(rows) {
      this.checkedRows = rows;
    },
    handleRowDblclick(row, column, event) {
      const {enableDblClickEdit} = this.option;
      if (!enableDblClickEdit) {
        this.$emit('row-dblclick', row, column, event);
        return;
      }
      // 若当前编辑行已经处于编辑状态, 则直接emit并返回;
      if (row.status === 'update' || row.status === 'insert') {
        this.$emit('row-dblclick', row, column, event);
        return;
      }

      // opt1: 当前存在编辑行时，不允许再新增编辑行
      if (this.status !== 'normal') {
        this.$emit('row-dblclick', row, column, event);
        return;
      }
      const {context, beforeEnableUpdate} = this.option;
      beforeEnableUpdate.call(context, {rows: [row.row]}).then(() => {
        row.status = 'update';
        this.editRows.push(row);
      }).catch(() => {
        console.debug('你已取消编辑')
      })

      // // opt2: 如果已经存在编辑行, 则保存已存在的编辑(包括新增、更新)行。doubt: opt2还存在问题: $nextTick不生效, 新编辑的行无法呈现为编辑状态
      // this.saveEditRows().then(() => {
      //   this.cancelEditStatus();
      //   this.$nextTick(() => {
      //     row.status = 'update';
      //     row.editRow = {...row.row}
      //     // this.status = 'update';
      //     this.editRows.push(row);
      //   })
      // })
    },
    /**
     * 激活批量编辑
     */
    activeBatchEdit() {
      if (this.status !== 'normal') {
        Message.warning('请先退出编辑状态')
        return;
      }
      this.list.forEach(r => r.status = 'update');
      this.editRows.push(...this.list);
    },
    /**
     * 取消编辑状态: 包括新增、更新状态。会将编辑状态的行状态重置为'normal', 并清空编辑行数组editRows, 同时将表格状态重置为'normal'
     */
    cancelEditStatus() {
      if (this.editRows.length === 0) {
        return;
      }
      // 移除列表中可能存在的insert状态记录
      remove(this.list, item => item.status === 'insert');
      // 将编辑的行状态改为normal, 并清空editRows
      this.editRows.forEach(r => r.status = 'normal');
      this.editRows.length = 0;
      this.tableKey++; // 控制表格重新渲染
    },
    /**
     * 保存编辑的行: 包括新增或更新状态的行。内部会将保存成功的记录的行状态置为normal
     * @return 返回Promise。不存在需要保存的行 或 保存成功则返回resolve, 否则返回reject。
     */
    saveEditRows() {
      if (this.editRows.length === 0) {
        return Promise.resolve();
      }
      // 保存编辑的行: 包括新增、更新状态的行
      let promise;
      if (this.status === 'insert') {
        promise = this._insertRows(this.editRows);
      } else if (this.status === 'update') {
        promise = this._updateRows(this.editRows);
      } else {
        throw new Error(`当前FastTable状态异常:${this.status}, 无法保存编辑记录`);
      }
      return new Promise((resolve, reject) => {
        promise.then(() => {
          this.cancelEditStatus();
          this.pageLoad().then(() => resolve()).catch(() => reject());
        }).catch(() => reject());
      });
    },
    /**
     * 批量更新记录
     */
    activeBatchUpdate() {
      // TODO 2.0 激活勾选，针对勾选的记录弹出批量更新弹窗，可指定要更新的字段和值，点击确定应用于这些记录
    },
    /**
     * 导出数据
     */
    exportData() {
      // TODO 2.0 导出数据，基于前端col和label,props。导出当前页或当前筛选条件下的全部数据
    },
    /**
     * 自定义表格
     */
    customTable() {
      // TODO 2.0 自定义表格: 可自定义——表格标题、默认简筛字段、默认排序字段和排序方式、各列宽、冻结哪些列等
    },
    /**
     * 新增行, 返回promise
     * @param rows
     */
    _insertRows(rows) {
      if (rows.length === 0) {
        return Promise.resolve();
      }
      return new Promise((resolve, reject) => {
        const {context, beforeInsert} = this.option;
        beforeInsert.call(context, {fatRows: rows}).then(() => {
          const toBeInsertRows = rows.map(r => r.editRow);
          const {insertUrl, batchInsertUrl, insertSuccess, insertFail} = this.option;
          const postPromise = (toBeInsertRows.length === 1 ? this.$http.post(insertUrl, toBeInsertRows[0]) : this.$http.post(batchInsertUrl, toBeInsertRows))
          postPromise.then(res => {
            resolve();
            insertSuccess.call(context, {fatRows: rows, rows: toBeInsertRows, res: res}).then(() => {
              Message.success(`成功新增${toBeInsertRows.length}条记录`);
            });
          }).catch(err => {
            reject(err);
            insertFail.call(context, {fatRows: rows, rows: toBeInsertRows, error: err}).then(() => {
              Message.success('新增失败:' + JSON.stringify(err));
            });
          })
        }).catch(err => {
          reject(err);
        })
      });
    },
    /**
     * 更新行
     * @param rows
     * @return 返回promise, 若成功更新则resolve; 若失败或取消, 则返回reject err或用户自定义的内容
     */
    _updateRows(rows) {
      if (rows.length === 0) {
        return Promise.resolve();
      }
      return new Promise((resolve, reject) => {
        const {context, beforeUpdate} = this.option;
        beforeUpdate.call(context, {fatRows: rows}).then(() => {
          const toBeUpdateRows = rows.map(r => r.editRow);
          const {updateUrl, batchUpdateUrl, updateSuccess, updateFail} = this.option;
          const postPromise = (toBeUpdateRows.length === 1 ? this.$http.post(updateUrl, toBeUpdateRows[0]) : this.$http.post(batchUpdateUrl, toBeUpdateRows))
          postPromise.then(res => {
            resolve();
            updateSuccess.call(context, {fatRows: rows, rows: toBeUpdateRows, res: res}).then(() => {
              Message.success(`成功更新${toBeUpdateRows.length}条记录`);
            });
          }).catch(err => {
            reject(err);
            updateFail.call(context, {fatRows: rows, rows: toBeUpdateRows, error: err}).then(() => {
              Message.success('更新失败:' + JSON.stringify(err));
            });
          })
        }).catch(err => {
          reject(err);
        })
      });
    }
  }
}
</script>

<style scoped lang="scss">
.fc-fast-table {
  padding: 10px;

  .fc-fast-table-title {
    font-weight: bold;

  }

  .fc-quick-filter-wrapper {
    padding: 10px 0;

    .fc-quick-filter-form {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      justify-content: start;
      align-items: center;
      align-content: flex-start;
    }
  }

  .fc-fast-table-divider {
    margin: 0 0 10px 0;
  }

  .fc-fast-table-operation-bar {
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;

    .fc-fast-table-operation-more {
      margin-left: 10px;
    }
  }

  .fc-fast-table-wrapper {
    ::v-deep {
      th {
        padding: 0;

        & > .cell {
          padding: 0 !important;
        }
      }
    }
  }

  .fc-pagination-wrapper {
    display: flex;
    flex-direction: row-reverse;
    margin-top: 3px;
  }
}
</style>