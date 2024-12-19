<template>
  <el-table-column :prop="prop" :label="label" :show-overflow-tooltip="showOverflowToolTip" v-bind="$attrs">
    <template v-slot:header="{column, $index}">
      <fast-table-head-cell class="fc-table-column-head-cell" :class="{'filter': filter}" :column="columnProp"
                            @click.native="headCellClick(column)">
        <slot v-bind:header="{column, $index}">
          <span>{{ column.label }}</span>
        </slot>
      </fast-table-head-cell>
    </template>

    <template v-slot:default="{row: {row, editRow, status, config}, column, $index}">
      <slot v-bind:default="{row, editRow, status, config, column, $index}">
        <div v-if="status === 'normal'">{{row[column.property]}}</div>
        <component :is="config[column.property]['component']"
                   v-model="editRow[column.property]" v-bind="config[column.property]['props']" v-else></component>
      </slot>
    </template>
  </el-table-column>
</template>

<script>
import FastTableHeadCell from "../../table-head-cell/src/table-head-cell.vue";
import tableColumn from "../../../mixins/table-column";

export default {
  name: "FastTableColumnNumber",
  components: {FastTableHeadCell},
  mixins: [tableColumn],
  data() {
    return {}
  }
}
</script>

<style scoped>

</style>