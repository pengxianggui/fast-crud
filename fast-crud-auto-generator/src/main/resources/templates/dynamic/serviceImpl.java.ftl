package ${package.ServiceImpl};

import io.github.pengxianggui.crud.BaseServiceImpl;
import ${package.Entity}.${entity};
import ${package.Mapper}.${table.mapperName};
import ${package.Service}.${table.serviceName};
import org.springframework.stereotype.Service;

@Service
public class ${table.serviceImplName} extends BaseServiceImpl<${entity}, ${table.mapperName}> implements ${table.serviceName} {

}
