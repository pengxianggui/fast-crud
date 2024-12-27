<template>
  <el-table-column class-name="fc-table-column" :prop="prop" :label="label" :min-width="minWidth"
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
        <div v-if="status === 'normal' || config[column.property]['props']['editable'] === false">
          <slot v-bind:normal="{row, editRow, status, config, column, $index}">
            <span>{{ showLabel(row[column.property]) }}</span>
          </slot>
        </div>
        <slot v-bind:edit="{row, editRow, status, config, column, $index}" v-else>
          <fast-select v-model="editRow[column.property]"
                       v-bind="config[column.property]['props']"
                       :ref="column.property + $index"
                       @change="(val) => handleChange(val, {row, editRow, status, column, config, $index})"
                       @blur="(event) => changeBlur(event, {row, editRow, status, column, config, $index})"
                       @focus="(event) => changeFocus(event, {row, editRow, status, column, config, $index})"
                       @clear="() => handleClear({row, editRow, status, column, config, $index})"
                       @visible-change="(visible) => $emit('visible-change', visible, {row, editRow, status, column, config, $index})"
                       @remove-tag="(tagVal) => $emit('remove-tag', tagVal, {row, editRow, status, column, config, $index})"></fast-select>
        </slot>
      </slot>
    </template>
  </el-table-column>
</template>

<script>

import FastTableHeadCell from "../../table-head-cell/src/table-head-cell.vue";
import tableColumn from "../../../mixins/table-column";
import FastSelect from "../../select/src/fast-select.vue";

export default {
  name: "FastTableColumnSelect",
  components: {FastTableHeadCell, FastSelect},
  mixins: [tableColumn],
  props: {
    options: {
      type: Array,
      required: true
    },
    labelKey: {
      type: String,
      default: () => "label"
    },
    valKey: {
      type: String,
      default: () => "value"
    },
    minWidth: {
      type: String,
      default: () => '90px'
    }
  },
  data() {
    return {}
  },
  methods: {
    showLabel(val) {
      if (this.options) {
        const option = this.options.find(item => item[this.valKey] === val);
        if (option) {
          return option[this.labelKey]
        }
      }
      return val;
    }
  }
}
</script>

<style scoped>

</style>