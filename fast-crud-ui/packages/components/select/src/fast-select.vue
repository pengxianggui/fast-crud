<template>
  <el-select v-model="modelValue" v-bind="$attrs" @input="handleInput" @change="handleChange">
    <el-option v-for="item in options" :key="item.value" :label="item.label" :value="item.value"></el-option>
  </el-select>
</template>

<script>
export default {
  name: "fast-select",
  props: {
    value: {
      type: String | Number,
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
    },
    multiple: { // TODO 兑现，存值采用逗号分隔 OR 数组？
      type: Boolean,
      default: () => false
    }
  },
  computed: {
    modelValue: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    }
  },
  methods: {
    handleInput(val) {
      this.modelValue = val
    },
    handleChange(val) {
      this.modelValue = val
      this.$emit('change', val)
    }
  }
}
</script>

<style scoped>

</style>