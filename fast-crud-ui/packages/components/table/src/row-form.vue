<template>
  <div class="fc-table-edit-form-wrapper">
    <el-form ref="editForm"
             class="fc-table-edit-form"
             :model="formData"
             :rules="rules"
             :label-width="option.style.formLabelWidth">
      <el-row v-for="row in localLayout" :key="row.col">
        <el-col v-for="(span, col) in row" :span="span" :key="col">
          <el-form-item :prop="col"
                        :label="config[col].label"
                        :key="col"
                        v-if="canEdit(col)">
            <component :is="config[col].component"
                       v-bind="config[col].props"
                       v-model="formData[col]"
                       style="width: 100%"></component>
          </el-form-item>
          <span v-else>&nbsp;</span>
        </el-col>
      </el-row>
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
import {colEditable} from "./util";

export default {
  name: "row-form",
  props: {
    option: FastTableOption,
    config: EditComponentConfig,
    row: Object,
    type: String,
    layout: String
  },
  data() {
    const formData = this.row.editRow
    const ruleMap = {};
    for (const col in this.config) {
      const {component, props: {rules = []}} = this.config[col];
      if (!isEmpty(rules)) {
        ruleMap[col] = rules;
      }
      if (component === 'fast-object-picker') { // 对于FastObjectPicker需要特别把formData传进去
        this.config[col].props.pickObject = formData;
      }
    }
    return {
      rules: ruleMap,
      formData: formData
    }
  },
  computed: {
    localLayout() {
      let {layout, config} = this;
      const colArr = Object.keys(config);
      if (isEmpty(layout)) {
        layout = colArr.join(",");
      }
      return layout.split(",").map(row => {
        const rowObj = {};
        const cols = row.split("|");
        const perSpan = Math.floor(24 / cols.length);
        for (let i = 0; i < cols.length; i++) {
          const key = cols[i].trim();
          if (rowObj.hasOwnProperty(key)) {
            rowObj[key] += perSpan;
          } else {
            rowObj[key] = perSpan;
          }
        }
        return rowObj;
      })
    }
  },
  methods: {
    /**
     * 是否展示编辑模式
     * @param col 列
     * @param config 列配置
     * @returns {boolean}
     */
    canEdit(col) {
      return colEditable.call(this.option.context, {...this.row, status: this.type}, col);
    },
    cancel() {
      const {context, beforeCancel} = this.option;
      beforeCancel.call(context, {
        fatRows: [this.row],
        rows: [this.row.row],
        status: this.type
      }).then(() => {
        this.$emit('cancel')
      }).catch(() => {
        // 不允许取消
      })
    },
    submit() {
      this.$refs['editForm'].validate().then((valid) => {
        if (!valid) {
          Message.warn('表单校验未通过! 请检查输入内容');
          return;
        }
        const fn = this.type === 'insert' ? this.option._insertRows : this.option._updateRows;
        fn.call(this.option, [this.row]).then(() => {
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
    //display: grid;
    //grid-template-columns: 1fr 1fr;
    //column-gap: 20px;
  }

  .fc-table-edit-form-btns {
    display: flex;
    justify-content: right;
  }
}
</style>