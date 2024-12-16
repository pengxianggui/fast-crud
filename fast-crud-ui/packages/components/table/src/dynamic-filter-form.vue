<template>
  <div class="fc-dynamic-filter-form">
    <!--    TODO 添加动态筛选-->
    <div class="sort-btn">
      <el-radio v-model="asc" label="" border :size="filter.props.size">不排序</el-radio>
      <el-radio v-model="asc" :label="true" border :size="filter.props.size">升序</el-radio>
      <el-radio v-model="asc" :label="false" border :size="filter.props.size">降序</el-radio>
    </div>
    <div class="component">
      <component :is="localFilter.component" v-model="localFilter.val" v-bind="localFilter.props"/>
    </div>
    <div class="fc-dynamic-filter-form-btn">
      <el-button type="primary" size="small" @click="ok">确认</el-button>
      <el-button size="small" @click="close">关闭</el-button>
    </div>
  </div>
</template>

<script>
import {FilterComponentConfig} from "../../../model";

export default {
  name: "dynamic-filter-form",
  props: {
    filter: FilterComponentConfig,
    order: [String]
  },
  data() {
    const localFilter = new FilterComponentConfig({...this.filter})
    return {
      localFilter: localFilter,
      asc: this.order === 'asc' ? true : (this.order === 'desc' ? false : '')
    }
  },
  methods: {
    ok() {
      this.$emit('ok', {
        filter: this.localFilter,
        order: {
          col: this.localFilter.col,
          asc: this.asc
        }
      })
    },
    close() {
      this.$emit('cancel')
    }
  }
}
</script>

<style scoped lang="scss">
.fc-dynamic-filter-form {
  & > * {
    margin-bottom: 10px;
  }

  .sort-btn, .component {
    display: flex;
    justify-content: space-between;

    & > * {
      flex: 1;
    }
  }

  .component {
    margin: 20px 0;
  }

  .fc-dynamic-filter-form-btn {
    display: flex;
    justify-content: right;
  }
}

</style>