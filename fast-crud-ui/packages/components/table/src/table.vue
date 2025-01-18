<template>
  <div class="fc-fast-table">
    <div ref="title" class="fc-fast-table-title" v-if="option.title">{{ option.title }}</div>
    <div ref="quick" class="fc-quick-filter-wrapper" v-if="quickFilters.length > 0">
      <!-- 快筛 -->
      <quick-filter-form :filters="quickFilters"
                         :form-label-width="option.style.formLabelWidth"
                         :size="option.style.size"
                         @search="pageLoad"/>
    </div>
    <div ref="operation" class="fc-fast-table-operation-bar">
      <div class="fc-operation-filter">
        <!-- 简筛区 -->
        <easy-filter :filters="easyFilters" :size="option.style.size" @search="pageLoad"
                     v-if="easyFilters.length > 0"></easy-filter>
        <!-- TODO 2.0 存筛区 -->
      </div>
      <!-- 按钮功能区 -->
      <div class="fc-fast-table-operation-btn">
        <slot name="button"
              v-bind="{size: option.style.size, choseRow: choseRow, checkedRows: checkedRows, editRows: editRows}"></slot>
        <template v-if="status === 'normal'">
          <el-button :size="option.style.size" icon="el-icon-plus" @click="toInsert"
                     v-if="option.insertable">新建
          </el-button>
          <el-button type="danger" plain :size="option.style.size" @click="deleteRow"
                     v-if="checkedRows.length === 0 && option.deletable">删除
          </el-button>
          <el-button type="danger" :size="option.style.size" @click="deleteRows"
                     v-if="checkedRows.length > 0 && option.deletable">删除
          </el-button>
        </template>
        <template v-if="status === 'update' || status === 'insert'">
          <el-button type="primary" :size="option.style.size" @click="saveEditRows">保存</el-button>
          <el-button :size="option.style.size" icon="el-icon-plus" @click="toInsert"
                     v-if="status === 'insert' && option.insertable">继续新建
          </el-button>
          <el-button :size="option.style.size" @click="cancelEditStatus">取消</el-button>
        </template>
        <!-- 下拉按钮-更多 -->
        <el-dropdown class="fc-fast-table-operation-more" trigger="click">
          <el-button type="primary" plain :size="option.style.size">
            更多<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item @click.native="activeBatchEdit" v-if="option.updatable">批量编辑</el-dropdown-item>
            <!-- TODO 2.0 批量编辑、导出和自定义表格 -->
            <!--            <el-dropdown-item @click.native="activeBatchUpdate">批量修改</el-dropdown-item>-->
            <!--            <el-dropdown-item @click.native="exportData">导出</el-dropdown-item>-->
            <!--            <el-dropdown-item @click.native="customTable">自定义表格</el-dropdown-item>-->
            <slot name="moreButton"
                  v-bind="{size: option.style.size, choseRow: choseRow, checkedRows: checkedRows, editRows: editRows}"></slot>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
    <div ref="dynamic" class="fc-dynamic-filter-wrapper">
      <!-- 动筛列表 -->
      <dynamic-filter-list :filters="dynamicFilters" :size="option.style.size"
                           @search="pageLoad"></dynamic-filter-list>
    </div>
    <div class="fc-fast-table-wrapper">
      <el-table v-bind="$attrs"
                :data="list"
                ref="table"
                :row-style="rowStyle"
                highlight-current-row
                @current-change="handleCurrentChange"
                @row-click="handleRowClick"
                @row-dblclick="handleRowDblclick"
                @select="handleSelect"
                @selection-change="handleSelectionChange"
                @select-all="handleSelectAll"
                v-loading="loading"
                :key="tableKey"
                :height="heightTable"
                border>
        <el-table-column type="selection" width="55" v-if="option.enableMulti"></el-table-column>
        <slot></slot>
      </el-table>
    </div>
    <div ref="pagination" class="fc-pagination-wrapper">
      <slot name="foot"
            v-bind="{size: option.style.size, choseRow: choseRow, checkedRows: checkedRows, editRows: editRows}">
        <span></span></slot>
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
import {Message} from 'element-ui';
import {remove} from 'lodash-es';
import QuickFilterForm from "./quick-filter-form.vue";
import EasyFilter from "./easy-filter.vue";
import DynamicFilterForm from "./dynamic-filter-form.vue";
import DynamicFilterList from "./dynamic-filter-list.vue";
import {PageQuery} from '../../../model';
import FastTableOption from "../../../model";
import {
  getFullHeight, getInnerHeight,
  ifBlank,
  isBoolean,
  isEmpty,
  isNull,
  noRepeatAdd
} from "../../../util/util";
import {getEditConfig, iterBuildComponentConfig, rowValid, toTableRow} from "./util";
import {openDialog} from "../../../util/dialog";
import {buildFinalComponentConfig} from "../../mapping";
import RowForm from "./row-form.vue";

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
    },
    heightTable() {
      if (this.$attrs.hasOwnProperty('height')) { // 自定义最高优先级
        return this.$attrs.height;
      }
      return this.tableFlexHeight;
    }
  },
  data() {
    const size = this.option.pagination.size;
    const pageQuery = new PageQuery(1, size);
    if (!ifBlank(this.option.sortField)) {
      pageQuery.addOrder(this.option.sortField, !this.option.sortDesc);
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
      total: 0, // 表格总数
      tableFlexHeight: null, //表格的弹性高度(动态计算值), 初始值是null非常重要, 如果内部计算出现问题外部又没自定义高度,相当于没有设置height值, 默认展示效果
    }
  },
  provide() {
    return {
      openDynamicFilterForm: this.openDynamicFilterForm, // 提供给fast-table-column* 触发创建动筛的能力
      tableStyle: this.option.style, // 提供给fast-table-column显示高度、尺寸等配置
      context: this.option.context // 提供给fast-table-column* 获取上下文信息
    }
  },
  mounted() {
    this.buildComponentConfig();
    if (!this.option.lazyLoad) {
      this.pageLoad();
    }
    if (this.option.style.flexHeight) {
      this.$nextTick(() => {
        this.calTableHeight();
        window.addEventListener('resize', this.calTableHeight);
        this.$watch('dynamicFilters.length', () => { // 动态过滤器变化时可能高度改变, 重新计算高度
          this.calTableHeight();
        })
      });
    }
  },
  methods: {
    /**
     * 添加到编辑行
     * @param fatRow
     */
    addToEditRows(fatRow) {
      this.editRows.push(...fatRow);
      rowValid(this.editRows, this.option.context).catch((errors) => {
      }); // 立即校验一下以便标识出必填等字段
    },
    /**
     * 重新渲染table，提供给外部是用
     */
    reRender() {
      this.tableKey++;
    },
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
              props.firstFilter !== false)
        }
        if (easyFilter) {
          const {props = {}} = easyFilter;
          noRepeatAdd(this.easyFilters, easyFilter,
              (ele, item) => ele.col === item.col,
              props.firstFilter !== false)
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
      if (isEmpty(col)) {
        return;
      }
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
      const quickConds = this.quickFilters.filter(f => !f.disabled && f.isEffective()).map(f => f.getConds()).flat();
      conds.push(...quickConds)
      // 添加简筛条件
      const easyConds = this.easyFilters.filter(f => !f.disabled && f.isEffective()).map(f => f.getConds()).flat();
      conds.push(...easyConds)
      // 添加动筛条件
      const dynamicConds = this.dynamicFilters.filter(f => !f.disabled && f.isEffective()).map(f => f.getConds()).flat();
      conds.push(...dynamicConds)
      // 添加固定的预置条件
      conds.push(...this.option.conds);
      this.pageQuery.setConds(conds);
      const context = this.option.context;
      const beforeLoad = this.option.beforeLoad;
      return new Promise((resolve, reject) => {
        beforeLoad.call(context, {query: this.pageQuery}).then(() => {
          this.loading = true;
          FastTableOption.$http.post(this.option.pageUrl, this.pageQuery.toJson()).then(res => {
            this.exitEditStatus();
            const loadSuccess = this.option.loadSuccess;
            loadSuccess.call(context, {query: this.pageQuery, data: res.data, res: res}).then(({records = [], total = 0}) => {
              this.list = records.map(r => toTableRow(r, this.columnConfig, 'normal', 'inline'));
              this.total = total;
              this.$nextTick(() => {
                this.setChoseRow(0); // 默认选中第一行
              })
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
    toInsert() {
      const {editType} = this.option;
      if (this.status !== 'normal' && this.status !== 'insert') {
        console.warn(`当前FastTable处于${this.status}状态, 不允许新增`);
        return;
      }
      if (editType === 'form') {
        this.addForm();
      } else {
        this.addRow();
      }
    },
    /**
     * 激活行内新增
     */
    addForm(row = {}) {
      if (this.option.insertable === false) {
        return;
      }
      const {context, beforeToInsert} = this.option;
      beforeToInsert.call(context).then(() => {
        const fatRow = toTableRow(row, this.columnConfig, 'insert', 'form');
        openDialog({
          component: RowForm,
          props: {
            option: this.option,
            config: fatRow.config,
            row: fatRow,
            type: 'insert',
            layout: this.option.style.formLayout
          },
          dialogProps: {
            width: '50%',
            title: '新增',
            'close-on-click-modal': false
          }
        }).then(() => {
          this.pageLoad();
        })
      }).catch(() => {
        console.debug('你已取消新建')
      })
    },
    /**
     * 增加一个新增编辑状态的行
     */
    addRow(row = {}) {
      this.addRows([row]);
    },
    /**
     * 增加多个新增状态的行
     * @param rows
     */
    addRows(rows = []) {
      if (this.option.insertable === false) {
        return;
      }
      if (this.status !== 'normal' && this.status !== 'insert') {
        Message.warning(`当前FastTable处于${this.status}状态, 不允许新增`);
        return;
      }
      const {context, beforeToInsert} = this.option;
      beforeToInsert.call(context).then(() => {
        const newRows = rows.map(r => toTableRow(r, this.columnConfig, 'insert', 'inline'));
        this.list.unshift(...newRows);
        this.addToEditRows(newRows);
      }).catch(() => {
        console.debug('你已取消新建')
      })
    },
    /**
     * 删除: 删除当前选中行记录
     */
    deleteRow() {
      if (this.option.deletable === false) {
        return;
      }
      const {choseRow} = this;
      const rows = [];
      if (!isEmpty(choseRow)) {
        rows.push(choseRow);
      }
      this.option._deleteRows(rows).then(() => this.pageLoad());
    },
    deleteRows() {
      if (this.option.deletable === false) {
        return;
      }
      this.option._deleteRows(this.checkedRows).then(() => this.pageLoad());
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
      openDialog({
        component: DynamicFilterForm,
        props: {
          filter: dynamicFilter,
          order: order,
          listUrl: this.option.listUrl,
          conds: this.pageQuery.conds,
          size: this.option.style.size
        },
        dialogProps: {
          width: '480px',
          title: `数据筛选及排序: ${label}`,
        }
      }).then(({filter: dynamicFilter, order}) => {
        if (dynamicFilter.isEffective()) {
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
    handleCurrentChange(row) {
      this.choseRow = row;
      this.$emit('current-change', {fatRow: row, row: isNull(row) ? null : row.row});
    },
    /**
     * 选中指定行
     * @param index 不传默认是0
     */
    setChoseRow(index = 0) {
      if (this.list.length === 0) {
        this.choseRow = null;
        this.$refs['table'].setCurrentRow(); // 清除选中高亮
        return;
      }
      this.choseRow = this.list[index];
      this.$refs['table'].setCurrentRow(this.choseRow);
    },
    getChoseRow() {
      return this.choseRow;
    },
    getCheckedRows() {
      return this.checkedRows;
    },
    handleSelect(rows, row) {
      this.$emit('select', {fatRows: rows, rows: rows.map(r => r.row), fatRow: row, row: row.row});
    },
    handleSelectionChange(rows) {
      this.checkedRows = rows;
      this.$emit('selection-change', {fatRows: rows, rows: rows.map(r => r.row)})
    },
    handleSelectAll(rows) {
      this.$emit('select-all', {fatRows: rows, rows: rows.map(r => r.row)});
    },
    handleRowClick(row, column, event) {
      this.$emit('row-click', {fatRow: row, column, event, row: row.row});
    },
    handleRowDblclick(row, column, event) {
      this.$emit('row-dblclick', {fatRow: row, column, event, row: row.row});
      const {enableDblClickEdit} = this.option;
      if (!enableDblClickEdit) {
        return;
      }
      // 若当前编辑行已经处于编辑状态, 则直接emit并返回;
      if (row.status === 'update' || row.status === 'insert') {
        return;
      }
      // 当前存在编辑行时，不允许再新增编辑行
      if (this.status !== 'normal') {
        return;
      }
      if (this.option.editType === 'form') {
        this.updateForm(row);
      } else {
        this.updateRow(row);
      }
    },
    /**
     * 表单更新一行
     * @param fatRow
     */
    updateForm(fatRow) {
      if (this.option.updatable === false) {
        return;
      }
      const {context, beforeToUpdate} = this.option;
      beforeToUpdate.call(context, {fatRows: [fatRow], rows: [fatRow.row]}).then(() => {
        openDialog({
          component: RowForm,
          props: {
            option: this.option,
            config: getEditConfig(this.columnConfig, 'form'),
            row: fatRow,
            type: 'update',
            layout: this.option.style.formLayout
          },
          dialogProps: {
            width: '50%',
            title: '编辑',
            'close-on-click-modal': false
          }
        }).then(() => {
          this.pageLoad();
        })
      }).catch(() => {
        console.debug('你已取消编辑')
      })
    },
    updateRow(fatRow) {
      if (this.option.updatable === false) {
        return;
      }
      if (this.status !== 'normal' && this.status !== 'update') {
        Message.warning(`当前FastTable处于${this.status}状态, 不允许更新`);
        return;
      }
      const {context, beforeToUpdate} = this.option;
      beforeToUpdate.call(context, {fatRows: [fatRow], rows: [fatRow.row]}).then(() => {
        fatRow.status = 'update';
        this.addToEditRows([fatRow]);
      }).catch(() => {
        console.debug('你已取消编辑')
      })
    },
    /**
     * 激活批量编辑
     */
    activeBatchEdit() {
      if (this.status !== 'normal') {
        Message.warning('请先退出编辑状态')
        return;
      }
      const {context, beforeToUpdate} = this.option;
      beforeToUpdate.call(context, {
        fatRows: this.list,
        rows: this.list.map(r => r.row),
        editRows: this.list.map(r => r.editRow)
      }).then(() => {
        this.list.forEach(r => r.status = 'update');
        this.addToEditRows(this.list);
      }).catch(() => {
        console.debug('你已取消编辑')
      })
    },
    /**
     * 取消编辑状态: 包括新增、更新状态。会将编辑状态的行状态重置为'normal', 并清空编辑行数组editRows, 同时将表格状态重置为'normal'
     */
    cancelEditStatus() {
      const {context, beforeCancel} = this.option;
      beforeCancel.call(context, {
        fatRows: this.editRows,
        rows: this.editRows.map(r => r.row),
        status: this.status
      }).then(() => {
        this.exitEditStatus();
      }).catch(() => {
        // 不允许取消
      })
    },
    exitEditStatus() {
      // 移除列表中可能存在的insert状态记录
      remove(this.list, item => item.status === 'insert');
      const isNormal = (this.status === 'normal'); // 非编辑状态
      // 将编辑的行状态改为normal, 并清空editRows,因为editRows是list中的引用，所以不能光清空数组
      this.editRows.forEach(r => {
        r.status = 'normal';
        r.editRow = {...r.row} // 重置editRow
      });
      this.editRows.length = 0;
      if (isNormal === false) { // 编辑状态时(尤其新建状态), 控制表格重新渲染, 避免一些“残留”
        this.reRender();
      }
    },
    /**
     * 保存编辑的行: 包括新增或更新状态的行。内部会将保存成功的记录的行状态置为normal
     * @return 返回Promise。不存在需要保存的行 或 保存成功则返回resolve, 否则返回reject。
     */
    saveEditRows() {
      if (this.editRows.length === 0) {
        return Promise.resolve();
      }
      if (this.status !== 'insert' && this.status !== 'update') {
        throw new Error(`当前FastTable状态异常:${this.status}, 无法保存编辑记录`);
      }
      rowValid(this.editRows, this.option.context).then(() => {
        // 保存编辑的行: 包括新增、更新状态的行
        let promise;
        if (this.status === 'insert') {
          promise = this.option._insertRows(this.editRows);
        } else {
          promise = this.option._updateRows(this.editRows);
        }
        promise.then(() => {
          this.exitEditStatus(); // 退出编辑状态
          this.pageLoad();
        }).catch(() => {
        });
      }).catch((errors) => {
        const firstError = errors[0];
        Message.error(firstError.message);
      })
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
     * 计算表格自适高度: tableFlexHeight
     */
    calTableHeight() {
      const totalHeight = getInnerHeight(this.$el);
      const titleHeight = getFullHeight(this.$refs.title);
      const quickHeight = getFullHeight(this.$refs.quick);
      const operationHeight = getFullHeight(this.$refs.operation);
      const dynamicHeight = getFullHeight(this.$refs.dynamic);
      const paginationHeight = getFullHeight(this.$refs.pagination);
      this.tableFlexHeight = totalHeight - titleHeight - quickHeight - operationHeight - dynamicHeight - paginationHeight - 2;
    }
  },
  beforeDestroy() {
    // 清理事件监听
    window.removeEventListener('resize', this.calTableHeight);
  }
}
</script>

<style scoped lang="scss">
.fc-fast-table {
  height: 100%;
  box-sizing: border-box;
  padding: 10px;
  overflow: auto;

  .fc-fast-table-title {
    font-weight: bold;
  }

  .fc-quick-filter-wrapper {
    padding: 10px 0;
    box-sizing: border-box;
    border-bottom: 1px solid #dfdfdf;
    margin-bottom: 10px;

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
    //height: auto;
    ::v-deep {
      .el-table__cell {
        padding: 0;
      }

      .fc-table-column-head-cell {
        text-align: center;
        height: 38px;
        line-height: 38px;
        margin: 0 10px;
      }

      .fc-table-column-head-cell.filter {
        cursor: pointer;
      }

      td.fc-table-column > .cell {
        padding: 0 3px;

        .fc-table-inline-edit-component {
          width: 100%;

          .el-input__inner {
            padding: 0 4px;
          }

          .el-input-number__decrease, .el-input-number__increase {
            width: 15px;
          }

          .el-input__prefix {
            display: none;
          }

          input {
            text-align: left;
          }


          .el-upload-list--picture-card .el-upload-list__item, .el-upload--picture-card {
            width: auto;
            height: 100%;
            aspect-ratio: 1 / 1;
            line-height: 100%;
            margin: 0;

            & .el-icon-plus {
              $uploadIconSize: 18px;
              font-size: $uploadIconSize;
              width: $uploadIconSize;
              height: $uploadIconSize;
              margin-top: calc(50% - $uploadIconSize / 2);
            }
          }

          .el-upload-list--text {
            .el-upload-list__item {
              //word-break: normal;
              margin: 0;
              line-height: 1;

              & > * {
                display: inline;
              }
            }
          }

          .el-link {
            //word-break: normal;
          }
        }

        .fc-table-inline-edit-component.valid-error {
          border: 1px solid #F56C6C;
        }
      }
    }
  }

  .fc-pagination-wrapper {
    display: flex;
    margin-top: 3px;
    justify-content: space-between;
  }
}
</style>