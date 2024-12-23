<template>
<!--  <valid-demo></valid-demo>-->
  <fast-table-demo></fast-table-demo>
</template>

<script>
import FastTableDemo from "./example/FastTableDemo.vue";
import ValidDemo from "@/example/ValidDemo.vue";

export default {
  name: "App",
  components: {ValidDemo, FastTableDemo},
  data() {
    return {}
  }
}
</script>

<style scoped lang="scss">
</style>