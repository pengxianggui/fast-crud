<template>
  <div class="export-confirm">
    <el-table :data="columns">
      <el-table-column prop="col" label="字段"/>
      <el-table-column prop="label" label="名称"/>
      <el-table-column label="是否导出">
        <!-- TODO -->
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: "export-confirm",
  props: {
    columns: Array
  }
}
</script>

<style scoped lang="scss">

</style>