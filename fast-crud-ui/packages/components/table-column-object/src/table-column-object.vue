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

    <template v-slot:default="{row:fatRow, column, $index}">
      <slot v-bind:default="{...fatRow, column, $index}">
        <div v-if="!canEdit(fatRow, column, $index)">
          <slot v-bind:normal="{...fatRow, column, $index}">
            <span>{{ fatRow[fatRow.status === 'normal' ? 'row' : 'editRow'][column.property] }}</span>
          </slot>
        </div>
        <slot v-bind:edit="{...fatRow, column, $index}" v-else>
          <fast-object-picker v-model="fatRow['editRow'][column.property]"
                              v-bind="fatRow['config'][column.property]['props']"
                              :table-option="getTableOption(fatRow, column, $index)"
                              :show-field="showField"
                              :pick-object="fatRow['editRow']"
                              :pick-map="pickMap"
                              :value-covert="valueConvert"
                              :before-open="beforeOpen"
                              :title="title"
                              :multiple="multiple"
                              :placeholder="placeholder"
                              :append-to-body="appendToBody"
                              :clearable="clearable"
                              :ref="column.property + $index"
                              @change="(val) => handleChange(val, {...fatRow, column, $index})"
                              @blur="(event) => handleBlur(event, {...fatRow, column, $index})"
                              @focus="(event) => handleFocus(event, {...fatRow, column, $index})"
                              @input="(val) => handleInput(val, {...fatRow, column, $index})"
                              @clear="() => handleClear({...fatRow, column, $index})">
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