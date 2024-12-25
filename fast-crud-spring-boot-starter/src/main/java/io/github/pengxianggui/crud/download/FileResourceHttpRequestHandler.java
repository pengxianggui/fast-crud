package io.github.pengxianggui.crud.download;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * 借助ResourceHttpRequestHandler实现本地资源响应时，支持range分段请求资源。
 */
public class FileResourceHttpRequestHandler extends ResourceHttpRequestHandler {
    public final static String FILE_PATH = "FILE_PATH";

    @Override
    protected Resource getResource(HttpServletRequest request) {
        String filePath = (String) request.getAttribute(FILE_PATH);
        return new FileSystemResource(filePath);
    }
}
