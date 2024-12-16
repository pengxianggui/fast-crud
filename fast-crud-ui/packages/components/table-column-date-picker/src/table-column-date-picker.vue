<template>
  <el-table-column :prop="prop" :label="label" :min-width="minWidth" v-bind="$attrs">
    <template v-slot:header="{column, $index}">
      <fast-table-head-cell class="fc-table-column-head-cell" :class="{'filter': filter}" :column="columnProp"
                            @click.native="headCellClick(column)">
        <slot v-bind:header="{column, $index}">
          <span>{{ column.label }}</span>
        </slot>
      </fast-table-head-cell>
    </template>

    <template v-slot:default="{row, column, $index}">
      <slot v-bind:default="{row, column, $index}">
        <!-- TODO 实现行内编辑的关键编码位置 -->
        <span>{{ row[column.property] | dateFormat(valueFormat) }}</span>
      </slot>
    </template>
  </el-table-column>
</template>

<script>
import FastTableHeadCell from '../../table-head-cell/src/table-head-cell.vue'
import tableColumn from "../../../mixins/table-column";
import {dateFormat} from "../../../filters";

export default {
  name: "FastTableColumnDatePicker",
  components: {FastTableHeadCell},
  mixins: [tableColumn],
  filters: {dateFormat},
  props: {
    valueFormat: {
      type: String,
      default: () => 'yyyy-MM-dd'
    },
    minWidth: {
      type: String,
      default: () => '120px'
    }
  },
  data() {
    return {}
  }
}
</script>

<style scoped>

</style>