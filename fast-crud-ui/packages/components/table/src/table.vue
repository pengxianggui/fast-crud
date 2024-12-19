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
    <div class="fc-operation-bar">
      <div class="fc-operation-filter">
        <!-- 简筛区 -->
        <easy-filter :filters="easyFilters" :size="option.style.size" @search="pageLoad"
                     v-if="easyFilters.length > 0"></easy-filter>
        <!-- TODO 存筛区 -->
      </div>
      <!-- TODO 按钮功能区 -->
      <div class="fc-operation-btn">
        <div v-if="status === 'normal'">
          <el-button :size="option.style.size" @click="addRow">新增</el-button>
          <el-button type="danger" plain :size="option.style.size" @click="deleteRow"
                     v-if="checkedRows.length === 0">删除
          </el-button>
          <el-button type="danger" :size="option.style.size" @click="deleteRows(checkedRows)"
                     v-if="checkedRows.length > 0">删除
          </el-button>
        </div>
        <div v-if="status === 'update' || status === 'insert'">
          <el-button type="primary" :size="option.style.size" @click="saveEditRows">保存</el-button>
          <el-button :size="option.style.size" @click="cancelEditStatus">取消</el-button>
        </div>
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
                v-loading="loading">
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

    <!-- TODO form编辑dialog -->
  </div>
</template>

<script>
import {MessageBox, Message} from 'element-ui';
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
      status: 'normal', // 状态: normal-常规状态; insert-新增状态; update-编辑状态
      loading: false, // 表格数据是否正加载中
      choseRow: null, // 当前选中的行记录
      checkedRows: [], // 代表多选时勾选的行记录
      editRows: [], // 处于编辑状态的行
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
      beforeLoad.call(context, {query: this.pageQuery}).then(() => {
        this.loading = true;
        this.$http.post(this.option.pageUrl, this.pageQuery.toJson()).then(res => {
          const loadSuccess = this.option.loadSuccess;
          loadSuccess.call(context, {query: this.pageQuery, data: res.data, res}).then(({records, total}) => {
            this.list = records.map(r => toTableRow(r, this.columnConfig));
            this.total = total;
          })
        }).catch(err => {
          const loadFail = this.option.loadFail;
          loadFail.call(context, {query: this.pageQuery, error: err}).then(() => {
            Message.success('加载失败:' + JSON.stringify(err));
          })
        }).finally(() => {
          this.loading = false;
        })
      }).catch(err => {
      })
    },
    addRow() {
      const {editType} = this.option;
      if (editType === 'form') {
        // TODO 表单编辑
      } else {
        // TODO 行内编辑: 增加一个编辑状态的空行
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
    deleteRows(rows) {
      if (isEmpty(rows)) {
        Message.warning('请先选中一条记录');
        return;
      }
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
      // TODO 若当前编辑行已经处于编辑状态, 则直接emit并返回; 否则, 若已经存在其他编辑状态的行, 则将其他编辑状态的行提交保存
      row.status = 'edit';
      this.status = 'update';
      this.editRows.push(row);
    },
    cancelEditStatus() {
      this.editRows.forEach(r => r.status = 'normal');
      this.status = 'normal';
    },
    saveEditRows() {
      // TODO
      console.log(this.editRows);
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

  .fc-operation-bar {
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;
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