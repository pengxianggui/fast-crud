# Fast-Crud

<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">FastCrud</h1>
<h4 align="center">基于SpringBoot/MybatisPlus/MybatisPlusJoin开发的CRUD快速开发工具(支持跨表)</h4>
<p align="center">
	<a href="http://fastcrud-doc.pengxg.cc/">文档</a> |
	<a href="http://fastcrud.pengxg.cc/">演示环境</a> |
	<a href="https://github.com/pengxianggui/fast-crud-ui3">配套前端</a> |
	<a href="https://github.com/pengxianggui/fast-crud/releases">ChangeLog</a> |
	<a href="https://github.com/pengxianggui/fast-crud/blob/main/LICENSE">LICENSE</a>
</p>

> 此项目通常配合[前端](https://github.com/pengxianggui/fast-crud-ui3)使用, 也可独立使用。

## 介绍

> 基于spring-boot、mybatis-plus、mybatis-plus-join的CRUD快速开发框架。

## 快速开始

### 引入maven依赖

```xml

<dependencies>
    <dependency>
        <groupId>io.github.pengxianggui</groupId>
        <artifactId>fast-crud-spring-boot-starter</artifactId>
        <version>${version}</version>
    </dependency>
    <!-- 下面这个可选 -->
    <dependency>
        <groupId>io.github.pengxianggui</groupId>
        <artifactId>fast-crud-auto-generator</artifactId>
        <version>${version}</version>
    </dependency>
</dependencies>
```

> 具体版本号请查看pom.xml

### 代码生成准备

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
>
> 若不使用fast-crud-auto-generator，则自行创建entity、controller、service等文件即可。

### 前端引入

详见[前端](https://github.com/pengxianggui/fast-crud-ui3)

## 模块介绍

- **fast-crud-spring-boot-starter**: 主要引入此依赖
- **fast-crud-auto-generator**: 基于mybatis-plus-generator封装的controller、service、serviceImpl、mapper、entity代码生成包，非必须。
- **demo**: 后端示例项目

## 版本

- **spring-boot**: 2.6.8
- **mybatis-plus**: 3.5.7, 更高版本会存在一些问题
- **mybatis-plus-join**: 1.5.3
- **knife4j**: 3.0.3
- **hutool-core**: 5.8.8
- **hutool-extra**: 5.8.8
- **freemarker**: 2.3.31