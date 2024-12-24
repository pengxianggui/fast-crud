<template>
  <el-table-column :prop="prop" :label="label"
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
<!--        <template v-if="status === 'normal' || config[column.property]['props']['editable'] === false">-->
<!--          <slot v-bind:normal="{row, editRow, status, config, column, $index}">-->
<!--            <div class="file-list" :style="{'height': tableStyle.bodyRowHeight}">-->
<!--              <template v-if="isArray(row[column.property])">-->
<!--                <el-link v-for="url in row[column.property]" :href="url">{{ url }}</el-link>-->
<!--              </template>-->
<!--              <el-link :href="row[column.property]" v-else>-->
<!--                {{ row[column.property] }}-->
<!--              </el-link>-->
<!--            </div>-->
<!--          </slot>-->
<!--        </template>-->
<!--        <slot v-bind="{row, editRow, status, config, column, $index}">-->
          <component :style="{'height': tableStyle.bodyRowHeight}"
                     :is="config[column.property]['component']"
                     v-model="editRow[column.property]"
                     v-bind="config[column.property]['props']"
                     :disabled="status === 'normal' || config[column.property]['props']['editable'] === false"
                     v-on="config[column.property]['eventHandlers']"></component>
<!--        </slot>-->
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
.file-list {
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex-wrap: wrap;
  padding: 3px 0;

  .el-link {
    word-break: normal;
  }
}
</style>