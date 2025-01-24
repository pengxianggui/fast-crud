<template>
  <el-table-column class-name="fc-table-column"
                   :prop="prop"
                   :label="label"
                   :min-width="minWidth"
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
        <template v-if="!canEdit(row, column, $index)">
          <slot name="normal" v-bind:row="row" v-bind:column="column" v-bind:$index="$index">
            <fast-upload :style="{'height': tableStyle.bodyRowHeight}"
                         class="fc-fast-upload-file"
                         v-model="row[row.status === 'normal' ? 'row' : 'editRow'][column.property]"
                         v-bind="row['config'][column.property]['props']"
                         list-type="text"
                         :disabled="true"></fast-upload>
          </slot>
        </template>
        <slot name="edit" v-bind:row="row" v-bind:column="column" v-bind:$index="$index" v-else>
          <fast-upload :style="{'height': tableStyle.bodyRowHeight}"
                       class="fc-fast-upload-file"
                       v-model="row['editRow'][column.property]"
                       :row="row['editRow']" :col="column.property"
                       v-bind="row['config'][column.property]['props']"
                       :ref="column.property + $index"
                       @change="(val) => handleChange(val, {row, column, $index})"
                       @success="(componentScope) => $emit('success', componentScope, {row, column, $index})"
                       @fail="(componentScope) => $emit('fail', componentScope, {row, column, $index})"
                       @exceed="(componentScope) => $emit('exceed', componentScope, {row, column, $index})"></fast-upload>
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