package io.github.pengxianggui.crud.demo;

import io.github.pengxianggui.crud.autogenerate.CodeAutoGenerator;

public class CodeGenerator {

    public static void main(String[] args) {
        CodeAutoGenerator.builder()
                .author("pengxg")
                .module("demo")//模块名
                .url("jdbc:mysql://127.0.0.1:3306/fast-crud")
//                .schema("fast-crud")
                .username("root")
                .password("123456")
                .parentPkg("io.github.pengxianggui.crud.demo")
//                .entitySuperClass(Base2.class) //实体继承父类,不需要时此行注释
                .build()
                .generate();

    }
}
