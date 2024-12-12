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
  name: "FastTableColumnSwitch",
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