<template>
  <div>
    <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange">全选</el-checkbox>
    <el-checkbox-group class="fc-checkbox-group" v-model="modelValue" @change="handleChange">
      <el-checkbox v-for="item in options" :label="item.value" :key="item.value">{{ item.label }}</el-checkbox>
    </el-checkbox-group>
  </div>
</template>

<script>
export default {
  name: "fast-checkbox-group",
  props: {
    value: {
      type: Array,
      required: true
    },
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
  computed: {
    // TODO 修复当给默认值时，modelValue值正确，但是UI没勾上
    modelValue: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    },
    isIndeterminate: {
      get() {
        return this.modelValue.length > 0 && this.modelValue.length < this.options.length
      },
      set(val) {
      }
    },
    checkAll: {
      get() {
        return this.modelValue.length === this.options.length
      },
      set(val) {
      }
    }
  },
  data() {
    return {
      // isIndeterminate: true,
      // checkAll: false,
    }
  },
  methods: {
    handleCheckAllChange(val) {
      this.modelValue = val ? this.options.map(item => item.value) : [];
      this.isIndeterminate = false;
    },
    handleChange(val) {
      this.modelValue = val
      this.$emit('change', val)
    }
  }
}
</script>

<style scoped lang="scss">
.fc-checkbox-group {
  display: inline-block;
  margin-left: 10px;
}
</style>