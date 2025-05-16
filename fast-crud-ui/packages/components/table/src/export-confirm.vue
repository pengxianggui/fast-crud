<template>
  <div class="export-confirm">
    <el-table :data="columns">
<!--      <el-table-column prop="col" label="字段"/>-->
      <el-table-column prop="label" label="名称">
        <template #default="{row}">
          <el-input v-model="row.label" size="small" placeholder="请输入名称"></el-input>
        </template>
      </el-table-column>
      <el-table-column prop="exportable" label="是否导出">
        <template #default="{row}">
          <el-switch v-model="row.exportable"></el-switch>
        </template>
      </el-table-column>
    </el-table>
    <div class="footer">
      <span class="flex"></span>
      <el-button icon="el-icon-third-daochudangqianye" size="small" type="primary" plain @click="exportData(false)">导出当前页</el-button>
      <el-button icon="el-icon-third-daochuquanbu" size="small" type="primary" @click="exportData(true)">导出全部</el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: "export-confirm",
  props: {
    columns: Array
  },
  methods: {
    exportData(all) {
      this.$emit('ok', { columns: this.columns, all: all })
    }
  }
}
</script>

<style scoped lang="scss">
.footer {
  display: flex;
  margin-top: 10px;

  .flex {
    flex: 1;
  }
}
</style>