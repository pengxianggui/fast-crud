<template>
  <el-table-column class-name="fc-table-column" :prop="prop" :label="label" :min-width="minWidth"
                   :show-overflow-tooltip="showOverflowTooltip" v-bind="$attrs">
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
          <el-switch v-model="row['editRow'][column.property]"
                     v-bind="row['config'][column.property]['props']"
                     :ref="column.property + $index"
                     @change="(val) => handleChange(val, {row, column, $index})"></el-switch>
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
    showLabel(fatRow, column) {
      const options = [
        {label: this.inactiveText, value: this.inactiveValue},
        {label: this.activeText, value: this.activeValue}
      ]
      const {row, editRow, status} = fatRow;
      const {property} = column;
      let val;
      if (status === 'normal') {
        val = row[property];
      } else {
        val = editRow[property];
      }
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