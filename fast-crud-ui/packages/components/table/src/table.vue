<template>
  <div class="fc-fast-table">
    <div class="fc-quick-filter-wrapper" v-if="quickFilters.length > 0">
      <!-- 快筛 -->
      <quick-filter-form :filters="quickFilters"
                         :form-label-width="option.style.formLabelWidth"
                         :size="option.style.size"
                         @search="onSearch"/>
    </div>
    <div class="fc-operation-bar">
      <div class="fc-operation-filter">
        <!-- 简筛区 -->
        <easy-filter :filters="easyFilters" :size="option.style.size" @search="onSearch"
                     v-if="easyFilters.length > 0"></easy-filter>
        <!-- TODO 存筛区 -->
      </div>
      <!-- TODO 按钮功能区 -->
      <div class="fc-operation-btn">
        <el-button :size="option.style.size" @click="addRow">新增</el-button>
      </div>
    </div>
    <div class="fc-dynamic-filter-wrapper">
      <!-- TODO 动筛区 UI完成 -->
      <dynamic-filter-list :filters="dynamicFilters" :size="option.style.size" @search="onSearch"></dynamic-filter-list>
    </div>
    <div class="fc-fast-table-wrapper">
      <el-table border :data="list">
        <slot></slot>
      </el-table>
    </div>
    <div class="fc-pagination-wrapper">
      <el-pagination :page-size="pageQuery.size"
                     :current-page="pageQuery.current"
                     :page-sizes="option.pagination['page-sizes']"
                     :total="total"
                     :layout="option.pagination.layout"></el-pagination>
    </div>

    <!-- TODO 动筛创建dialog -->

    <!-- TODO form编辑dialog -->
  </div>
</template>

<script>
import QuickFilterForm from "./quick-filter-form.vue";
import EasyFilter from "./easy-filter.vue";
import DynamicFilterForm from "./dynamic-filter-form.vue";
import DynamicFilterList from "./dynamic-filter-list.vue";
import {Order, PageQuery} from '../../../model';
import FastTableOption from "../../../model";
import {ifBlank, isBoolean, isEmpty} from "../../../util/util";
import {iterBuildFilterConfig} from "./util";
import {openDialog} from "../../../util/dialog";
import {buildFinalFilterComponentConfig} from "../../mapping";

export default {
  name: "FastTable",
  components: {QuickFilterForm, EasyFilter, DynamicFilterList},
  props: {
    option: {
      type: FastTableOption,
      required: true
    }
  },
  data() {
    const size = this.option.pagination.size;
    const pageQuery = new PageQuery(1, size);
    if (!ifBlank(this.option.sortField)) {
      pageQuery.setOrders([new Order(this.option.sortField, !this.option.sortDesc)])
    }

    return {
      pageQuery: pageQuery,
      columnMap: {}, // key: column prop, value为自定义filterConfig
      quickFilters: [], // 快筛配置
      easyFilters: [], // 简筛配置
      dynamicFilters: [], // 动筛配置
      list: [],
      total: 0
    }
  },
  provide() {
    return {
      openDynamicFilterForm: this.openDynamicFilterForm // 提供给fast-table-column触发创建动筛的能力
    }
  },
  mounted() {
    this.buildFilters()
    if (!this.option.lazyLoad) {
      this.onSearch()
    }
  },
  methods: {
    buildFilters() {
      const children = this.$slots.default ? this.$slots.default : [];
      const props = { // 通过option传入配置项, 需要作用到filterConfig内
        size: this.option.style.size
      }
      iterBuildFilterConfig(children, props, ({
                                                tableColumnComponentName,
                                                label,
                                                prop,
                                                customConfig,
                                                quickFilter,
                                                easyFilter
                                              }) => {
        if (quickFilter) {
          this.quickFilters.push(quickFilter) // TODO 去重?
        }
        if (easyFilter) {
          this.easyFilters.push(easyFilter) // TODO 去重?
        }
        this.columnMap[prop] = {tableColumnComponentName, ...customConfig}
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
    onSearch() {
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

      this.pageQuery.setConds(conds)
      // TODO 兑现 this.option.beforeLoad
      this.$http.post(this.option.pageUrl, this.pageQuery.toJson()).then(res => {
        this.option.loadSuccess({query: this.pageQuery, data: res.data, res}).then((data) => {
          this.list = data.records
          this.total = data.total
        })
      }).catch(err => {
        // TODO 兑现 this.option.loadFail
      })
    },
    addRow() {
      // TODO 根据option.editType决定是弹出新增表单 OR 增加一个编辑状态的空行
    },
    openDynamicFilterForm(column) {
      // 打开动筛创建面板
      const {prop, label, order} = column
      const {tableColumnComponentName, ...customConfig} = this.columnMap[prop]
      const dynamicFilter = buildFinalFilterComponentConfig(customConfig, tableColumnComponentName, 'dynamic')
      openDialog({
        component: DynamicFilterForm,
        props: {
          filter: dynamicFilter,
          order: order
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
        this.onSearch();
      }).catch(msg => {
        console.log(msg)
      })
    }
  }
}
</script>

<style scoped lang="scss">
.fc-fast-table {
  padding: 10px;

  .fc-quick-filter-wrapper {
    padding: 10px;

    .fc-quick-filter-form {
      display: flex;
      flex-direction: row;
      flex-wrap: wrap;
      justify-content: start;
      align-items: center;
      align-content: flex-start;
    }
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