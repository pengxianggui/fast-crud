<template>
  <el-table-column v-bind="$attrs">
    <slot></slot>
  </el-table-column>
</template>

<script>
export default {
  name: "FastTableColumnImg",
  data() {
    return {}
  }
}
</script>

<style scoped>

</style>