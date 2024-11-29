package io.github.pengxianggui;


public class CodeGenerator {

    public static void main(String[] args) {

        CodeAutoGenerator.Config config = CodeAutoGenerator.Config.builder()
                .author("pengxg")
                .module("demo")//模块名
                .url("jdbc:mysql://127.0.0.1:3306/back-up")
//                .schema("logmanager")
                .username("root")
                .password("134411")
                .parentPkg("io.github.pengxianggui")
//                .entitySuperClass(Base2.class) //实体继承父类,不需要时此行注释
                .build();

        CodeAutoGenerator codeAutoGenerator = new CodeAutoGenerator();

        codeAutoGenerator.generate(config);
        
    }
}
