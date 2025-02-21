package ${package.Controller};

import ${package.Service}.${table.serviceName};
import ${package.Entity}.${entity};
<#if swagger>
import io.swagger.annotations.Api;
</#if>
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.pengxianggui.crud.BaseController;

import javax.annotation.Resource;

<#if swagger>
@Api(tags="${table.comment}")
</#if>
@RestController
@RequestMapping("${table.name}")
public class ${table.controllerName} extends BaseController<${entity}>{

    @Resource
    private ${table.serviceName} ${table.serviceName?uncap_first};

    public ${table.controllerName}(${table.serviceName} ${table.serviceName?uncap_first}) {
        super(${table.serviceName?uncap_first});
    }

}
