<template>
  <el-table-column :prop="prop" :label="label" v-bind="$attrs">
    <template v-slot:header="{column, $index}">
      <fast-table-head-cell class="fc-table-column-head-cell" :class="{'filter': filter}" :column="columnProp"
                            @click.native="headCellClick(column)">
        <slot v-bind:header="{column, $index}">
          <span>{{ column.label }}</span>
        </slot>
      </fast-table-head-cell>
    </template>

    <slot>
      <!-- TODO 实现行内编辑的关键编码位置 -->
    </slot>
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