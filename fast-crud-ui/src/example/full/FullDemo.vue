<template>
  <div class="demo">
    <div class="param">
      <h5>行为配置</h5>
      <el-switch size="mini" v-model="params.editType" @change="(val) => updateOption('editType', val)"
                 inactive-value="inline" inactive-color="#13ce66" inactive-text="行内编辑"
                 active-value="form" active-color="#ff4949" active-text="表单编辑"></el-switch>
      <el-checkbox v-model="params.pageLoadable">允许加载分页</el-checkbox>
      <el-checkbox v-model="params.insertable" @change="(val) => updateOption('insertable', val)">允许新增
      </el-checkbox>
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

      <h5>外观配置</h5>
      <div class="line">
        <label>尺寸</label>
        <fast-select class="comp" size="mini" v-model="params.size" @change="(val) => updateOptionStyle('size', val)"
                     :options="[{label:'超小',value: 'mini'}, {label:'小',value: 'small'}, {label:'中等',value: 'medium'}, {label:'大', value: 'default'}]"></fast-select>
      </div>
      <div class="line">
        <label>行高</label>
        <el-slider class="comp" v-model="params.bodyRowHeight" :min="40" :max="100"
                   @change="(val) => updateOptionStyle('bodyRowHeight', val + 'px')"></el-slider>
      </div>
      <el-checkbox v-model="params.fixedAvatar">固定头像列</el-checkbox>
      <el-checkbox v-model="params.flexHeight" @change="(val) => updateOptionStyle('flexHeight', val)">
        表格高度弹性自适应
      </el-checkbox>

      <h5>钩子函数应用</h5>
      <el-checkbox v-model="params.loadSuccessTip">分页加载成功提示</el-checkbox>
      <el-checkbox v-model="params.customLoadFailTip">自定义加载失败提示</el-checkbox>
      <el-checkbox v-model="params.notDelete">不能删除诸葛亮(不弹窗)</el-checkbox>
      <el-checkbox v-model="params.notDeleteAfterConfirm">不允许删除赵云(弹窗后)</el-checkbox>
      <el-checkbox v-model="params.disableDefultDeleteSuccessTip">删除吕蒙时庆祝</el-checkbox>
      <el-checkbox v-model="params.customDeleteFailTip">自定义删除失败提示</el-checkbox>
      <el-checkbox v-model="params.customInsertSuccessTip">自定义插入成功提示</el-checkbox>
      <el-checkbox v-model="params.customInsertFailTip">自定义插入失败提示</el-checkbox>
      <el-checkbox v-model="params.disableUpdate">曹操不允许编辑</el-checkbox>
      <el-checkbox v-model="params.disableUpdateToHd">名字不允许改为皇帝</el-checkbox>
      <el-checkbox v-model="params.disableInsertSmy">不允许添加司马懿</el-checkbox>
      <el-checkbox v-model="params.disableCancelWhenUpdate">更新时不允许取消</el-checkbox>

      <h5>事件</h5>
      <el-checkbox v-model="params.autoSetGraduatedWhenAgeChange">超50岁自动毕业</el-checkbox>
      <el-checkbox v-model="params.noEditLuckWhenAgeGT35">超35岁不允许编辑幸运时刻</el-checkbox>
    </div>

    <my-table ref="myTable" :params="params"></my-table>
  </div>
</template>

<script>
import MyTable from "./MyTable.vue";

export default {
  name: "FastTableDemo",
  components: {MyTable},
  data() {
    return {
      params: {
        editType: 'inline',
        // 允许分页加载
        pageLoadable: true,
        // 允许新增
        insertable: true,
        // 允许编辑
        updatable: true,
        // 允许删除
        deletable: true,
        // 允许动态筛选
        enableColumnFilter: true,
        // 启用多选
        enableMulti: true,
        // 启用双击编辑
        enableDblClickEdit: true,
        // 默认尺寸
        size: 'medium',
        bodyRowHeight: 45,
        // 表格高度弹性自适应
        flexHeight: true,
        fixedAvatar: false,
        // 加载成功时提示
        loadSuccessTip: false,
        // 自定义加载失败提示
        customLoadFailTip: true,
        // 自定义插入成功提示
        customInsertSuccessTip: false,
        // 自定义插入失败提示
        customInsertFailTip: false,
        // 不允许删除利威尔
        notDelete: true,
        // 不允许删除珊莎
        notDeleteAfterConfirm: true,
        // 自定义删除失败提示
        customDeleteFailTip: true,
        // 当删除对象包含艾伦时禁用默认删除成功提示
        disableDefultDeleteSuccessTip: true,
        // 阿明不允许编辑
        disableUpdate: true,
        // 名字不允许改为张三
        disableUpdateToHd: true,
        // 不允许添加李四
        disableInsertSmy: true,
        // 更新行时不允许取消
        disableCancelWhenUpdate: false,
        // 年龄大于50自动毕业
        autoSetGraduatedWhenAgeChange: true,
        // 年龄大于35, 不可编辑幸运时刻
        noEditLuckWhenAgeGT35: true,
      }
    }
  },
  methods: {
    updateOption(key, val) {
      this.$refs.myTable.updateOption(key, val)
    },
    updateOptionStyle(key, val) {
      this.$refs.myTable.updateOptionStyle(key, val)
    }
  }
}
</script>

<style scoped lang="scss">
.demo {
  height: 100%;
  display: flex;

  .param {
    width: 200px;
    min-width: 200px;
    padding: 0 20px;
    overflow: auto;

    & > * {
      display: block;
      margin-bottom: 5px;
    }

    .methods {
      display: grid;
      grid-template-columns: 1fr 1fr;
      grid-gap: 5px;
    }

    .el-button {
      margin: 0;
    }

    h5 {
      margin-top: 13px;
    }

    .line {
      display: flex;
      align-items: center;

      label {
        font-size: 14px;
        width: 60px;
      }

      .comp {
        width: 100%;
      }
    }
  }
}
</style>