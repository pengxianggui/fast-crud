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
        <template v-if="!canEdit(status, config, column)">
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
          <fast-upload :style="{'height': tableStyle.bodyRowHeight}"
                       class="fc-fast-upload-file"
                       v-model="editRow[column.property]"
                       :row="editRow" :col="column.property"
                       v-bind="config[column.property]['props']"
                       :ref="column.property + $index"
                       @change="(val) => handleChange(val, {row, editRow, status, column, config, $index})"
                       @success="(componentScope) => $emit('success', componentScope, {row, editRow, status, column, config, $index})"
                       @fail="(componentScope) => $emit('fail', componentScope, {row, editRow, status, column, config, $index})"
                       @exceed="(componentScope) => $emit('exceed', componentScope, {row, editRow, status, column, config, $index})"></fast-upload>
        </slot>
      </slot>
    </template>
  </el-table-column>
</template>

<script>
import FastTableHeadCell from "../../table-head-cell/src/table-head-cell.vue";
import FastUpload from "../../upload/src/fast-upload.vue";
import tableColumn from "../../../mixins/table-column";

export default {
  name: "FastTableColumnFile",
  components: {FastTableHeadCell, FastUpload},
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