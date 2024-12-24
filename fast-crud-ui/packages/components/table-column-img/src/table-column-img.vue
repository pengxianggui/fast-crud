<template>
  <el-table-column :prop="prop" :label="label" :min-width="minWidth" v-bind="$attrs">
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
            <div class="img-list">
              <template v-if="isArray(row[column.property])">
                <img v-for="url in row[column.property]" :style="{'height': tableStyle.bodyRowHeight}" :src="url"/>
              </template>
              <img :style="{'height': tableStyle.bodyRowHeight}" :src="row[column.property]" v-else/>
            </div>
          </slot>
        </template>
        <slot v-bind:edit="{row, editRow, status, config, column, $index}" v-else>
          <component :style="{'height': tableStyle.bodyRowHeight}"
                     :is="config[column.property]['component']"
                     v-model="editRow[column.property]"
                     v-bind="config[column.property]['props']"
                     v-on="config[column.property]['eventHandlers']"></component>
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
  name: "FastTableColumnImg",
  methods: {isArray},
  components: {FastTableHeadCell},
  mixins: [tableColumn],
  props: {
    minWidth: {
      type: String,
      default: () => '100px'
    },
  },
  data() {
    return {}
  }
}
</script>

<style scoped lang="scss">
.img-list {
  display: flex;
  flex-wrap: wrap;
  padding: 3px 0;
}
img {
  object-fit: cover;
}
</style>