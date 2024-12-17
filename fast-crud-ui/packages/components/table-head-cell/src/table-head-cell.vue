<template>
  <div>
    <slot></slot>
    <i :class="column.order === 'asc' ? 'el-icon-sort-up' : 'el-icon-sort-down'" class="sort-icon"
       v-if="column.order === 'asc' || column.order === 'desc'"></i>
  </div>
</template>

<script>
export default {
  name: "fast-table-head-cell",
  props: {
    column: Object
  }
}
</script>

<style scoped lang="scss">
.sort-icon {
  color: #409eff;
}
</style>