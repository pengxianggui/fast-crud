<template>
  <el-table-column v-bind="$attrs">
    <template v-slot:default="{row, column, $index}">
      <slot v-bind="{row, column, $index}">
<!--        <span>{{column}}</span>-->
        <span>{{ showLabel(row[column.property]) }}</span>
      </slot>
    </template>
  </el-table-column>
</template>

<script>

export default {
  name: "FastTableColumnSelect",
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