<template>
  <div class="demos">
<!--    <easy-demo></easy-demo>-->
    <full-demo></full-demo>

  </div>
</template>

<script>
import EasyDemo from "@/example/easy/EasyDemo.vue";
import FullDemo from "./example/full/FullDemo.vue";

export default {
  name: "App",
  components: {EasyDemo, FullDemo},
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