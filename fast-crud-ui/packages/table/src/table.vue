<template>
  <div class="fc_fast_table">
    <div class="fc_quick_filter_form_wrapper" v-if="quickFilters.length > 0">
      <el-form :model="searchForm" :inline="true">
        <el-form-item v-for="field in quickFilters"
                      :key="field.prop"
                      :label="field.label">
          <component :is="field.type" v-model="searchForm[field.prop]" v-bind="field.props"/>
        </el-form-item>
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button type="primary" @click="onReset">重置</el-button>
      </el-form>
    </div>
    <div class="fc_fast_table_wrapper">
      <el-table>
        <slot></slot>
      </el-table>
    </div>
    <div class="fc_pagination_wrapper">

    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "FastTable",
  props: {
    option: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      quickFilters: [], // 快捷筛选条件
    }
  }
})
</script>

<style scoped>

</style>