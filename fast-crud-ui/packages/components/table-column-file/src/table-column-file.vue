<template>
  <el-table-column class-name="fc-table-column"
                   :prop="prop"
                   :label="label"
                   :min-width="minWidth"
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
        <template v-if="status === 'normal' || config[column.property]['props']['editable'] === false">
          <slot v-bind:normal="{row, editRow, status, config, column, $index}">
            <fast-upload :style="{'height': tableStyle.bodyRowHeight}"
                         class="fc-fast-upload-file"
                         v-model="row[column.property]"
                         v-bind="config[column.property]['props']"
                         list-type="text"
                         :disabled="true"></fast-upload>
          </slot>
        </template>
        <slot v-bind="{row, editRow, status, config, column, $index}" v-else>
          <component :style="{'height': tableStyle.bodyRowHeight}"
                     class="fc-fast-upload-file"
                     :is="config[column.property]['component']"
                     v-model="editRow[column.property]"
                     :row="editRow" :col="column.property"
                     v-bind="config[column.property]['props']"
                     :ref="column.property + $index"
                     @change="(val) => handleChange(val, {row, editRow, status, column, config, $index})"></component>
        </slot>
      </slot>
    </template>
  </el-table-column>
</template>

<script>
import FastTableHeadCell from "../../table-head-cell/src/table-head-cell.vue";
import tableColumn from "../../../mixins/table-column";
import {isArray} from "../../../util/util";

export default {
  name: "FastTableColumnFile",
  methods: {isArray},
  components: {FastTableHeadCell},
  mixins: [tableColumn],
  props: {
    minWidth: {
      type: String,
      default: () => '180px'
    },
  },
  data() {
    return {}
  }
}
</script>

<style scoped lang="scss">
.fc-fast-upload-file {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
</style>