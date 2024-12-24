<template>
  <el-upload v-model="modelValue"
             v-bind="$attrs"
             :action="action"
             :multiple="multiple"
             :limit="limit"
             :list-type="listType"
             :show-file-list="false"
             :on-success="handleSuccess"
             :on-error="handleError"
             class="uploader-wrapper">
    <template v-if="isEmpty(modelValue)">
      <i class="el-icon-plus"></i>
    </template>
    <template v-else>
      <!-- 图片 -->
      <template v-if="isPicture">
        <div class="picture-list" v-if="multiple">
          <img v-for="url in modelValue" :src="url">
        </div>
        <img :src="modelValue" v-else>
      </template>
      <!-- 普通文件 -->
      <template v-else>
        <div class="file-list" v-if="multiple">
          <el-link v-for="url in modelValue" :href="url">{{url}}</el-link>
        </div>
        <el-link :href="modelValue" v-else>{{modelValue}}</el-link>
      </template>
    </template>
  </el-upload>
</template>

<script>
import {isEmpty, isFunction} from "../../../util/util";

export default {
  name: "fast-upload",
  props: {
    // multiple为true则应当是单个url地址, 否则为url数组
    value: {
      type: [String, Array],
      default: () => null
    },
    multiple: {
      type: Boolean,
      default: () => false
    },
    action: {
      type: String,
      default: () => '/'
    },
    listType: {
      type: String,
      default: () => 'text'
    },
    limit: {
      type: Number,
      default: () => 1
    },
    /**
     * 上传成功后的回调, 必须解析出并返回url地址
     */
    responseHandler: {
      type: Function,
      default: (response, file, fileList) => response
    }
  },
  computed: {
    modelValue: {
      get() {
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      }
    },
    isPicture() {
      return this.listType === 'picture-card';
    }
  },
  methods: {
    isEmpty,
    handleSuccess(response, file, fileList) {
      const url = this.responseHandler(response, file, fileList);
      if (this.multiple) {
        this.modelValue.push(url);
      } else {
        this.modelValue = url;
      }
      if (this.$attrs.hasOwnProperty('on-success')) {
        const customOnSuccess = this.$attrs['on-success'];
        if (isFunction(customOnSuccess)) {
          customOnSuccess(response, file, fileList);
        }
      }
    },
    handleError(err, file, fileList) {
      debugger
    }
  }
}
</script>

<style scoped lang="scss">
img {
  height: 100%;
  object-fit: cover;
}
</style>