package io.github.pengxianggui;


import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.builder.Entity;
import com.baomidou.mybatisplus.generator.config.converts.DmTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.*;

import static com.baomidou.mybatisplus.generator.config.rules.DbColumnType.LOCAL_DATE_TIME;

public class CodeAutoGenerator {

    @Data
    @Builder
    public static class Config{

        private String author;
        private String module;
        private String url;
        private String username;
        private String password;
        private String schema;
        private String parentPkg;
        private Class entitySuperClass;

    }

    public void generate(Config config){

        String moduleName = scanner("请输入模块名称:",false);
        String tables = scanner("请输入表名，多个英文逗号分隔",false);
        String controllerMappingTemp = "";

        int level = scannerInt("请输入生成代码的最高层级（controller(0),service(1),mapper(2),model(3)）:");
        if(level==0&&!tables.contains(",")){
            controllerMappingTemp = scanner("请输入控制器路由，默认（" + tables.replaceFirst("_", "/") + "）:",true);
        }

        String controllerMapping = controllerMappingTemp;


        String systemPath = System.getProperty("user.dir")+ File.separator+config.getModule();
        //String out = "D://";
        String out = systemPath + File.separator + "src" + File.separator + "main" + File.separator + "java";
        DataSourceConfig.Builder dataSourceConfigBuilder = new DataSourceConfig.Builder(config.getUrl(), config.getUsername(), config.getPassword());
        dataSourceConfigBuilder.schema(config.getSchema());
        if(config.getUrl().contains(":dm://")) {
            dataSourceConfigBuilder.typeConvert(new DmTypeConvert() {
                @Override
                public IColumnType processTypeConvert(GlobalConfig config, String fieldType) {
                    if (fieldType.equalsIgnoreCase("date")) {
                        return DbColumnType.LOCAL_DATE;
                    }
                    if (fieldType.equalsIgnoreCase("time")) {
                        return DbColumnType.LOCAL_TIME;
                    }
                    if (fieldType.toLowerCase().contains("time")) {
                        return LOCAL_DATE_TIME;
                    }
                    return super.processTypeConvert(config, fieldType);
                }
            });
        }

        FastAutoGenerator.create(dataSourceConfigBuilder).injectionConfig(builder -> {
            builder.customMap(new HashMap() {{
                put("moduleName", moduleName);
                put("controllerMapping", controllerMapping);
            }});
        }).globalConfig(builder -> {
            builder
                    .author(config.getAuthor()) // 设置作者
                    .enableSwagger() // 开启 swagger 模式
                    //.fileOverride() // 覆盖已生成文件
                    .dateType(DateType.TIME_PACK)
                    .outputDir(out)// 指定输出目录
                    .disableOpenDir();// 生成代码后不打开输出目录
        }).packageConfig(builder -> {
            builder.parent(config.getParentPkg()) // 设置父包名
                    .entity("domain." + moduleName)
                    .controller("controller." + moduleName)
                    .service("service." + moduleName)
                    .serviceImpl("service." + moduleName + ".impl")
                    .mapper("mapper." + moduleName)
                    .pathInfo(Collections.singletonMap(OutputFile.xml, systemPath + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "mapper")); // 设置mapperXml生成路径
        }).strategyConfig(builder -> {
                    Entity.Builder builder1 = builder.addInclude(getTables(tables)) // 设置需要生成的表名
                            //.addTablePrefix("base_")// 设置过滤表前缀
                            .enableSchema()
                            .entityBuilder()
                            .disableSerialVersionUID()
                            .enableLombok();

                    if(config.getEntitySuperClass()!=null){
                        builder1 = builder1
                                //.addTableFills(iFills)//生成时间自动填充属性
                                .superClass(config.getEntitySuperClass());
                    }

                    builder1.controllerBuilder()
                    .enableRestStyle()//开启@RestController风格
                    .serviceBuilder()
                    .formatServiceFileName("%sService"); //去掉默认的I前缀
        }).templateEngine(new FreemarkerTemplateEngine())//使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateConfig(builder -> {                //设置自定义模板路径
                    if(level>0){
                        builder.controller("");
                    }
                    if(level>1){
                        builder.service("");
                        builder.serviceImpl("");
                    }
                    if(level>2){
                        builder.mapper("");
                        builder.xml("");
                    }
                    builder.xml("");
                }).execute();

    }

    /**
     * 读取控制台内容
     */
    public String scanner(String tip,boolean blankAccess) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append(tip);
        System.out.println(help);
        if(blankAccess&&scanner.hasNextLine()){
            return scanner.nextLine();
        }
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if(blankAccess||(ipt!=null&&!ipt.trim().equals(""))) {
                return ipt;
            }
        }
        throw new RuntimeException("请输入正确的的值！");
    }

    public int scannerInt(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append(tip);
        System.out.println(help);
        if (scanner.hasNextInt()) {
            return scanner.nextInt();
        }
        return 0;
    }

    protected static List<String> getTables(String tables) {
        return Arrays.asList(tables.split(","));
    }

}
