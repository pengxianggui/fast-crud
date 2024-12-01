package ${package.ServiceImpl};

import io.github.pengxianggui.crud.BaseServiceImpl;
import ${package.Entity}.${entity};
import ${package.Mapper}.${table.mapperName};
import ${package.Service}.${table.serviceName};
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ${table.serviceImplName} extends BaseServiceImpl<${entity}, ${table.mapperName}> implements ${table.serviceName} {

    @Resource
    private ${table.mapperName} ${table.mapperName?uncap_first};

    @Override
    public void init() {
        this.baseMapper = ${table.mapperName?uncap_first};
        this.clazz = ${entity}.class;
    }
}
