<template>
  <div>
    <el-checkbox :indeterminate="isIndeterminate" v-model="checkAll" @change="handleCheckAllChange" v-if="showChoseAll">全选</el-checkbox>
    <el-checkbox-group class="fc-checkbox-group" v-model="modelValue" @change="handleChange">
      <el-checkbox v-for="item in options" :label="item[valKey]" :key="item[valKey]"
                   :disabled="disableVal.indexOf(item[valKey]) > -1">{{ item[labelKey] }}</el-checkbox>
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
    },
    showChoseAll: {
      type: Boolean,
      default: () => true
    },
    disableVal: {
      type: Array,
      default: () => []
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
    }
  },
  methods: {
    handleCheckAllChange(val) {
      this.modelValue = val ? this.options.filter(item => this.disableVal.indexOf(item[this.valKey]) === -1)
          .map(item => item[this.valKey]) : [];
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