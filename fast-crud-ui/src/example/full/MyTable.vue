<template>
  <fast-table ref="fastTable" class="fast-table" :option="tableOption" :key="tableKey"
              @current-change="handleCurrentChange"
              @row-click="handleRowClick"
              @row-dblclick="handleRowDblClick"
              @select="handleSelect"
              @selection-change="handleSelectionChange"
              @select-all="handleSelectAll">
    <fast-table-column label="ID" prop="id"/>
    <fast-table-column-img label="头像" prop="avatarUrl" :fixed="params.fixedAvatar" required/>
    <fast-table-column-input label="姓名" prop="name" first-filter :quick-filter="true" required/>
    <fast-table-column-number label="年龄" prop="age" required
                              :min="18" :max="60"
                              :rules="[{type: 'number', min: 18, max: 60, message: '年龄必须在[18,60]之间'}]"
                              @change="handleAgeChange"/>
    <fast-table-column-select label="性别" prop="sex" :options="sexOptions" :quick-filter="true" required/>
    <fast-table-column-select label="爱好" prop="hobby" :options="hobbyOptions"
                              :quick-filter="true" quick-filter-block quick-filter-checkbox
                              val-key="code" label-key="name"
                              :default-val_q="['1', '2', '3', '4', '5']"
                              :disable-val="['6']"
                              required/>
    <fast-table-column label="爱慕者Id" prop="loveId"/>
    <fast-table-column-object label="爱慕者姓名" prop="loveName"
                              :table-option="loveOption" show-field="name" :pick-map="{id: 'loveId'}"/>
    <fast-table-column-textarea label="地址" prop="address"/>
    <fast-table-column-switch label="已毕业" prop="graduated" :quick-filter="true" required/>
    <fast-table-column-time-picker label="幸运时刻" prop="luckTime" required
                                   :editable="({editRow}) => !(editRow.age > 35)"/>
    <fast-table-column-date-picker label="生日" prop="birthday" :picker-options="pickerOptionsE" required/>
    <fast-table-column-file label="简历" prop="resumeUrl" :show-overflow-tool-tip="false"/>
    <fast-table-column-date-picker label="创建时间" prop="createTime" :picker-options_q="pickerOptionsQ"
                                   type="datetime"
                                   :quick-filter="false" :default-val_q="defaultQueryOfCreatedTime"
                                   value-format_e="yyyy-MM-ddTHH:mm:ss"
                                   :default-time="['00:00:00', '23:59:59']"
                                   :editable="false"/>
    <el-table-column label="操作" width="60px">
      <template slot-scope="scope">
        <el-button type="text" size="small" @click="edit(scope)">编辑</el-button>
      </template>
    </el-table-column>
    <template #button="scope">
      <el-button :size="scope.size" icon="el-icon-link" @click="expandButton(scope)">查看当前Demo源码</el-button>
      <el-button :size="scope.size" @click="tryPick(false)">Try Pick</el-button>
      <el-button :size="scope.size" @click="tryPick(true)">Try Pick(多选)</el-button>
    </template>
    <template #moreButton="scope">
      <el-dropdown-item :size="scope.size" @click.native="expandMoreButton(scope)">扩展按钮</el-dropdown-item>
    </template>
    <template #foot="scope">
      <el-button size="mini" @click="expandFoot(scope)">扩展按钮</el-button>
    </template>
  </fast-table>
</template>

<script>
import {FastTableOption, util} from "../../../packages";
import {Message} from 'element-ui';
import staticDict from './dict'
import {pick} from "../../../packages/util/pick";
export default {
  name: "MyTable",
  props: {
    params: Object
  },
  data() {
    const now = new Date();
    const monthAgo = new Date();
    monthAgo.setTime(monthAgo.getTime() - 3600 * 1000 * 24 * 30);
    return {
      tableOption: new FastTableOption({
        context: this, // important! 否则钩子函数里无法获取当当前组件实例上下文
        title: '',
        module: 'student', // 配置了这个, 默认分页接口就是: /student/page, 新增接口就是: /student/insert, 其它同理
        enableDblClickEdit: true,
        enableMulti: true,
        enableColumnFilter: true,
        lazyLoad: false,
        editType: 'inline', // 默认inline
        insertable: true,
        updatable: true,
        deletable: true,
        sortField: 'createTime',
        sortDesc: true,
        pagination: {
          size: 10,
          "page-sizes": [5, 10, 20, 50, 100]
        },
        style: {
          flexHeight: true,
          size: 'medium', // mini,small,medium,default
          bodyRowHeight: '45px',
          formLabelWidth: 'auto', // 默认为auto
          formLayout: 'id,avatarUrl, name|age|sex, graduated|hobby|hobby, loveId|loveName|loveName, address, birthday|luckTime, resumeUrl, createTime' // 弹窗表单布局设置
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
        beforeInsert({fatRows, rows, editRows}) {
          if (editRows.findIndex(r => r.name === '李四') > -1 && this.params.disableInsertLs) {
            Message.warning('你已勾选【不允许添加李四】');
            return Promise.reject();
          }
          return Promise.resolve();
        },
        insertSuccess({fatRows, rows, editRows, res}) {
          if (this.params.customInsertSuccessTip) {
            Message.success('啧啧啧, 插入成功啦!');
            return Promise.reject(); // 取消内置的插入成功提示
          }
          return Promise.resolve();
        },
        insertFail({fatRows, rows, editRows, error}) {
          if (this.params.customInsertFailTip) {
            Message.error('哦豁, 插入失败了!');
            return Promise.reject();
          }
          return Promise.resolve();
        },
        beforeToUpdate({fatRows, rows}) {
          if (rows.findIndex(r => r.name === '阿明') > -1 && this.params.disableUpdateAM) {
            Message.warning("你已勾选【阿明不允许编辑】")
            return Promise.reject();
          }
          return Promise.resolve();
        },
        beforeUpdate({fatRows, rows, editRows}) {
          if (editRows.findIndex(r => r.name === '张三') > -1 && this.params.disableUpdateToZs) {
            Message.warning('你已勾选【名字不允许改为张三】');
            return Promise.reject();
          }
          return Promise.resolve();
        },
        updateSuccess({fatRows, rows, editRows, res}) {
          return Promise.resolve();
        },
        updateFail({fatRows, rows, editRows, error}) {
          return Promise.resolve();
        },
        beforeDeleteTip({fatRows, rows}) {
          if (rows.findIndex(r => r.name === '利威尔') > -1 && this.params.notDeleteLWL) {
            Message.warning('你已勾选【不能删除利威尔】');
            return Promise.reject();
          }
          return Promise.resolve();
        },
        beforeDelete({fatRows, rows}) {
          const {notDeleteSS} = this.params;
          if (rows.findIndex(r => r.name === '珊莎') > -1 && notDeleteSS) {
            Message.warning('删除记录中包含珊莎, 你已勾选不能删除珊莎');
            return Promise.reject();
          }
          return Promise.resolve(rows);
        },
        deleteSuccess({fatRows, rows, res}) {
          const {disableDefultDeleteSuccessWhenAL} = this.params;
          if (disableDefultDeleteSuccessWhenAL && rows.findIndex(r => r.name === '艾伦') > -1) {
            Message.success('恭喜恭喜! 删除对象中包含艾伦');
            return Promise.reject(); // 通过reject覆盖默认的删除成功提示
          }
          return Promise.resolve();
        },
        deleteFail({fatRows, rows, error}) {
          if (this.params.customDeleteFailTip) {
            Message.error('哦豁, 删除失败了! ' + JSON.stringify(error));
            return Promise.reject(); // 通过reject覆盖默认的删除失败提示
          }
          return Promise.resolve();
        },
        beforeCancel({fatRows, rows, status}) {
          if (status === 'update' && this.params.disableCancelWhenUpdate) {
            Message.warning('你已经勾选更新时不允许取消')
            return Promise.reject();
          }
          return Promise.resolve();
        }
      }),
      loveOption: new FastTableOption({
        module: 'student',
        conds: [
          // 预筛
          // {col: 'name', opt: '=', val: '利威尔'} // 写法一
          // new Cond('name', 'like', '利威尔') // 写法二
        ],
        render(h) {
          return [
            h('fast-table-column', {props: {prop: 'id', label: 'id'}}),
            h('fast-table-column-img', {props: {prop: 'avatarUrl', label: '头像'}}),
            h('fast-table-column', {props: {prop: 'name', label: '姓名1', firstFilter: true}})
          ]
        }
      }),
      tableKey: 0,
      defaultQueryOfCreatedTime: [monthAgo, now],
      ...staticDict
    }
  },
  methods: {
    updateOption(key, val) {
      this.tableOption[key] = val;
    },
    updateOptionStyle(key, val) {
      this.tableOption.style[key] = val;
      this.tableKey++;
    },
    edit({row: fatRow, column, $index}) {
      const {row, editRow, config, status} = fatRow
      this.$refs['fastTable'].updateForm(fatRow)
    },
    handleAgeChange(age, {row, editRow, status, column, config, $index}) {
      console.log('index:', $index);
      console.log('status:', status);
      console.log('editRow:', editRow);
      console.log('row:', row);
      console.log('config:', config);
      console.log('column:', column);
      if (this.params.autoSetGraduatedWhenAgeChange) {
        if (util.isNumber(age) && age > 50) {
          editRow.graduated = true;
          config['graduated'].props.disabled = true
        } else {
          config['graduated'].props.disabled = false
        }
      }
    },
    handleCurrentChange({fatRow, row}) {
      console.log('current-change..........................................................')
      console.log('fatRow:', fatRow);
      console.log('row:', row);
    },
    handleSelect({fatRows, rows, fatRow, row}) {
      console.log('select..........................................................')
      console.log('fatRows:', fatRows);
      console.log('rows:', rows);
      console.log('fatRow:', fatRow);
      console.log('row:', row);
    },
    handleSelectionChange({fatRows, rows}) {
      console.log('selection-change..........................................................')
      console.log('fatRows:', fatRows);
      console.log('rows:', rows);
    },
    handleSelectAll({fatRows, rows}) {
      console.log('select-all..........................................................')
      console.log('fatRows:', fatRows)
      console.log('rows:', rows)
    },
    handleRowClick({fatRow, row, column, event}) {
      console.log('row-click..........................................................')
      console.log('fatRow:', fatRow);
      console.log('row:', row);
      console.log('column:', column);
      console.log('event:', event);
    },
    handleRowDblClick({fatRow, row, column, event}) {
      console.log('row-dblclick..........................................................')
      console.log('fatRow:', fatRow);
      console.log('row:', row);
      console.log('column:', column);
      console.log('event:', event);
    },
    expandButton({choseRow, checkedRows, editRows}) {
      window.open('https://github.com/pengxianggui/fast-crud/tree/main/fast-crud-ui/src/example/full/MyTable.vue', '_blank')
    },
    tryPick(multiple) {
      pick({
        multiple: multiple,
        option: this.loveOption,
        dialog: {
          width: '80%'
        }
      }).then((data) => {
        Message.success('打开控制台查看你选择的数据!')
        console.log('你选择数据:', data)
      }).catch(() => {
        Message.info('你取消了')
      })
    },
    expandMoreButton({choseRow, checkedRows, editRows}) {
      console.log('choseRow', choseRow)
      console.log('checkedRows', checkedRows)
      console.log('editRows', editRows)
    },
    expandFoot({choseRow, checkedRows, editRows}) {
      console.log('choseRow', choseRow)
      console.log('checkedRows', checkedRows)
      console.log('editRows', editRows)
    }
  }
}
</script>

<style scoped lang="scss">

</style>