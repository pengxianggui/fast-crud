<template>
  <div>
    <slot></slot>
    <i :class="column.order === 'asc' ? 'el-icon-sort-up' : 'el-icon-sort-down'"
       v-if="column.order !== ''"></i>
    <!-- TODO 提供一个form dialog提供编辑，并向外emit dynamicFilter。 如果这里提供dialog，会有很多个dialog-->
  </div>
</template>

<script>
export default {
  name: "fast-table-head-cell",
  props: {
    column: Object,
  }
}
</script>

<style scoped>

</style>