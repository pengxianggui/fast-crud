<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "App",
  data() {
    return {
      val: 'hello world'
    }
  }
})
</script>

<template>
  <el-input v-model="val"></el-input>
</template>

<style scoped>

</style>