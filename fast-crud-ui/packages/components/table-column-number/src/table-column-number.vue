<template>
  <el-table-column v-bind="$attrs">
    <slot></slot>
  </el-table-column>
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "FastTableColumnNumber",
  data() {
    return {
    }
  }
})
</script>

<style scoped>

</style>