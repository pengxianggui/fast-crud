<template>
  <div class="fc-dynamic-filter-list">
    <el-popover v-for="(f, index) in filters">
      <template v-slot:reference>
        <div class="fc-dynamic-filter-btns">
          <el-button type="text" class="fc-dynamic-filter-open-btn" :class="{'strikethrough': f.disabled}">
            {{ f | label | ellipsis(30) }}
          </el-button>
          <el-button type="text" class="fc-dynamic-filter-del-btn" icon="el-icon-close"
                     @click.stop="delConfig(index)"></el-button>
        </div>
      </template>
      <component class="component" :is="f.component" v-model="f.val" v-bind="f.props"
                 v-if="f.opt !== Opt.NULL && f.opt !== Opt.NNULL && f.opt !== Opt.EMPTY && f.opt !== Opt.NEMPTY"/>
      <div class="fc-dynamic-filter-footer">
        <el-button type="primary" size="mini" icon="el-icon-search" @click="confirm">查询</el-button>
        <el-button :type="f.disabled ? 'primary' : 'info'" plain size="mini" @click="toggleFilter(f)">
          {{ f.disabled ? '启用' : '禁用' }}
        </el-button>
      </div>
    </el-popover>
    <el-button class="fc-dynamic-filter-clear-btn" type="text" style="padding: 0; color: #d37c84" @click="clearFilters"
               v-if="filters.length > 1">清空筛选
    </el-button>
  </div>
</template>

<script>
import {Opt} from "../../../model";
import {escapeValToLabel} from "./util";
import {ellipsis} from "../../../filters";
import {isEmpty, isString} from "../../../util/util";

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
  data() {
    return {
      Opt: Opt
    }
  },
  filters: {
    ellipsis,
    label(filter) {
      const {label, component} = filter;
      if (!filter.isEffective()) {
        filter.disabled = true;
        return `[${label}]无有效值`
      }
      const conds = filter.getConds();
      let tip = '';
      const {props} = filter
      for (let i = 0; i < conds.length; i++) {
        let {opt, val} = conds[i];
        val = escapeValToLabel(component, val, props);
        switch (opt) {
          case Opt.EQ:
          case Opt.GT:
          case Opt.GE:
          case Opt.LT:
          case Opt.LE:
            tip += `${label} ${opt} ${val}`;
            break;
          case Opt.LIKE:
            tip += `${label} 模糊匹配 ${val}`;
            break;
          case Opt.NLIKE:
            tip += `${label} 非模糊匹配 ${val}`;
            break;
          case Opt.IN:
            tip += `${label} 包含 ${val}`;
            break;
          case Opt.NIN:
            tip += `${label} 不包含 ${val}`;
            break;
          case Opt.NULL:
            tip += `${label} 为null`;
            break;
          case Opt.NNULL:
            tip += `${label} 不为null`;
            break;
          case Opt.EMPTY:
            tip += `${label} 为空`;
            break;
          case Opt.NEMPTY:
            tip += `${label} 不为空`;
            break;
          case Opt.BTW:
            tip += `${label} 在${val}之间`;
            break;
          default:
            tip += `${label}未知的比较符`
            break
        }
        if (i !== conds.length - 1) {
          tip += " 且 "
        }
      }
      return tip;
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
      this.confirm()
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
  max-width: 420px;
  max-height: 600px;
  overflow: auto;

  ::v-deep {
    .fc-checkbox-group {
      display: block;

      .el-checkbox {
        display: block;
      }
    }
  }
}

.fc-dynamic-filter-footer {
  display: flex;
  justify-content: center;
  margin-top: 10px;
}

</style>