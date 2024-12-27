<template>
  <el-upload v-model="modelValue"
             v-bind="$attrs"
             :action="actionValue"
             :data="formData"
             :limit="limit"
             :list-type="listType"
             :file-list="files"
             :show-file-list="true"
             :on-success="handleSuccess"
             :on-error="handleError"
             :on-change="handleChange"
             :on-exceed="handleExceed"
             class="fc-fast-upload"
             :class="{'fc-fast-upload__hidden': hideUploadButton, 'fc-fast-upload__disable': disabled}">
    <template #default>
      <i class="el-icon-plus"></i>
    </template>
    <template #file="{file}">
      <!-- 图片 -->
      <template v-if="isPicture">
        <img class="el-upload-list__item-thumbnail" :src="file.url" alt=""/>
        <span class="el-upload-list__item-actions">
          <span class="el-upload-list__item-preview" @click="preview(file)">
            <i class="el-icon-zoom-in"></i>
          </span>
          <!--        <span-->
          <!--            v-if="!disabled"-->
          <!--            class="el-upload-list__item-download"-->
          <!--            @click="handleDownload(file)"-->
          <!--        >-->
          <!--          <i class="el-icon-download"></i>-->
          <!--        </span>-->
          <span v-if="!disabled" class="el-upload-list__item-delete" @click="handleRemove(file)">
            <i class="el-icon-delete"></i>
          </span>
        </span>
      </template>
      <!-- 普通文件 -->
      <template v-else>
        <i :class="'el-icon-paperclip'" v-if="disabled"></i>
        <el-button type="text" icon="el-icon-delete" style="padding: 2px; color: #f56c6c;" v-else
                   @click="handleRemove(file)"></el-button>
        <el-link :href="file.url" style="display: inline">{{ file.name }}</el-link>
      </template>
    </template>
  </el-upload>
</template>

<script>
import {Message} from 'element-ui';
import {isArray, isEmpty, isFunction, getNameFromUrl, defaultIfBlank} from "../../../util/util";
import FastTableOption from "../../../model";
import {openDialog} from "../../../util/dialog";

export default {
  name: "fast-upload",
  props: {
    // multiple为true则应当是单个url地址, 否则为url数组
    value: {
      type: [String, Array],
      default: () => null
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
    disabled: {
      type: Boolean,
      default: () => false
    },
    col: {
      type: String,
      default: () => ''
    },
    row: {
      type: Object,
      default: () => {
      }
    },
    data: {
      type: Object,
      default: () => {
      }
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
        this.refreshFiles(this.value)
        return this.value;
      },
      set(val) {
        this.$emit('input', val);
      }
    },
    actionValue() {
      return this.apiPrefix + this.action;
    },
    isPicture() {
      return this.listType === 'picture-card';
    },
    hideUploadButton() {
      return this.disabled || (!isEmpty(this.modelValue) && this.files.length >= this.limit);
    },
    formData() {
      return {
        row: JSON.stringify(this.row),
        col: this.col,
        ...this.data
      }
    }
  },
  data() {
    return {
      files: [],
      apiPrefix: defaultIfBlank(FastTableOption.$http.defaults.baseURL, '')
    }
  },
  methods: {
    isEmpty,
    refreshFiles(value) {
      const urls = [];
      if (isArray(value)) {
        urls.push(...value)
      } else if (!isEmpty(value)) {
        urls.push(value)
      }
      if (isEmpty(urls)) {
        this.files = [];
        return;
      }
      const {apiPrefix} = this;
      urls.forEach(url => {
        const name = getNameFromUrl(url);
        if (this.files.every(f => f.name !== name && f.url !== url)) {
          this.files.push({name: name, url: apiPrefix + url});
        }
      });
      for (let i = this.files.length - 1; i >= 0; i--) {
        if (urls.every(url => apiPrefix + url !== this.files[i].url)) {
          this.files.splice(i, 1);
        }
      }
    },
    handleSuccess(response, file, fileList) {
      const url = this.responseHandler(response, file, fileList);
      if (this.limit === 1) {
        this.modelValue = url;
      } else {
        this.modelValue = this.modelValue.push(url);
      }
      try {
        if (this.$attrs.hasOwnProperty('on-success')) {
          const customOnSuccess = this.$attrs['on-success'];
          if (isFunction(customOnSuccess)) {
            customOnSuccess(response, file, fileList);
          }
        }
      } catch (err) {
        log.error(err);
      }
      this.$emit('success', {response, file, fileList})
    },
    handleError(err, file, fileList) {
      // debugger
      this.$emit('fail', {err, file, fileList})
    },
    handleChange(file, fileList) {
      this.$nextTick(() => { // 延迟执行, 等待modelValue更新
        this.$emit('change', this.modelValue);
      })
    },
    handleExceed(files, fileList) {
      Message.warning('文件数量超过限制');
      this.$emit('exceed', {files, fileList})
    },
    handleRemove(file) {
      const index = this.files.findIndex(f => f.url === file.url);
      this.files.splice(index, 1);
      const urls = this.files.map(f => f.url);
      if (this.limit === 1) {
        this.modelValue = isEmpty(urls) ? '' : urls[0];
      } else {
        this.modelValue = urls;
      }
    },
    preview(file) {
      openDialog({
        component: {
          render: function (h) {
            return h('img', {
              attrs: {
                src: file.url,
                width: '100%'
              }
            })
          }
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.fc-fast-upload {
  .el-upload-list__item-actions {
    display: flex;
    align-items: center;
    justify-content: space-around;
    //justify-content: center;

    & > * {
      margin: 0 !important;

      & i {
        font-size: 14px;
      }
    }

    &::after {
      display: none;
    }
  }

  img {
    height: 100%;
    object-fit: cover;
  }
}

.fc-fast-upload.fc-fast-upload__hidden {
  ::v-deep {
    .el-upload {
      display: none;
    }
  }
}
.fc-fast-upload.fc-fast-upload__disable {
  border: none !important;
}
</style>