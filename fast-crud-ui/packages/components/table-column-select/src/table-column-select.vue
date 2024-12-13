<template>
  <el-table-column v-bind="$attrs">
    <template v-slot:header="{column, $index}">
      <fast-table-head-cell class="fc-table-column-head-cell" :class="{'filter': filter}" :column="column"
                            @click.native="headCellClick(column)">
        <slot v-bind:header="{column, $index}">
          <span>{{ column.label }}</span>
        </slot>
      </fast-table-head-cell>
    </template>

    <template v-slot:default="{row, column, $index}">
      <slot v-bind="{row, column, $index}">
        <span>{{ showLabel(row[column.property]) }}</span>
        <!-- TODO 实现行内编辑的关键编码位置 -->
      </slot>
    </template>
  </el-table-column>
</template>

<script>

import FastTableHeadCell from "../../table-head-cell/src/table-head-cell.vue";
import tableColumn from "../../../mixins/table-column";

export default {
  name: "FastTableColumnSelect",
  components: {FastTableHeadCell},
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