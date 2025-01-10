# Fast-Crud

> 基于spring-boot、spring-mvc、mybatis-plus的代码生成式 + 动态crud注册的快速开发框架。

应用本框架的前提是你的项目必须使用以上三个框架。文档参考[这里](http://pengxg.cc/tags/fast-crud)

## 模块介绍

- **fast-crud-spring-boot-starter**: 主要引入此依赖
- **fast-crud-auto-generator**: 基于mybatis-plus-generator封装的controller、service、serviceImpl、mapper、entity代码生成包，非必须。
- **fast-crud-ui**: 前端组件库, 文档详见: [这里](./fast-crud-ui/README.md)
- **demo**: 后端示例项目

## 版本

- **spring-boot**: 2.6.8
- **mybatis-plus**: 3.5.7, 更高版本会存在一些问题
- **knife4j**: 3.0.3
- **hutool-core**: 5.8.8
- **hutool-extra**: 5.8.8
- **freemarker**: 2.3.31

## 使用方式
### 后端集成
#### 引入maven依赖
```xml
<dependencies>
    <dependency>
        <groupId>io.github.pengxianggui</groupId>
        <artifactId>fast-crud-spring-boot-starter</artifactId>
        <version>${version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.pengxianggui</groupId>
        <artifactId>fast-crud-auto-generator</artifactId>
        <version>${version}</version>
    </dependency>
</dependencies>
```
> 具体版本号请查看pom.xml

#### 代码生成准备
手撸一个main方法
```java
public class CodeGenerator {
    public static void main(String[] args) {
        CodeAutoGenerator.builder()
                .author("pengxg") // 替换为你的名称
                .module("demo") // maven多模块时配置模块名,表名要生成的代码文件存放的模块
                .url("jdbc:mysql://127.0.0.1:3306/fast-crud") // 替换成你的数据库连接地址
                .username("root") // 替换成你的数据库用户名
                .password("123456") // 替换成你的数据库密码
                .parentPkg("io.github.pengxianggui.crud.demo") // 替换成你的包根目录
                .build()
                .generate();
    }
}
```
> 运行这个main方法，会交互式询问要针对哪个表生成代码文件，可选择性生成对应的controller、service、serviceImpl、mapper以及mapper.xml。
> 且生成的controller方法已具备crud相关接口(由于几个标准接口是动态注册的, 所以controller类里不会体现, 可在swagger中查看)

### 前端引入(可选)
详见[这里](./fast-crud-ui/README.md)

## Roadmap

### 1.x 版本

#### 后端: 基础功能CRUD接口

- [x] 支持基于注解动态生成CRUD涉及的相关接口(新增(批量新增)、删除(批量删除)、修改(批量修改)、查询(分页、列表、详情)
  、唯一性验证)
- [x] 支持controller、service、serviceImpl、mapper、entity自动生成
- [x] 调整CRUD动态注册策略: 不根据ApiOperation来识别是否是接口方法，而是通过GetMapper、PostMapper、DeleteMapper、PutMapper、RequestMapper等来识别
- [x] 移除对knife4j-spring-boot-starter的强依赖

#### 前端: 提供配套的前端CRUD表格组件库

##### 创建(C)

- [x] 提供新增按钮功能, 支持
    - [x] 行内新增(必填校验)
    - [x] 弹窗表单新增(表单校验)
- [x] 批量新增

##### 查询(R)

- [x] **快筛**：表单形式的筛选，特点：快速。基于表格列定义 quicker-filter 自动生成, 并支持自定义开发
- [x] **简筛**：单一输入框的简单筛选，特点：简单。基于表格列定义 filter 自动生成的简单筛选，每次只能针对一个属性(可切换属性)。
- [x] **动筛**：基于表头的动态筛选，特点：筛选项可动态增减。每一列的表头支持点击弹窗输入过滤，支持：
    - [x] 普通字符串模糊匹配(通过=前缀的精准匹配)
    - [x] 枚举类型(下拉单选/checkbox多选)的精准匹配
    - [x] 日期、日期时间、时间的范围匹配
    - [x] 数值类型的范围或精准匹配(可选)
    - [x] 支持针对该列的distinct查询，并支持勾选以便多值精准匹配(in)
    - [x] 输入的过滤条件合理美观的展示在过滤列表, 并且可二次编辑, 包括启/禁用(参考es日志搜索)

##### 更新(U)

- [x] 提供双击行编辑功能:
    - [x] 行内编辑(必填校验)
    - [x] 弹窗表单编辑(表单校验)
- [x] 批量编辑: 当前页所有记录均进入可编辑模式(行内)

##### 删除(D)

- [x] 删除功能: 删除单行/批量删除

##### 其它

- [x] 增加FastTableColumnFile和FastTableColumnImg，分别文件上传和图片上传功能
- [x] FastTableColumnObject: 根据另一个tableOption和render来渲染弹窗pick对象，可以回填到当前行

### 2.x 版本：CRUD的外围扩展

- [ ] 批量修改: 选择要修改的行，弹窗输入要修改的字段和值, 批量更新这些行(输入一次值即可作用所有选中行)
- [ ] xlsx数据导出功能: 导出当前筛选条件下的当前页数据/全部数据，可勾选字段
- [ ] **存筛**：应用已保存的筛选条件进行筛选，下拉按钮菜单，特点：针对已保存的组合筛选条件进行筛选。
    - [ ] 可针对当前搜索条件进行前端保存并加入到快筛菜单按钮里
    - [ ] 点击快筛中的下拉菜单，可快速基于其所保存的筛选条件进行过滤
    - [ ] 快筛应用前, 应当做兼容性校验：字段、字段类型需和当前表格列一致,否则提示并删除此快筛项
    - [ ] 针对快筛应用时,应当将过滤条件回显在表单搜索和表头过滤中,并支持二次编辑,此时应当显示针对当前快筛的两个操作按钮:
      删除和保存
- [ ] 表格自定义配置
- [ ] 支持"更多"里扩展增加新功能按钮
- [ ] 支持针对mapping.js进行扩展，以便自定义增加新的FastTableColumn*组件
- [ ] **高级筛选**: 支持类似jira中jql的使用方式。 


## ChangeLog
参考[这里](https://github.com/pengxianggui/fast-crud/blob/main/ChangeLog.md)