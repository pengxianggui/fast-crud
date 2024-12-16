<template>
  <el-form :inline="true" :label-width="formLabelWidth" class="fc-quick-filter-form">
    <el-form-item v-for="filter in visibleFilters"
                  :key="filter.col"
                  :prop="filter.col"
                  :label="filter.label + '：'"
                  class="fc-quick-filter-form-item">
      <component :is="filter.component" v-model="filter.val" v-bind="filter.props"/>
    </el-form-item>
    <el-button type="primary" :size="size" icon="el-icon-search" @click="search"></el-button>
    <el-button type="info" plain :size="size" icon="el-icon-refresh-left" @click="reset"></el-button>
    <el-button type="text" :size="size" @click="expColl">
      <span>{{ expand ? '收起' : '展开' }}</span>
      <i :class="expand ? 'el-icon-arrow-up': 'el-icon-arrow-down'"/>
    </el-button>
  </el-form>
</template>

<script>
export default {
  name: "quick-filter-form",
  props: {
    formLabelWidth: {
      type: String,
      default: () => 'auto'
    },
    filters: {
      type: Array,
      default: () => []
    },
    size: {
      type: String,
      default: () => 'small'
    }
  },
  data() {
    return {
      showNum: 3, // 收缩展示数量
      expand: false // 展开？
    }
  },
  computed: {
    visibleFilters() {
      return this.expand ? this.filters : this.filters.slice(0, this.showNum)
    }
  },
  methods: {
    search() {
      this.$emit('search')
    },
    reset() {
      this.filters.forEach((filter) => filter.reset());
      this.search()
    },
    expColl() {
      this.expand = !this.expand;
    }
  }
}
</script>

<style scoped lang="scss">
.fc-quick-filter-form-item {
  margin-bottom: 0 !important;
}
</style>