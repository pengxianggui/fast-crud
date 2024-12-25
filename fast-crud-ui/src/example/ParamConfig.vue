<template>
  <el-form class="param" label-position="left" label-width="40px">
    <h3>行为配置</h3>
    <el-switch size="mini" v-model="params.editType" @change="(val) => updateOption('editType', val)"
               inactive-value="inline" inactive-color="#13ce66" inactive-text="行内编辑"
               active-value="form" active-color="#ff4949" active-text="表单编辑"></el-switch>
    <el-checkbox v-model="params.pageLoadable">允许加载分页</el-checkbox>
    <el-checkbox v-model="params.insertable" @change="(val) => updateOption('insertable', val)">允许新增</el-checkbox>
    <el-checkbox v-model="params.updatable" @change="(val) => updateOption('updatable', val)">允许更新</el-checkbox>
    <el-checkbox v-model="params.deletable" @change="(val) => updateOption('deletable', val)">允许删除</el-checkbox>
    <el-checkbox v-model="params.enableColumnFilter" @change="(val) => updateOption('enableColumnFilter', val)">
      允许表头动态筛选
    </el-checkbox>
    <el-checkbox v-model="params.enableMulti" @change="(val) => updateOption('enableMulti', val)">启用多选
    </el-checkbox>
    <el-checkbox v-model="params.enableDblClickEdit" @change="(val) => updateOption('enableDblClickEdit', val)">
      启用双击编辑
    </el-checkbox>

    <h3>外观配置</h3>
    <el-form-item label="尺寸">
      <fast-select size="mini" v-model="params.size" @change="(val) => updateOptionStyle('size', val)"
                   :options="[{label:'超小',value: 'mini'}, {label:'小',value: 'small'}, {label:'中等',value: 'medium'}, {label:'大', value: 'default'}]"></fast-select>
    </el-form-item>
    <el-form-item label="行高">
      <el-slider v-model="params.bodyRowHeight" :min="40" :max="100"
                 @change="(val) => updateOptionStyle('bodyRowHeight', val + 'px')"></el-slider>
    </el-form-item>

    <h3>钩子函数应用</h3>
    <el-checkbox v-model="params.loadSuccessTip">分页加载成功提示</el-checkbox>
    <el-checkbox v-model="params.customLoadFailTip">自定义加载失败提示</el-checkbox>
    <el-checkbox v-model="params.notDeleteLWL">不能删除利威尔(不弹窗)</el-checkbox>
    <el-checkbox v-model="params.notDeleteSS">不允许删除珊莎(弹窗后)</el-checkbox>
    <el-checkbox v-model="params.disableDefultDeleteSuccessWhenAL">删除艾伦时庆祝</el-checkbox>
    <el-checkbox v-model="params.customDeleteFailTip">自定义删除失败提示</el-checkbox>
    <el-checkbox v-model="params.disableUpdateAM">阿明不允许编辑</el-checkbox>
    <h3>方法</h3>
    <div>
      <el-button size="mini" @click="$refs['fastTable'].addRow()">插入一行</el-button>
      <el-button size="mini" @click="$refs['fastTable'].addForm()">弹窗新增</el-button>
    </div>
  </el-form>
</template>

<script>
import FastTableOption from "../../packages/model";

export default {
  name: "ParamConfig",
  props: {
    params: Object,
    option: FastTableOption
  },
  data() {
    return {
    }
  },
  methods: {
    updateOption(key, val) {
      this.option[key] = val;
    },
    updateOptionStyle(key, val) {
      this.option.style[key] = val;
      if (key === 'size') {
        this.$emit('refresh')
      }
    }
  }
}
</script>

<style scoped lang="scss">

</style>