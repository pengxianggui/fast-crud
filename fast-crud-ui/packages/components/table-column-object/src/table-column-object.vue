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
          <fast-object-picker v-model="row['editRow'][column.property]"
                              v-bind="row['config'][column.property]['props']"
                              :table-option="getTableOption(row, column, $index)"
                              :pick-object="row['editRow']"
                              :ref="column.property + $index"
                              @change="(val) => handleChange(val, {row, column, $index})"
                              @blur="(event) => handleBlur(event, {row, column, $index})"
                              @focus="(event) => handleFocus(event, {row, column, $index})"
                              @input="(val) => handleInput(val, {row, column, $index})"
                              @clear="() => handleClear({row, column, $index})">
          </fast-object-picker>
        </slot>
      </slot>
    </template>
  </el-table-column>
</template>

<script>
import FastTableHeadCell from "../../table-head-cell/src/table-head-cell.vue";
import tableColumn from "../../../mixins/table-column";
import {FastTableOption} from "../../../index";
import {isFunction} from "../../../util/util";

export default {
  name: "FastTableColumnObject",
  components: {FastTableHeadCell},
  mixins: [tableColumn],
  props: {
    minWidth: {
      type: String,
      default: () => '100px'
    },
    tableOption: {
      type: FastTableOption | Function,
      required: true
    },
    showField: String, // 回显到input上的字段
    pickMap: Object, // 单选时, pick选择后回填到目标object上时，指导字段对应关系: key为pick的数据的字段名, value为pickObject中的字段名
    valueConvert: Function,
    beforeOpen: Function,
    title: String,
    multiple: {
      type: Boolean,
      default: () => false
    },
    placeholder: String,
    appendToBody: Boolean,
    clearable: {
      type: Boolean,
      default: () => true
    },
  },
  data() {
    return {}
  },
  methods: {
    getTableOption(fatRow, column, $index) {
      if (isFunction(this.tableOption)) {
        return this.tableOption(fatRow, column, $index);
      }
      return this.tableOption;
    }
  }
}
</script>

<style scoped>

</style>