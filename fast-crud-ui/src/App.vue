<template>
  <div class="demos">
    <!--  <valid-demo></valid-demo>-->
    <fast-table-demo></fast-table-demo>
  </div>
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
  .demos {
    height: 100%;
  }
</style>