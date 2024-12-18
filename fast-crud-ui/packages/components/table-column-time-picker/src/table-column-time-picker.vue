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

    <slot>
      <!-- TODO 实现图片展示&行内编辑 -->
    </slot>
  </el-table-column>
</template>

<script>
import FastTableHeadCell from "../../table-head-cell/src/table-head-cell.vue";
import tableColumn from "../../../mixins/table-column";

export default {
  name: "FastTableColumnTimePicker",
  components: {FastTableHeadCell},
  mixins: [tableColumn],
  data() {
    return {}
  }
}
</script>

<style scoped>

</style>