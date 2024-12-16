<template>
  <div class="fc-dynamic-filter-list">
    <el-popover v-for="(f, index) in filters">
      <template v-slot:reference>
        <div class="fc-dynamic-filter-btns">
          <el-button type="text" class="fc-dynamic-filter-open-btn" :class="{'strikethrough': f.disabled}">
            {{ f | label }}
          </el-button>
          <el-button type="text" class="fc-dynamic-filter-del-btn" icon="el-icon-close" @click.stop="delConfig(index)"></el-button>
        </div>
      </template>
      <component class="component" :is="f.component" v-model="f.val" v-bind="f.props"/>
      <div class="fc-dynamic-filter-footer">
        <el-button type="primary" size="mini" icon="el-icon-search" @click="confirm">查询</el-button>
        <el-button :type="f.disabled ? 'primary' : 'info'" plain size="mini" @click="toggleFilter(f)">{{ f.disabled ? '启用' : '禁用' }}</el-button>
      </div>
    </el-popover>
    <el-button class="fc-dynamic-filter-clear-btn" type="text" style="padding: 0; color: #d37c84" @click="clearFilters" v-if="filters.length > 1">清空筛选</el-button>
  </div>
</template>

<script>
import {Opt} from "../../../model";

export default {
  name: "dynamic-filter-list",
  props: {
    filters: {
      type: Array,
      default: () => []
    },
    size: {
      type: String,
      default: () => 'small'
    }
  },
  filters: {
    label(filter) {
      const {label, val, opt} = filter
      // TODO 判断val是字典值，并转换为显示值。
      switch (opt) {
        case Opt.LIKE:
          return `[${label}] 包含 ${val}`;
        case Opt.EQ:
          return `[${label}] 等于 ${val}`;
        case Opt.GT:
          return `[${label}] 大于 ${val}`;
        case Opt.GE:
          return `[${label}] 大于等于 ${val}`;
        case Opt.LT:
          return `[${label}] 小于 ${val}`;
        case Opt.LE:
          return `[${label}] 小于等于 ${val}`;
        case Opt.IN:
          return `[${label}] 包含 ${val}`;
        case Opt.NIN:
          return `[${label}] 不包含 ${val}`;
        case Opt.NULL:
          return `[${label}] 为 null`;
        case Opt.NNULL:
          return `[${label}] 不为 null`;
        case Opt.BTW:
          return `[${label}] 在 ${val[0]} 和 ${val[1]} 之间`;
        default:
          return `[${label}] 检索条件`;
      }
    }
  },
  methods: {
    delConfig(index) {
      this.filters.splice(index, 1)
      this.confirm()
    },
    confirm(/*filter*/) {
      this.$emit('search')
    },
    toggleFilter(filter) {
      filter.disabled = !filter.disabled
      this.confirm(filter)
    },
    clearFilters() {
      this.filters.splice(0, this.filters.length);
      this.confirm();
    }
  }
}
</script>

<style scoped lang="scss">
.fc-dynamic-filter-list {
  display: flex;
  flex-wrap: wrap;
  column-gap: 5px;

  .fc-dynamic-filter-btns {
    &:hover {
      .fc-dynamic-filter-open-btn {
        text-decoration: underline;
      }
      .fc-dynamic-filter-del-btn {
        //display: inline-block;
        visibility: visible;
      }
    }
  }

  .strikethrough {
    text-decoration: line-through !important;
  }

  .fc-dynamic-filter-open-btn {
    color: gray;
    padding: 3px 5px;
    border: 1px solid #cacaca;
    border-radius: 3px;
    font-size: 13px !important;
  }

  .fc-dynamic-filter-del-btn {
    visibility: hidden;
    margin-left: 5px;
    padding: 5px 0;
    color: #8d4343;
    font-size: 13px !important;
  }

  .fc-dynamic-filter-clear-btn {
    font-size: 13px !important;
    margin-left: 10px;
  }
}

.component {
  margin: 10px 0;
}

.fc-dynamic-filter-footer {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

</style>