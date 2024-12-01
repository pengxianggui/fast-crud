//package io.github.pengxianggui.filter;
//
//import org.springframework.boot.web.servlet.ServletComponentScan;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import java.io.IOException;
//
//@Component
//@ServletComponentScan
//public class HttpServletRequestFilter implements Filter {
//
//    @Override
//    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
//
//        if (servletRequest instanceof HttpServletRequest) {
//            servletRequest = new RequestWrapper((HttpServletRequest) servletRequest);
//        }
//        filterChain.doFilter(servletRequest, servletResponse);
//    }
//
//}
//
//
