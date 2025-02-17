package ${package.Controller};

import ${package.Service}.${table.serviceName};
import ${package.Entity}.${entity};
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.pengxianggui.crud.BaseController;

import javax.annotation.Resource;

@Api(tags="${table.comment}")
@RestController
@RequestMapping("<#if controllerMapping!="">${controllerMapping}<#else >${table.name?replace("_","/","f")}</#if>")
public class ${table.controllerName} extends BaseController<${entity}>{

    @Resource
    private ${table.serviceName} ${table.serviceName?uncap_first};

    public StudentController(${table.serviceName} ${table.serviceName?uncap_first}) {
        super(${table.serviceName?uncap_first});
    }

}
