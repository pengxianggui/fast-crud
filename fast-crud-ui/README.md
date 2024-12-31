# fast-crud-ui

> 一套组件库, 快速实现表格CRUD, 支持强大、灵活的筛选、排序、新增和数据编辑功能。
> 并且扩展性高、配置灵活。

## 组件列表

- FastTable: 核心组件
- FastTableColumn: 只读列组件
- FastTableColumnInput: 文本输入框列组件
- FastTableColumnNumber: 数字输入框列组件
- FastTableColumnTextarea: 文本域输入框列组件
- FastTableColumnSelect: 下拉框列组件
- FastTableColumnSwitch: switch列组件
- FastTableColumnDatePicker: 日期选择列组件
- FastTableColumnTimePicker: 时间选择列组件
- FastTableColumnImg: 图片列组件
- FastTableColumnFile: 文件列组件

## 快速开始

安装

```bash
npm install fast-crud-ui
```

配置

```js
import Vue from 'vue'
import App from './App.vue'
import FastCrudUI from 'fast-crud-ui'
import http from "@/http";

Vue.use(FastCrudUI, {
    $http: http // axios实例, 必须提供!
})

new Vue({
    render: (h) => h(App)
}).$mount('#app')

```

使用
```vue

<template>
  <fast-table :option="tableOption">
    <fast-table-column-img prop="avatarUrl" label="头像"/>
    <fast-table-column-input prop="name" label="姓名"/>
    <fast-table-column-number prop="age" label="年龄"/>
    <fast-table-column-select prop="sex" label="性别"
                              :options="[{label: '男', value: '1'}, {label: '女', value: '0'}]"/>
    <fast-table-column-date-picker prop="createTime" label="创建时间" type="datetime" :editable="false"/>
  </fast-table>
</template>

<script>
  import {FastTableOption} from "fast-crud-ui";

  export default {
    name: "EasyDemo",
    data() {
      return {
        tableOption: new FastTableOption({
          module: 'student',
          // 更多配置参考文档或者完整示例(./src/example/full/FullDemo.vue)
        })
      }
    }
  }
</script>
```

更多使用文档参见: [这里](http://pengxg.cc/tags/fast-crud)