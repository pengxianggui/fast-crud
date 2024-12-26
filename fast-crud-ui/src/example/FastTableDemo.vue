<template>
  <div class="demo">
    <div class="param">
      <el-form label-position="left" label-width="40px">
        <h3>行为配置</h3>
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
        <el-checkbox v-model="params.customInsertSuccessTip">自定义插入成功提示</el-checkbox>
        <el-checkbox v-model="params.customInsertFailTip">自定义插入失败提示</el-checkbox>
        <el-checkbox v-model="params.disableUpdateAM">阿明不允许编辑</el-checkbox>
        <el-checkbox v-model="params.disableUpdateToZs">名字不允许改为张三</el-checkbox>
        <el-checkbox v-model="params.disableInsertLs">不允许添加李四</el-checkbox>
        <h3>方法</h3>
        <div>
          <el-button size="mini" @click="$refs['fastTable'].addRow()">插入一行</el-button>
          <el-button size="mini" @click="$refs['fastTable'].addForm()">弹窗新增</el-button>
        </div>
      </el-form>
    </div>

    <fast-table ref="fastTable" class="el-card" :option="tableOption">
      <fast-table-column label="ID" prop="id"/>
      <fast-table-column-img label="头像" prop="avatarUrl" required/>
      <fast-table-column-input label="姓名" prop="name" first-filter :quick-filter="true" required/>
      <fast-table-column-number label="年龄" prop="age" required
                                :rules="[{type: 'number', min: 18, max: 60, message: '年龄必须在[18,60]之间'}]"/>
      <fast-table-column-select label="性别" prop="sex" :options="sexOptions" :quick-filter="true" required/>
      <fast-table-column-select label="爱好" prop="hobby" :options="hobbyOptions"
                                :quick-filter="true" quick-filter-block quick-filter-checkbox
                                val-key="code" label-key="name"
                                :default-val__q="['1', '2', '3', '4', '5']"
                                :disable-val="['6']"
                                required/>
      <fast-table-column-textarea label="地址" prop="address" required/>
      <fast-table-column-switch label="已毕业" prop="graduated" :quick-filter="true" :default-val="false" required/>
      <fast-table-column-time-picker label="幸运时刻" prop="luckTime" required/>
      <fast-table-column-date-picker label="生日" prop="birthday" :picker-options="pickerOptionsE" required/>
      <fast-table-column-file label="简历" prop="resumeUrl" :show-overflow-tool-tip="false"/>
      <fast-table-column-date-picker label="创建时间" prop="createTime" :picker-options__q="pickerOptionsQ"
                                     type="datetime"
                                     :quick-filter="false" :default-val__q="defaultQueryOfCreatedTime"
                                     value-format__q="yyyy-MM-dd HH:mm:ss"
                                     value-format__e="yyyy-MM-ddTHH:mm:ss"
                                     :default-time="['00:00:00', '23:59:59']"
                                     :editable="false"/>
      <el-table-column label="操作">
        <template slot-scope="scope">
          <el-button type="text" size="small" @click="edit(scope)">编辑</el-button>
        </template>
      </el-table-column>
    </fast-table>
  </div>
</template>

<script>
import FastTableOption from "../../packages/model";
import {Message} from 'element-ui';
import staticDict from './dict'

export default {
  name: "FastTableDemo",
  data() {
    const now = new Date();
    const monthAgo = new Date();
    monthAgo.setTime(monthAgo.getTime() - 3600 * 1000 * 24 * 30);
    const defaultEditType = 'inline';
    const defaultSize = 'medium';
    const defaultRowHeight = 45;
    return {
      tableOption: new FastTableOption({
        context: this, // important! 否则钩子函数里无法获取当当前组件实例上下文
        title: '学生管理',
        module: 'student', // 配置了这个, 默认分页接口就是: /student/page, 新增接口就是: /student/insert, 其它同理
        enableDblClickEdit: true,
        enableMulti: true,
        enableColumnFilter: true,
        lazyLoad: false,
        editType: defaultEditType, // 默认inline
        insertable: true,
        updatable: true,
        deletable: true,
        sortField: 'createTime',
        sortDesc: true,
        pagination: {
          size: 5,
          "page-sizes": [5, 10, 20, 50, 100]
        },
        style: {
          size: defaultSize, // mini,small,medium,default
          bodyRowHeight: defaultRowHeight + 'px',
          formLabelWidth: 'auto', // 默认为auto
          formLayout: 'avatarUrl|name, age|graduated, sex|hobby, address, birthday|luckTime, resumeUrl, createTime' // 弹窗表单布局设置
        },
        beforeLoad({query}) {
          if (this.params.pageLoadable) {
            return Promise.resolve();
          }
          Message.warning('未勾选【允许加载分页】, 不会分页请求');
          return Promise.reject()
        },
        loadSuccess({query, data, res}) {
          if (this.params.loadSuccessTip) {
            Message.success('分页加载成功!');
          }
          return Promise.resolve(data);
        },
        loadFail({query, error}) {
          if (this.params.customLoadFailTip) {
            Message.error('哦豁, 分页加载失败了:' + JSON.stringify(error));
            return Promise.reject();
          }
          return Promise.resolve(); // 可以通过reject覆盖默认的加载失败提示
        },
        beforeInsert({fatRows, rows, editRows}) {
          if (editRows.findIndex(r => r.name === '李四') > -1 && this.params.disableInsertLs) {
            Message.warning('你已勾选【不允许添加李四】');
            return Promise.reject();
          }
          return Promise.resolve();
        },
        insertSuccess({fatRows, rows, editRows, res}) {
          if (this.params.customInsertSuccessTip) {
            Message.success('啧啧啧, 插入成功啦!');
            return Promise.reject(); // 取消内置的插入成功提示
          }
          return Promise.resolve();
        },
        insertFail({fatRows, rows, editRows, error}) {
          if (this.params.customInsertFailTip) {
            Message.error('哦豁, 插入失败了!');
            return Promise.reject();
          }
          return Promise.resolve();
        },
        beforeToUpdate({fatRows, rows}) {
          if (rows.findIndex(r => r.name === '阿明') > -1 && this.params.disableUpdateAM) {
            Message.warning("你已勾选【阿明不允许编辑】")
            return Promise.reject();
          }
          return Promise.resolve();
        },
        beforeUpdate({fatRows, rows, editRows}) {
          if (editRows.findIndex(r => r.name === '张三') > -1 && this.params.disableUpdateToZs) {
            Message.warning('你已勾选【名字不允许改为张三】');
            return Promise.reject();
          }
          return Promise.resolve();
        },
        updateSuccess({fatRows, rows, editRows, res}) {
          return Promise.resolve();
        },
        updateFail({fatRows, rows, editRows, error}) {
          return Promise.resolve();
        },
        beforeDeleteTip({fatRows, rows}) {
          if (rows.findIndex(r => r.name === '利威尔') > -1 && this.params.notDeleteLWL) {
            Message.warning('你已勾选【不能删除利威尔】');
            return Promise.reject();
          }
          return Promise.resolve();
        },
        beforeDelete({fatRows, rows}) {
          const {notDeleteSS} = this.params;
          if (rows.findIndex(r => r.name === '珊莎') > -1 && notDeleteSS) {
            Message.warning('删除记录中包含珊莎, 你已勾选不能删除珊莎');
            return Promise.reject();
          }
          return Promise.resolve(rows);
        },
        deleteSuccess({fatRows, rows, res}) {
          const {disableDefultDeleteSuccessWhenAL} = this.params;
          if (disableDefultDeleteSuccessWhenAL && rows.findIndex(r => r.name === '艾伦') > -1) {
            Message.success('恭喜恭喜! 删除对象中包含艾伦');
            return Promise.reject(); // 通过reject覆盖默认的删除成功提示
          }
          return Promise.resolve();
        },
        deleteFail({fatRows, rows, error}) {
          if (this.params.customDeleteFailTip) {
            Message.error('哦豁, 删除失败了! ' + JSON.stringify(error));
            return Promise.reject(); // 通过reject覆盖默认的删除失败提示
          }
          return Promise.resolve();
        }
      }),

      defaultQueryOfCreatedTime: [monthAgo, now],
      params: {
        editType: defaultEditType,
        pageLoadable: true, // 允许分页加载
        insertable: true, // 允许新增
        updatable: true, // 允许编辑
        deletable: true, // 允许删除
        enableColumnFilter: true, // 允许动态筛选
        enableMulti: true, // 启用多选
        enableDblClickEdit: true, // 启用双击编辑
        size: defaultSize, // 默认尺寸
        bodyRowHeight: defaultRowHeight,
        loadSuccessTip: false, // 加载成功时提示
        customLoadFailTip: true, // 自定义加载失败提示
        customInsertSuccessTip: false, // 自定义插入成功提示
        customInsertFailTip: false, // 自定义插入失败提示
        notDeleteLWL: true, // 不允许删除利威尔
        notDeleteSS: true, // 不允许删除珊莎
        customDeleteFailTip: true, // 自定义删除失败提示
        disableDefultDeleteSuccessWhenAL: true, // 当删除对象包含艾伦时, 禁用默认删除成功提示
        disableUpdateAM: true, // 阿明不允许编辑
        disableUpdateToZs: true, // 名字不允许改为张三
        disableInsertLs: true, // 不允许添加李四
      },
      ...staticDict
    }
  },
  methods: {
    updateOption(key, val) {
      this.tableOption[key] = val;
      this.$refs['fastTable'].reRender()
    },
    updateOptionStyle(key, val) {
      this.tableOption.style[key] = val;
      this.$refs['fastTable'].reRender()
    },
    edit({row: fatRow, column, $index}) {
      const {row, editRow, config, status} = fatRow
      this.$refs['fastTable'].updateForm(fatRow)
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
    padding: 0 20px;

    & > * {
      display: block;
      margin-bottom: 5px;
    }
  }
}
</style>