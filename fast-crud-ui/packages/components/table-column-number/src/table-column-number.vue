<template>
  <el-table-column class-name="fc-table-column"
                   :prop="prop"
                   :label="label"
                   :min-width="minWidth"
                   :show-overflow-tooltip="showOverflowToolTip"
                   v-bind="$attrs">
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
        <div v-if="!canEdit(status, config, column)">
          <slot v-bind:normal="{row, editRow, status, config, column, $index}">
            <span>{{ row[column.property] }}</span>
          </slot>
        </div>
        <slot v-bind:edit="{row, editRow, status, config, column, $index}" v-else>
          <el-input-number v-model="editRow[column.property]"
                           v-bind="config[column.property]['props']"
                           :ref="column.property + $index"
                           @change="(val) => handleChange(val, {row, editRow, status, column, config, $index})"
                           @blur="(event) => handleBlur(event, {row, editRow, status, column, config, $index})"
                           @focus="(event) => handleFocus(event, {row, editRow, status, column, config, $index})"></el-input-number>
        </slot>
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
  props: {
    minWidth: {
      type: String,
      default: () => '90px'
    }
  },
  data() {
    return {}
  }
}
</script>

<style scoped>

</style>