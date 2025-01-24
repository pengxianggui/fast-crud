<template>
  <el-table-column class-name="fc-table-column" :prop="prop" :label="label" :min-width="minWidth"
                   :show-overflow-tooltip="showOverflowTooltip"
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
        <div v-if="!canEdit(row, column, $index)">
          <slot name="normal" v-bind:row="row" v-bind:column="column" v-bind:$index="$index">
            <span>{{ showLabel(row, column) }}</span>
          </slot>
        </div>
        <slot name="edit" v-bind:row="row" v-bind:column="column" v-bind:$index="$index" v-else>
          <el-input v-model="row['editRow'][column.property]"
                    v-bind="row['config'][column.property]['props']"
                    :ref="column.property + $index"
                    @change="(val) => handleChange(val, {row, column, $index})"
                    @blur="(event) => handleBlur(event, {row, column, $index})"
                    @focus="(event) => handleFocus(event, {row, column, $index})"
                    @input="(val) => handleInput(val, {row, column, $index})"
                    @clear="() => handleClear({row, column, $index})">
          </el-input>
        </slot>
      </slot>
    </template>
  </el-table-column>
</template>

<script>
import FastTableHeadCell from "../../table-head-cell/src/table-head-cell.vue";
import tableColumn from "../../../mixins/table-column";

export default {
  name: "FastTableColumnInput",
  components: {FastTableHeadCell},
  mixins: [tableColumn],
  props: {
    minWidth: {
      type: String,
      default: () => '100px'
    }
  },
  data() {
    return {}
  }
}
</script>

<style scoped>

</style>