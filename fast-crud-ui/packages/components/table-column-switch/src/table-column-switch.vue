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