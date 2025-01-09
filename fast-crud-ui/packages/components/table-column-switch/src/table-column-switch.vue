<template>
  <el-table-column class-name="fc-table-column" :prop="prop" :label="label" :min-width="minWidth"
                   :show-overflow-tooltip="showOverflowToolTip" v-bind="$attrs">
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
          <el-switch v-model="fatRow['editRow'][column.property]"
                     v-bind="fatRow['config'][column.property]['props']"
                     :ref="column.property + $index"
                     @change="(val) => handleChange(val, {...fatRow, column, $index})"></el-switch>
        </slot>
      </slot>
    </template>
  </el-table-column>
</template>

<script>
import FastTableHeadCell from "../../table-head-cell/src/table-head-cell.vue";
import tableColumn from "../../../mixins/table-column";

export default {
  name: "FastTableColumnSwitch",
  components: {FastTableHeadCell},
  mixins: [tableColumn],
  props: {
    activeValue: {
      type: String | Number | Boolean,
      default: () => true
    },
    inactiveValue: {
      type: String | Number | Boolean,
      default: () => false
    },
    activeText: {
      type: String,
      default: () => '是'
    },
    inactiveText: {
      type: String,
      default: () => '否'
    },
    minWidth: {
      type: String,
      default: () => '100px'
    }
  },
  data() {
    return {}
  },
  methods: {
    showLabel(val) {
      const options = [
        {label: this.inactiveText, value: this.inactiveValue},
        {label: this.activeText, value: this.activeValue}
      ]
      if (options) {
        const option = options.find(item => item.value === val);
        if (option) {
          return option.label
        }
      }
      return val;
    }
  }
}
</script>

<style scoped>

</style>