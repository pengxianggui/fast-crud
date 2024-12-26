<template>
  <div class="demo">
    <param-config :params="params" :option="tableOption" @refresh="refreshTable"></param-config>

    <fast-table ref="fastTable" class="el-card" :option="tableOption" :key="tableKey">
      <fast-table-column label="ID" prop="id"/>
      <fast-table-column-img label="头像" prop="avatarUrl" required/>
      <fast-table-column-input label="姓名" prop="name" first-filter :quick-filter="true" required/>
      <fast-table-column-number label="年龄" prop="age" required/>
      <fast-table-column-select label="性别" prop="sex" :options="sexOptions" :quick-filter="true" required/>
      <fast-table-column-select label="爱好" prop="hobby" :options="hobbyOptions"
                                :quick-filter="true" quick-filter-block quick-filter-checkbox
                                val-key="code" label-key="name"
                                :default-val__q="['1', '2', '3', '4', '5']"
                                :disable-val="['6']"
                                required/>
      <fast-table-column-textarea label="地址" prop="address"/>
      <fast-table-column-switch label="已毕业" prop="graduated" :quick-filter="true" :default-val="false"/>
      <fast-table-column-time-picker label="幸运时刻" prop="luckTime"/>
      <fast-table-column-date-picker label="生日" prop="birthday" :picker-options="pickerOptionsE" required/>
      <fast-table-column-file label="简历" prop="resumeUrl" :show-overflow-tool-tip="false"/>
      <fast-table-column-date-picker label="创建时间" prop="createTime" :picker-options__q="pickerOptionsQ"
                                     type="datetime"
                                     :quick-filter="false" :default-val__q="defaultQueryOfCreatedTime"
                                     value-format__q="yyyy-MM-dd HH:mm:ss"
                                     value-format__e="yyyy-MM-ddTHH:mm:ss"
                                     :default-time="['00:00:00', '23:59:59']"
                                     :editable="false"/>
    </fast-table>
  </div>
</template>

<script>
import FastTableOption from "../../packages/model";
import {Message} from 'element-ui';
import ParamConfig from "@/example/ParamConfig.vue";
import staticDict from './dict'

export default {
  name: "FastTableDemo",
  components: {ParamConfig},
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
        beforeToUpdate({rows}) {
          const {disableUpdateAM} = this.params;
          if (rows.findIndex(r => r.name === '阿明') > -1 && disableUpdateAM) {
            Message.warning("你已勾选【阿明不允许编辑】")
            return Promise.reject();
          }
          return Promise.resolve();
        },
        beforeDeleteTip({rows}) {
          const {notDeleteLWL} = this.params;
          if (rows.findIndex(r => r.name === '利威尔') > -1 && notDeleteLWL) {
            Message.warning('你已勾选【不能删除利威尔】');
            return Promise.reject();
          }
          return Promise.resolve();
        },
        beforeDelete({rows}) {
          const {notDeleteSS} = this.params;
          if (rows.findIndex(r => r.name === '珊莎') > -1 && notDeleteSS) {
            Message.warning('删除记录中包含珊莎, 你已勾选不能删除珊莎');
            return Promise.reject();
          }
          return Promise.resolve(rows);
        },
        deleteSuccess({rows, res}) {
          const {disableDefultDeleteSuccessWhenAL} = this.params;
          if (disableDefultDeleteSuccessWhenAL && rows.findIndex(r => r.name === '艾伦') > -1) {
            Message.success('恭喜恭喜! 删除对象中包含艾伦');
            return Promise.reject(); // 通过reject覆盖默认的删除成功提示
          }
          return Promise.resolve();
        },
        deleteFail({rows, error}) {
          if (this.params.customDeleteFailTip) {
            Message.error('哦豁, 删除失败了! ' + JSON.stringify(error));
            return Promise.reject(); // 通过reject覆盖默认的删除失败提示
          }
          return Promise.resolve();
        }
      }),

      tableKey: 0,
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
        notDeleteLWL: true, // 不允许删除利威尔
        notDeleteSS: true, // 不允许删除珊莎
        customDeleteFailTip: true, // 自定义删除失败提示
        disableDefultDeleteSuccessWhenAL: true, // 当删除对象包含艾伦时, 禁用默认删除成功提示
        disableUpdateAM: true, // 阿明不允许编辑
      },
      ...staticDict,
    }
  },
  methods: {
    refreshTable() {
      this.tableKey += 1; // 强刷
    }
  }
}
</script>

<style scoped lang="scss">
.demo {
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