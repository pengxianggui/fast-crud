# Fast-Crud

> 基于spring-boot、spring-mvc、mybatis-plus的代码生成式 + 动态crud注册的快速开发框架。

应用本框架的前提是你的项目必须使用以上三个框架。

## 模块介绍

- **fast-crud-spring-boot-starter**: 主要引入此依赖
- **fast-crud-auto-generator**: 基于mybatis-plus-generator封装的controller、service、serviceImpl、mapper、entity代码生成包，非必须。
- **demo**: 示例项目

## 版本

- **spring-boot**: 2.6.8
- **mybatis-plus**: 3.5.7, 更高版本会存在一些问题
- **knife4j**: 3.0.3
- **hutool-core**: 5.8.8
- **freemarker**: 2.3.31

## Roadmap

### 1.0 基础功能

- [x] 支持基于注解动态生成CRUD涉及的相关接口(新增(批量新增)、删除(批量删除)、修改(批量修改)、查询(分页、列表、详情)
  、唯一性验证)
- [x] 支持controller、service、serviceImpl、mapper、entity自动生成
- [x] 调整CRUD动态注册策略: 不根据ApiOperation来识别是否是接口方法，而是通过GetMapper、PostMapper、DeleteMapper、PutMapper、RequestMapper等来识别
- [x] 移除对knife4j-spring-boot-starter的强依赖

### 2.0 提供配套的前端CRUD表格组件库

- [ ] 提供一个纯js库, 用来灵活构建Query和PageQuery查询条件
- [ ] 表单搜索: 基于表格列定义 quicker-filter 自动生成, 并支持自定义开发
- [ ] 表头过滤: 每一列的表头支持点击弹窗输入过滤，支持：
    - [ ] 普通字符串模糊匹配(通过=前缀的精准匹配)
    - [ ] 枚举类型(下拉多选/单选)的精准匹配
    - [ ] 日期、日期时间、时间的范围匹配
    - [ ] 数值类型的范围或精准匹配(可选)
    - [ ] 支持针对该列的distinct查询，并支持勾选以便多值精准匹配(in)
    - [ ] 输入的过滤条件合理美观的展示在过滤列表, 并且可二次编辑(参考es日志搜索)
- [ ] 增加“快筛”下拉按钮菜单
    - [ ] 可针对当前搜索条件进行前端保存并加入到快筛菜单按钮里
    - [ ] 点击快筛中的下拉菜单，可快速基于其所保存的筛选条件进行过滤
    - [ ] 快筛应用前, 应当做兼容性校验：字段、字段类型需和当前表格列一致,否则提示并删除此快筛项
    - [ ] 针对快筛应用时,应当将过滤条件回显在表单搜索和表头过滤中,并支持二次编辑,此时应当显示针对当前快筛的两个操作按钮:
      删除和保存

### 3.0 提供数据导出和更多按钮的扩展功能

- [ ] 提供"更多"按钮
    - [ ] xlsx数据导出功能: 导出当前筛选条件下的当前页数据/全部数据，可勾选字段
    - [ ] "更多"按钮支持扩展增加新功能