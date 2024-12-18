<template>
  <div class="demo">
    <div class="param">
      <el-checkbox v-model="params.loadCondition">允许加载分页</el-checkbox>
    </div>
    <fast-table class="el-card" :option="tableOption">
      <fast-table-column label="ID" prop="id"/>
      <fast-table-column-img label="头像" prop="avatar" :filter="false"/>
      <fast-table-column label="姓名" prop="name" first-filter :quick-filter="true"/>
      <fast-table-column-number label="年龄" prop="age"/>
      <fast-table-column-select label="性别" prop="sex" :options="sexOptions" :quick-filter="true"/>
      <fast-table-column-select label="爱好" prop="hobby" :options="hobbyOptions"
                                :quick-filter="true" quick-filter-block quick-filter-checkbox
                                val-key="code" label-key="name"
                                :default-val__q="['1','4','7']" :disable-val__q="['2','3']"/>
      <fast-table-column-textarea label="地址" prop="address"/>
      <fast-table-column-switch label="已毕业" prop="graduated" :quick-filter="true"/>
      <fast-table-column-time-picker label="闹钟" prop="clockTime"/>
      <fast-table-column-date-picker label="生日" prop="birthday" :picker-options="pickerOptionsE"/>
      <fast-table-column-number label="身高" prop="height"/>
      <fast-table-column-number label="体重" prop="weight"/>
      <fast-table-column-date-picker label="创建时间" prop="createTime" :picker-options__q="pickerOptionsQ" type="datetime"
                                     :quick-filter="true" :default-val__q="defaultQueryOfCreatedTime"
                                     value-format="yyyy-MM-dd HH:mm:ss" :default-time="['00:00:00', '23:59:59']"/>
    </fast-table>
  </div>
</template>

<script>
import FastTableOption from "../packages/model";

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
          size: 5
        },
        style: {
          size: 'small',
          bodyRowHeight: '40px'
        },
        beforeLoad({query}) {
          console.log('beforeLoad: 你可以在这里加一些逻辑, 比如在某些条件下中断分页请求(返回Promise.reject(任意值))')
          if (this.params.loadCondition) {
            return Promise.resolve();
          }
          return Promise.reject('不满足加载条件, 请勾选【加载条件】')
        },
        loadSuccess({query, data, res}) {
          console.log('loadSuccess: 你可以在这里加一些逻辑, 比如加载子表之类的');
          return Promise.resolve(data);
        },
        loadFail({query, error}) {
          console.log('loadFail: 你可以在这里加一些逻辑, 错误提示之类的(返回Promise.reject(任意值)将取消默认行为)');
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
        loadCondition: true
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
    padding: 20px;
  }
}
</style>