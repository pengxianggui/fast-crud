<template>
  <div class="fc_fast_table">
    <div class="fc_quick_filter_form_wrapper" v-if="quickFilters.length > 0">
      <el-form :inline="true" :label-width="option.style.formLabelWidth" class="fc_quick_filter_form">
        <el-form-item v-for="filter in quickFilters"
                      :key="filter.col"
                      :label="filter.label"
                      class="fc_quick_filter_form_item">
          <component :is="filter.component" v-model="filter.val" v-bind="filter.props"/>
        </el-form-item>
        <el-button type="primary" :size="option.style.size" @click="onSearch">搜索</el-button>
        <el-button :size="option.style.size" @click="onReset">重置</el-button>
      </el-form>
    </div>
    <div class="fc_fast_table_wrapper">
      <el-table border :data="list">
        <slot></slot>
      </el-table>
    </div>
    <div class="fc_pagination_wrapper">
      <el-pagination :page-size="pageQuery.size"
                     :current-page="pageQuery.current"
                     :page-sizes="option.pagination['page-sizes']"
                     :total="total"
                     :layout="option.pagination.layout"></el-pagination>
    </div>
  </div>
</template>

<script>
import {PageQuery} from '../../../model';
import FastTableOption, {FilterComponentConfig} from "../../../model";
import {ifBlank, merge} from "../../../util/util";
import {getConfigFn} from "../../mapping";

export default {
  name: "FastTable",
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
      pageQuery.addOrder(this.option.sortField, !this.option.sortDesc)
    }

    return {
      pageQuery: pageQuery,
      quickFilters: [], // 快捷筛选条件
      list: [],
      total: 0
    }
  },
  mounted() {
    this.buildQuickFilters()
    this.onSearch()
  },
  methods: {
    buildQuickFilters() {
      const children = this.$slots.default ? this.$slots.default : [];
      for (const vnode of children) {
        const {
          data: {
            attrs: {'quick-filter': quickFilter = false, label = '', prop: col = '', ...props} = {}
          } = {},
          componentOptions: {tag: tableColumnComponentName} = {}
        } = vnode
        if (!quickFilter) {
          continue;
        }
        // 排除props中后缀为__e的属性, 因为这些配置项仅用于编辑控件, 并将__q后缀的属性名移除此后缀
        const filteredProps = Object.keys(props).filter(key => !key.endsWith('__e'))
            .reduce((obj, key) => {
              obj[key.replace(/__q$/, '')] = props[key];
              return obj;
            }, {});
        const filterConfig = {label: label, col: col, props: {...filteredProps, size: this.option.style.size}}
        try {
          const queryConfig = getConfigFn(tableColumnComponentName, 'query')
          merge(filterConfig, queryConfig(filterConfig));
          this.quickFilters.push(new FilterComponentConfig(filterConfig)); // 创建Filter对象 TODO 去重
        } catch (err) {
          console.error(err)
        }
      }
    },
    onSearch() {
      const quickConds = []
      this.quickFilters.filter((filter) => filter.hasVal()).forEach((filter) => {
        quickConds.push(...filter.getConds())
      })
      this.pageQuery.setConds(quickConds)
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
    onReset() {
      this.quickFilters.forEach((filter) => filter.reset());
      this.onSearch();
    }
  }
}
</script>

<style scoped lang="scss">
.fc_quick_filter_form_wrapper {
  padding: 20px;

  .fc_quick_filter_form {
    display: flex;
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: start;
    align-items: center;
    align-content: flex-start;

    .fc_quick_filter_form_item {
      margin-bottom: 0 !important;
    }
  }
}

.fc_pagination_wrapper {
  display: flex;
  flex-direction: row-reverse;
  margin-top: 3px;
}
</style>