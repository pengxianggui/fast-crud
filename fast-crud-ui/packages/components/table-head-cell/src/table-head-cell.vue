<template>
  <div>
    <slot></slot>

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