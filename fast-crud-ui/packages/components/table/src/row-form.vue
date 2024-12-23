<template>
  <div class="fc-table-edit-form-wrapper">
    <el-form ref="editForm"
             class="fc-table-edit-form"
             :model="formData"
             :rules="rules"
             :label-width="option.style.formLabelWidth">
      <el-form-item v-for="(conf, key) in config" :key="key"
                    :prop="key" :label="conf.label"
                    v-if="conf.props.editable !== false">
        <component :is="conf.component"
                   v-bind="conf.props"
                   v-model="formData[key]"
                   style="width: 100%"></component>
      </el-form-item>
    </el-form>
    <div class="fc-table-edit-form-btns">
      <el-button :size="option.style.size" type="primary" @click="submit">保存</el-button>
      <el-button :size="option.style.size" @click="cancel">取消</el-button>
    </div>
  </div>
</template>

<script>
import {Message} from 'element-ui';
import FastTableOption, {EditComponentConfig} from "../../../model";
import {isEmpty} from "../../../util/util";

export default {
  name: "row-form",
  props: {
    option: FastTableOption,
    config: EditComponentConfig,
    row: Object,
    type: String
  },
  data() {
    const ruleMap = {};
    for (const col in this.config) {
      const {props: {rules = []}} = this.config[col];
      if (!isEmpty(rules)) {
        ruleMap[col] = rules;
      }
    }
    ;
    const {editRow} = this.row
    return {
      rules: ruleMap,
      formData: {
        ...editRow
      }
    }
  },
  methods: {
    cancel() {
      this.$emit('cancel')
    },
    submit() {
      this.$refs['editForm'].validate().then((valid) => {
        if (!valid) {
          Message.warn('表单校验未通过! 请检查输入内容');
          return;
        }
        const fn = this.type === 'insert' ? this.option._insertRows : this.option._updateRows;
        const {row, config} = this.row;
        const fatRow = {
          row: row,
          editRow: this.formData,
          config: config
        }
        fn.call(this.option, [fatRow]).then(() => {
          this.$emit('ok')
        })
      })
    }
  }
}
</script>

<style scoped lang="scss">
.fc-table-edit-form-wrapper {
  .fc-table-edit-form {
    display: grid;
    grid-template-columns: 1fr 1fr;
    column-gap: 20px;
  }

  .fc-table-edit-form-btns {
    display: flex;
    justify-content: right;
  }
}
</style>