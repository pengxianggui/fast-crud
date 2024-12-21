<template>
  <div class="demo">
    <div class="param">
      <h3>开关</h3>
      <el-checkbox v-model="params.loadCondition">允许加载分页</el-checkbox>
      <el-checkbox v-model="params.loadSuccessTip">分页加载成功提示</el-checkbox>
      <el-checkbox v-model="params.customLoadFailTip">自定义加载失败提示</el-checkbox>
      <el-checkbox v-model="params.notDeleteLWL">不能删除利威尔(不弹窗)</el-checkbox>
      <el-checkbox v-model="params.notDeleteSS">不允许删除珊莎(弹窗后)</el-checkbox>
      <el-checkbox v-model="params.disableDefultDeleteSuccessWhenAL">删除艾伦时庆祝</el-checkbox>
      <el-checkbox v-model="params.customDeleteFailTip">自定义删除失败提示</el-checkbox>
      <el-checkbox v-model="params.disableUpdateAM">阿明不允许编辑</el-checkbox>
    </div>
    <fast-table class="el-card" :option="tableOption">
      <fast-table-column label="ID" prop="id"/>
      <!-- TODO 1.0 fast-table-column-img还不具备状态 -->
<!--      <fast-table-column-img label="头像" prop="avatar" :filter="false"/>-->
      <fast-table-column-input label="姓名" prop="name" first-filter :quick-filter="true" :required="true"/>
      <fast-table-column-number label="年龄" prop="age"/>
      <fast-table-column-select label="性别" prop="sex" :options="sexOptions" :quick-filter="true"/>
      <fast-table-column-select label="爱好" prop="hobby" :options="hobbyOptions"
                                :quick-filter="true" quick-filter-block quick-filter-checkbox
                                val-key="code" label-key="name"
                                :default-val__q="['1', '2', '3', '4', '5']" :disable-val__q="['6']"/>
      <fast-table-column-textarea label="地址" prop="address"/>
      <fast-table-column-switch label="已毕业" prop="graduated" :quick-filter="true"/>
      <fast-table-column-time-picker label="闹钟" prop="clockTime"/>
      <fast-table-column-date-picker label="生日" prop="birthday" :picker-options="pickerOptionsE"/>
      <fast-table-column-number label="身高" prop="height"/>
      <fast-table-column-number label="体重" prop="weight"/>
      <fast-table-column-date-picker label="创建时间" prop="createTime" :picker-options__q="pickerOptionsQ" type="datetime"
                                     :quick-filter="false" :default-val__q="defaultQueryOfCreatedTime"
                                     value-format__q="yyyy-MM-dd HH:mm:ss"
                                     value-format__e="yyyy-MM-ddTHH:mm:ss"
                                     :default-time="['00:00:00', '23:59:59']"
                                     :editable="false"/>
    </fast-table>
  </div>
</template>

<script>
import FastTableOption from "../packages/model";
import {Message} from 'element-ui';

export default {
  name: "App",
  data() {
    const now = new Date();
    const monthAgo = new Date();
    monthAgo.setTime(monthAgo.getTime() - 3600 * 1000 * 24 * 30);
    return {
      tableOption: new FastTableOption({
        context: this, // important! 否则钩子函数里无法获取当当前组件实例上下文
        title: '学生管理',
        module: 'student', // 配置了这个, 默认分页接口就是: /student/page, 新增接口就是: /student/insert, 其它同理
        enableDblClickEdit: true,
        enableMulti: true,
        enableColumnFilter: true,
        lazyLoad: false,
        editType: 'inline',
        sortField: 'createTime',
        sortDesc: true,
        pagination: {
          size: 5,
          "page-sizes": [5, 10, 20, 50, 100]
        },
        style: {
          size: 'small', // mini,small,medium
          bodyRowHeight: '45px'
        },
        beforeLoad({query}) {
          if (this.params.loadCondition) {
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
        beforeEnableUpdate({rows}) {
          const { disableUpdateAM } = this.params;
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
      pickerOptionsQ: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        },
        shortcuts: [{
          text: '最近一周',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: '最近一个月',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
            picker.$emit('pick', [start, end]);
          }
        }, {
          text: '最近三个月',
          onClick(picker) {
            const end = new Date();
            const start = new Date();
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
            picker.$emit('pick', [start, end]);
          }
        }]
      },
      pickerOptionsE: {
        disabledDate(time) {
          return time.getTime() > Date.now();
        },
        shortcuts: [{
          text: '今天',
          onClick(picker) {
            picker.$emit('pick', new Date());
          }
        }, {
          text: '昨天',
          onClick(picker) {
            const date = new Date();
            date.setTime(date.getTime() - 3600 * 1000 * 24);
            picker.$emit('pick', date);
          }
        }, {
          text: '一周前',
          onClick(picker) {
            const date = new Date();
            date.setTime(date.getTime() - 3600 * 1000 * 24 * 7);
            picker.$emit('pick', date);
          }
        }]
      },
      sexOptions: [
        {
          label: '男',
          value: '1'
        },
        {
          label: '女',
          value: '0'
        }
      ],
      hobbyOptions: [
        {
          name: '篮球',
          code: '1'
        },
        {
          name: '足球',
          code: '2'
        },
        {
          name: '排球',
          code: '3'
        },
        {
          name: '乒乓球',
          code: '4'
        },
        {
          name: '羽毛球',
          code: '5'
        },
        {
          name: '台球',
          code: '6'
        },
        {
          name: '游泳',
          code: '7'
        }
      ],
      defaultQueryOfCreatedTime: [monthAgo, now],
      params: {
        loadCondition: true, // 允许分页加载
        loadSuccessTip: false, // 加载成功时提示
        customLoadFailTip: true, // 自定义加载失败提示
        notDeleteLWL: true, // 不允许删除利威尔
        notDeleteSS: true, // 不允许删除珊莎
        customDeleteFailTip: true, // 自定义删除失败提示
        disableDefultDeleteSuccessWhenAL: true, // 当删除对象包含艾伦时, 禁用默认删除成功提示
        disableUpdateAM: true, // 阿明不允许编辑
      }
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
  }
}
</style>