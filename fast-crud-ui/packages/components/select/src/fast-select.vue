<template>
  <el-select v-model="modelValue" v-bind="$attrs" :size="size" :multiple="multiple"
             @input="handleInput"
             @change="handleChange"
             @clear="() => $emit('clear')"
             @focus="(event) => $emit('focus', event)"
             @blur="(event) => $emit('blur', event)"
             @visible-change="(visible) => $emit('visible-change', visible)"
             @remove-tag="(tagVal) => $emit('remove-tag', tagVal)">
    <el-option v-for="item in options" :key="item.value" :label="item[labelKey]" :value="item[valKey]"
               :disabled="disableVal.indexOf(item[valKey]) > -1"></el-option>
  </el-select>
</template>

<script>
export default {
  name: "fast-select",
  props: {
    value: {
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
    multiple: { // 多值时, value为数组
      type: Boolean,
      default: () => false
    },
    disableVal: {
      type: Array,
      default: () => []
    },
    size: String
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