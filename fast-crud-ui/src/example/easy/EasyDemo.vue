<template>
  <fast-table :option="tableOption">
    <fast-table-column-input prop="name" label="姓名"/>
    <fast-table-column-img prop="avatarUrl" label="头像"/>
    <fast-table-column-number prop="age" label="年龄"/>
    <fast-table-column-select prop="sex" label="性别" :options="[{label: '男', value: '1'}, {label: '女', value: '0'}]"/>
    <fast-table-column-date-picker prop="createTime" label="创建时间" type="datetime" :editable="false"/>
    <template #button="scope">
      <el-button :size="scope.size" icon="el-icon-link" @click="expandButton(scope)">查看当前Demo源码</el-button>
    </template>
  </fast-table>
</template>

<script>
import {FastTableOption} from "../../../packages";

export default {
  name: "EasyDemo",
  data() {
    return {
      tableOption: new FastTableOption({
        module: 'student',
      })
    }
  },
  methods: {
    expandButton({choseRow, checkedRows, editRows}) {
      window.open('https://github.com/pengxianggui/fast-crud/blob/main/fast-crud-ui/src/example/easy/EasyDemo.vue', '_blank')
    }
  }
}
</script>

<style scoped lang="scss">
</style>