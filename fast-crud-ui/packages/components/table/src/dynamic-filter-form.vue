<template>
  <div class="fc-dynamic-filter-form">
    <div class="fc-dynamic-filter-sort-btn">
      <el-radio v-model="asc" label="" border :size="filter.props.size">不排序</el-radio>
      <el-radio v-model="asc" :label="true" border :size="filter.props.size">升序</el-radio>
      <el-radio v-model="asc" :label="false" border :size="filter.props.size">降序</el-radio>
    </div>
    <div class="fc-dynamic-filter-component-wrapper">
      <div class="title">输入过滤：</div>
      <div class="fc-dynamic-filter-component">
        <component :is="localFilter.component" v-model="localFilter.val" v-bind="localFilter.props"/>
      </div>
    </div>
    <div class="fc-dynamic-filter-distinct-wrapper">
      <div class="title">
        <span>DISTINCT：</span>
        <el-button type="text"
                   :style="{'color': distinctOptionAsc === '' ? 'gray': '#409EFF', 'padding': 0}"
                   :icon="distinctOptionsAscIcon"
                   @click="() => distinctOptionAsc = !distinctOptionAsc"
                   v-if="distinctLoaded"></el-button>
      </div>
      <!-- 由于distinct查询可能比较慢, 因此由用户点击触发展示 -->
      <div class="fc-dynamic-filter-distinct" v-loading="distinctLoading">
        <!-- distinct 勾选项 -->
        <el-input size="mini" v-model="distinctOptionFilterKeyword" :clearable="true" placeholder="输入过滤.." v-if="distinctLoaded"></el-input>
        <fast-checkbox-group :options="distinctFilteredOptions" :show-chose-all="false"
                             class="fc-dynamic-filter-distinct-options"
                             v-model="distinctCheckedValue"
                             v-if="distinctLoaded"></fast-checkbox-group>
        <div style="display: flex; justify-content: center;" v-if="!distinctLoaded">
          <el-button type="text" style="color: gray;" @click="distinctLoad">请点击加载</el-button>
        </div>
        <el-empty v-if="distinctLoaded && distinctOptions.length === 0">
          <template #image><span></span></template>
        </el-empty>
      </div>
    </div>
    <div class="fc-dynamic-filter-form-btn">
      <el-button type="primary" size="small" @click="ok">确认</el-button>
      <el-button size="small" @click="close">关闭</el-button>
    </div>
  </div>
</template>

<script>
import {FilterComponentConfig, Query} from "../../../model";
import {escapeValToLabel} from "./util";
import {isEmpty, isObject, toStr} from "../../../util/util";

export default {
  name: "dynamic-filter-form",
  props: {
    filter: FilterComponentConfig,
    order: [String],
    listUrl: String,
    conds: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    distinctOptionsAscIcon() {
      return this.distinctOptionAsc === '' ? 'el-icon-sort' : (this.distinctOptionAsc === true ? 'el-icon-sort-up' : 'el-icon-sort-down')
    },
    distinctFilteredOptions() {
      const {distinctOptionFilterKeyword, distinctOptionAsc} = this
      return this.distinctOptions.filter(item => isEmpty(distinctOptionFilterKeyword) || toStr(item.label).indexOf(distinctOptionFilterKeyword) !== -1)
          .sort((a, b) => {
            const label1 = toStr(a.label), label2 = toStr(b.label);
            return distinctOptionAsc ? label1.localeCompare(label2) : label2.localeCompare(label1)
          })
    }
  },
  data() {
    const localFilter = new FilterComponentConfig({...this.filter})
    const {col} = localFilter
    return {
      localFilter: localFilter,
      asc: this.order === 'asc' ? true : (this.order === 'desc' ? false : ''),
      distinctQuery: new Query().setDistinct().setCols([col]).setConds(this.conds),
      distinctLoaded: false, // 是否distinct query loaded
      distinctLoading: false, // 是否distinct query loading中
      distinctOptions: [], // 检索出的distinct选项
      distinctOptionAsc: '', // distinct选项排序, 默认不排序
      distinctOptionFilterKeyword: null, // distinct选项过滤关键词
      distinctCheckedValue: [], // 勾选的distinct值
      distinctAbortCtrl: null // distinct请求终止控制器
    }
  },
  methods: {
    distinctLoad() {
      this.distinctLoading = true;
      this.distinctAbortCtrl = new AbortController();
      const {col, component, props} = this.localFilter;
      this.$http.post(this.listUrl, this.distinctQuery.toJson(), { signal: this.distinctAbortCtrl.signal}).then(({data = []}) => {
        const distinctValues = data.filter(item => isObject(item) && item.hasOwnProperty(col)).map(item => item[col]);
        // 拼装distinctOptions TODO 如果结果值太多, 是采取前端滚动加载 or 展示top100? 如果太多, 比如针对createTime这种字段distinct就没有意义了
        this.distinctOptions = distinctValues.map(v => {
          return {
            value: v,
            label: escapeValToLabel(component, v, props)
          };
        })
        this.distinctLoaded = true;
      }).catch(err => {
        console.error(err)
      }).finally(() => {
        this.distinctLoading = false;
      })
    },
    ok() {
      let filterConfig;
      if (this.distinctCheckedValue.length > 0) {
        filterConfig = {
          component: 'fast-checkbox-group',
          col: this.localFilter.col,
          label: this.localFilter.label,
          opt: 'in',
          val: this.distinctCheckedValue,
          disabled: false,
          props: {
            options: this.distinctOptions,
            labelKey: 'label',
            valKey: 'value',
            showChoseAll: true
          }
        }
      } else {
        filterConfig = {...this.localFilter};
      }
      this.$emit('ok', {
        filter: new FilterComponentConfig(filterConfig),
        order: {
          col: this.localFilter.col,
          asc: this.asc
        }
      })
    },
    close() {
      this.$emit('cancel')
    }
  },
  beforeDestroy() {
    if (this.distinctAbortCtrl) {
      this.distinctAbortCtrl.abort()
    }
  }
}
</script>

<style scoped lang="scss">
.fc-dynamic-filter-form {
  & > * {
    margin-bottom: 10px;
  }

  & .title {
    margin-bottom: 5px;
    font-size: 13px;
    display: flex;
    justify-content: space-between;
  }

  .fc-dynamic-filter-sort-btn, .fc-dynamic-filter-component {
    display: flex;
    justify-content: space-between;

    & > * {
      flex: 1;
    }
  }

  .fc-dynamic-filter-component-wrapper {
    margin: 20px 0;
  }

  .fc-dynamic-filter-distinct {
    border: 1px solid #e1e1e1;
    padding: 10px;

    .fc-dynamic-filter-distinct-options {
      margin-top: 10px;

      ::v-deep {
        .el-checkbox {
          display: block;
        }
      }
    }
  }

  .fc-dynamic-filter-form-btn {
    display: flex;
    justify-content: right;
  }
}

</style>