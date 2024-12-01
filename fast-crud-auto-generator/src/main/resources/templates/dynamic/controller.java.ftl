package ${package.Controller};

import io.github.pengxianggui.crud.dynamic.Crud;
import io.github.pengxianggui.crud.dynamic.CrudService;
import ${package.Service}.${table.serviceName};
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Api(tags="${table.comment}")
@RestController
@RequestMapping("<#if controllerMapping!="">${controllerMapping}<#else >${table.name?replace("_","/","f")}</#if>")
@Crud
public class ${table.controllerName} {

    @Resource
    @CrudService
    private ${table.serviceName} ${table.serviceName?uncap_first};

}
