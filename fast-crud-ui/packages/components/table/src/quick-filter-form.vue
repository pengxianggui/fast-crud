<template>
  <el-form :inline="true" :label-width="formLabelWidth" class="fc-quick-filter-form">
    <el-form-item v-for="filter in visibleFilters"
                  :key="filter.col"
                  :prop="filter.col"
                  :label="filter.label + '：'"
                  :class="filter.props && filter.props.hasOwnProperty('quick-filter-block') ? 'fc-quick-filter-form-item-block': ''"
                  class="fc-quick-filter-form-item">
      <component :is="filter.component" v-model="filter.val" v-bind="filter.props"/>
    </el-form-item>
    <div class="fc-quick-filter-form-btns">
      <el-button type="primary" :size="size" icon="el-icon-search" @click="search"></el-button>
      <el-button type="info" plain :size="size" icon="el-icon-refresh-left" @click="reset"></el-button>
      <el-button type="text" :size="size" @click="expColl" v-if="filters.length > 3">
        <span>{{ expand ? '收起' : '展开' }}</span>
        <i :class="expand ? 'el-icon-arrow-up': 'el-icon-arrow-down'"/>
      </el-button>
    </div>
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
      const {expand, filters = []} = this;
      // 确保独占一行的快筛项排前面
      filters.sort((a, b) => {
        const {props: propsA} = a;
        const {props: propsB} = b;
        if (propsA.hasOwnProperty('quick-filter-block') && !propsB.hasOwnProperty('quick-filter-block')) {
          return -1
        } else if (!propsA.hasOwnProperty('quick-filter-block') && propsB.hasOwnProperty('quick-filter-block')) {
          return 1
        }
        return 0;
      })
      return expand ? filters : filters.slice(0, this.showNum)
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

.fc-quick-filter-form-item-block {
  width: 100%;
}

.fc-quick-filter-form-btns {
  margin-left: 10px;
}
</style>