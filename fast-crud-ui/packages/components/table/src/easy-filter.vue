<template>
  <div class="fc-easy-filter">
    <fast-select class="fc-easy-filter-column" :options="filters" v-model="activeFilterCol" label-key="label"
                 val-key="col"
                 :size="size"
                 :filterable="true"
                 @change="changeField"></fast-select>
    <component class="fc-easy-filter-value" :is="activeFilter.component" v-model="activeFilter.val"
               v-bind="activeFilter.props"
               :size="size"
               @clear="handleClear" @keydown.enter.native="handleEnter"/>
    <el-button type="primary" class="fc-easy-filter-btn" :size="size" icon="el-icon-search" @click="search"></el-button>
  </div>
</template>

<script>
import FastSelect from "../../select/src/fast-select.vue";

export default {
  name: "easy-filter",
  components: {FastSelect},
  props: {
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
    const mainFilter = this.filters[0]
    return {
      activeFilterCol: mainFilter.col,
      activeFilter: mainFilter
    }
  },
  methods: {
    changeField() {
      this.activeFilter = this.filters.find(filter => filter.col === this.activeFilterCol)
      this.filters.map(filter => {
        filter.disabled = (filter.col !== this.activeFilterCol) // 保证只有activeFilter生效， 这样就不用清理切换之前的控件值了，使用体验更好
      })
    },
    handleClear() {
      this.$nextTick(() => {
        this.search()
      })
    },
    handleEnter() {
      this.$nextTick(() => {
        this.search()
      })
    },
    search() {
      this.$emit('search')
    }
  }
}
</script>

<style scoped lang="scss">
.fc-easy-filter {
  display: flex;

  .fc-easy-filter-column {
    width: 120px;
  }

  .fc-easy-filter-value {
    width: 280px;
  }

  .fc-easy-filter-btn {
    margin-left: 10px;
  }
}
</style>