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

    <template v-slot:default="{row: fatRow, column, $index}">
      <slot v-bind:default="{...fatRow, column, $index}">
        <div v-if="!canEdit(fatRow, column, $index)">
          <slot v-bind:normal="{...fatRow, column, $index}">
            <span>{{ showLabel(fatRow['row'][column.property]) }}</span>
          </slot>
        </div>
        <slot v-bind:edit="{...fatRow, column, $index}" v-else>
          <fast-select v-model="fatRow['editRow'][column.property]"
                       v-bind="fatRow['config'][column.property]['props']"
                       :ref="column.property + $index"
                       @change="(val) => handleChange(val, {...fatRow, column, $index})"
                       @blur="(event) => handleBlur(event, {...fatRow, column, $index})"
                       @focus="(event) => handleFocus(event, {...fatRow, column, $index})"
                       @clear="() => handleClear({...fatRow, column, $index})"
                       @visible-change="(visible) => $emit('visible-change', visible, {...fatRow, column, $index})"
                       @remove-tag="(tagVal) => $emit('remove-tag', tagVal, {...fatRow, column, $index})"></fast-select>
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