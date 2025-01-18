<template>
  <el-table-column class-name="fc-table-column"
                   :prop="prop"
                   :label="label"
                   :show-overflow-tooltip="showOverflowToolTip"
                   v-bind="$attrs">
    <template #header="{column, $index}">
      <fast-table-head-cell class="fc-table-column-head-cell" :class="{'filter': filter}" :column="columnProp"
                            @click.native="headCellClick(column)">
        <slot name="header" v-bind:column="column" v-bind:$index="$index">
          <span>{{ column.label }}</span>
        </slot>
      </fast-table-head-cell>
    </template>

    <template #default="{row, column, $index}">
      <slot v-bind:row="row" v-bind:column="column" v-bind:$index="$index">
        <span>{{ showLabel(row, column) }}</span>
      </slot>
    </template>
  </el-table-column>
</template>

<script>
import FastTableHeadCell from '../../table-head-cell/src/table-head-cell.vue'
import tableColumn from "../../../mixins/table-column";

export default {
  name: "FastTableColumn",
  components: {FastTableHeadCell},
  mixins: [tableColumn],
  data() {
    return {}
  }
}
</script>

<style scoped lang="scss">

</style>