package io.github.pengxianggui.crud.autogenerate;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.builder.Controller;
import com.baomidou.mybatisplus.generator.config.builder.Entity;
import com.baomidou.mybatisplus.generator.config.builder.Mapper;
import com.baomidou.mybatisplus.generator.config.builder.Service;
import com.baomidou.mybatisplus.generator.config.converts.DmTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.generator.config.rules.DbColumnType.LOCAL_DATE_TIME;

@Setter
@Getter
@Builder
public class CodeAutoGenerator {
    private String author;
    private String module;
    private String url;
    private String username;
    private String password;
    private String schema;
    private String parentPkg;
    private Class entitySuperClass;

    public void generate() {

        String moduleName = scanner("请输入模块名称:", true);
        String tables = scanner("请输入表名，多个英文逗号分隔", false);

        List<String> levels;
        String level = scanner("请输入要生成的层级,controller(1),service(2),mapper(3),model(4). 注意: 多选时英文逗号分割, 若为空则表示全部", true);
        if (level == null || level.trim().length() == 0) {
            levels = Arrays.asList("1", "2", "3", "4");
        } else {
            levels = Arrays.stream(level.split(",")).map(String::trim).collect(Collectors.toList());
        }

        String controllerMappingTemp = "";
        if (level.contains("1")) {
            controllerMappingTemp = scanner("请输入控制器路由，默认（" + tables.replaceFirst("_", "/") + "）:", true);
        }
        String controllerMapping = controllerMappingTemp;

        String systemPath = System.getProperty("user.dir") + File.separator + getModule();
        String out = systemPath + File.separator + "src" + File.separator + "main" + File.separator + "java";
        DataSourceConfig.Builder dataSourceConfigBuilder = new DataSourceConfig.Builder(getUrl(), getUsername(), getPassword());
        dataSourceConfigBuilder.schema(getSchema());
        if (getUrl().contains(":dm://")) {
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
                }).globalConfig((scanner, builder) -> {
                    builder.author(getAuthor())
                            .enableSwagger() // 开启 swagger 模式
                            //.fileOverride() // 覆盖已生成文件
                            .dateType(DateType.TIME_PACK)
                            .outputDir(out)// 指定输出目录
                            .disableOpenDir();// 生成代码后不打开输出目录
                }).packageConfig(builder -> {
                    builder.parent(getParentPkg()) // 设置父包名
                            .entity(emptyIfElse(moduleName, "domain", "domain." + moduleName))
                            .controller(emptyIfElse(moduleName, "controller", "controller." + moduleName))
                            .service(emptyIfElse(moduleName, "service", "service." + moduleName))
                            .serviceImpl(emptyIfElse(moduleName, "service.impl", "service." + moduleName + ".impl"))
                            .mapper(emptyIfElse(moduleName, "mapper", "mapper." + moduleName))
                            .pathInfo(Collections.singletonMap(OutputFile.xml, systemPath + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "mapper")); // 设置mapperXml生成路径
                }).strategyConfig(builder -> {
                    Entity.Builder builder1 = builder.addInclude(getTables(tables)) // 设置需要生成的表名
                            //.addTablePrefix("base_")// 设置过滤表前缀
                            .enableSchema()
                            .entityBuilder()
                            .disableSerialVersionUID()
                            .enableLombok();

                    if (getEntitySuperClass() != null) {
                        builder1 = builder1
                                //.addTableFills(iFills)//生成时间自动填充属性
                                .superClass(getEntitySuperClass());
                    }

                    Controller.Builder controllerBuilder = builder1.controllerBuilder()
                            .template("templates/dynamic/controller.java")
                            .enableRestStyle();//开启@RestController风格
                    if (!levels.contains("1")) {
                        controllerBuilder.disable();
                    }
                    Service.Builder serviceBuilder = builder1.serviceBuilder()
                            .formatServiceFileName("%sService") //去掉默认的I前缀
                            .serviceTemplate("templates/dynamic/service.java")
                            .serviceImplTemplate("templates/dynamic/serviceImpl.java");
                    if (!levels.contains("2")) {
                        serviceBuilder.disable();
                    }
                    // mapper暂时未定制化
                    Mapper.Builder mapperBuilder = builder1.mapperBuilder()
//                            .mapperTemplate("classpath:/templates/mapper.java")
//                            .mapperXmlTemplate("classpath:/templates/mapper.xml")
                            ;
                    if (!levels.contains("3")) {
                        mapperBuilder.disable();
                    }
                    Entity.Builder entityBuilder = builder1.entityBuilder()
                            .javaTemplate("templates/dynamic/entity.java");
                    if (!levels.contains("4")) {
                        entityBuilder.disable();
                    }
                }).templateEngine(new FreemarkerTemplateEngine())//使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();

    }

    /**
     * 读取控制台内容
     */
    private String scanner(String tip, boolean blankAccess) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append(tip);
        System.out.println(help);
        if (blankAccess && scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (blankAccess || (ipt != null && !ipt.trim().equals(""))) {
                return ipt;
            }
        }
        throw new RuntimeException("请输入正确的的值！");
    }

    private int scannerInt(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append(tip);
        System.out.println(help);
        if (scanner.hasNextInt()) {
            return scanner.nextInt();
        }
        return 0;
    }

    private List<String> getTables(String tables) {
        return Arrays.asList(tables.split(","));
    }

    private String emptyIfElse(String str, String ifState, String elseState) {
        if (str == null || str.length() == 0) {
            return ifState;
        }
        return elseState;
    }
}
