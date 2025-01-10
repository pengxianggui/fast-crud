<template>
  <el-table-column class-name="fc-table-column"
                   :prop="prop"
                   :label="label"
                   :show-overflow-tooltip="showOverflowToolTip"
                   :min-width="minWidth"
                   v-bind="$attrs">
    <template v-slot:header="{column, $index}">
      <fast-table-head-cell class="fc-table-column-head-cell" :class="{'filter': filter}" :column="columnProp"
                            @click.native="headCellClick(column)">
        <slot v-bind:header="{column, $index}">
          <span>{{ column.label }}</span>
        </slot>
      </fast-table-head-cell>
    </template>

    <template v-slot:default="{row:fatRow, column, $index}">
      <slot v-bind:default="{...fatRow, column, $index}">
        <div v-if="!canEdit(fatRow, column, $index)">
          <slot v-bind:normal="{...fatRow, column, $index}">
            <span>{{ fatRow[fatRow.status === 'normal' ? 'row' : 'editRow'][column.property] }}</span>
          </slot>
        </div>
        <slot v-bind:edit="{...fatRow, column, $index}" v-else>
          <el-input v-model="fatRow['editRow'][column.property]"
                    v-bind="fatRow['config'][column.property]['props']"
                    :ref="column.property + $index"
                    @change="(val) => handleChange(val, {...fatRow, column, $index})"
                    @blur="(event) => handleBlur(event, {...fatRow, column, $index})"
                    @focus="(event) => handleFocus(event, {...fatRow, column, $index})"
                    @input="(val) => handleInput(val, {...fatRow, column, $index})"
                    @clear="() => handleClear({...fatRow, column, $index})"></el-input>
        </slot>
      </slot>
    </template>
  </el-table-column>
</template>

<script>
import FastTableHeadCell from "../../table-head-cell/src/table-head-cell.vue";
import tableColumn from "../../../mixins/table-column";

export default {
  name: "FastTableColumnTextarea",
  components: {FastTableHeadCell},
  mixins: [tableColumn],
  props: {
    minWidth: {
      type: String,
      default: () => '180px'
    }
  },
  data() {
    return {}
  }
}
</script>

<style scoped>

</style>