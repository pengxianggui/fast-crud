<template>
  <div>
    {{test}}
    <el-input v-model="test.name"></el-input>
    <br>
    <el-input-number v-model="test.age"></el-input-number>
    <br>
    <el-button @click="valid">验证</el-button>
    <br>
    {{ result }}
  </div>
</template>

<script>
import Schema from 'async-validator'

export default {
  name: "ValidDemo",
  data() {
    return {
      test: {
        name: null,
        age: 0
      },
      result: []
    }
  },
  methods: {
    valid() {
      const rules = {
        name: [
          {required: true, message: '用户名不能为空', trigger: 'blur'},
          {min: 3, max: 12, message: '用户名长度应在 3 到 12 个字符之间', trigger: 'blur'},
        ],
        age: [
          // {required: true, message: '年龄不能为空', trigger: 'change'}, // 不生效
          // {required: true, message: '年龄不能为空'}, // 生效
          {type: 'number', required: true, message: '年龄不能为空', trigger: 'change'}, // 生效
          {type: 'number', min: 18, max: 60, message: '年龄必须在 18 到 60 岁之间', trigger: 'change'},
        ],
      };

      const validator = new Schema(rules);

      // 校验
      validator.validate(this.test, (errors, fields) => {
        this.result = errors
        if (errors) {
          console.error('验证失败:', errors);
        } else {
          console.log('验证通过');
        }
      });
    }
  }
}
</script>

<style scoped>

</style>