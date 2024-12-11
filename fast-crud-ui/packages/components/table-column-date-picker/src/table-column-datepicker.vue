<template>
  <el-table-column v-bind="$attrs">
    <slot></slot>
  </el-table-column>
</template>

<script>
export default {
  name: "FastTableColumnDatePicker",
  data() {
    return {}
  }
}
</script>

<style scoped>

</style>