package io.github.pengxianggui.crud.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 把请求不合法类异常映射为可读的 HTTP 400。
 * 批量修改的校验失败（字段不在白名单、值类型不合法、未开 allowNull 却传空值等）若不拦截，
 * 会冒泡成 HTTP 500 空响应，前端只能提示「服务器错误」，响应体结构见 {@link BatchUpdateError}。
 * 注意这是全局 Advice，会作用于使用方的所有 Controller，因此默认不注册，
 * 需要时设置 fast-crud.batch-update.map-validation-error=true 开启；
 * 关闭状态下校验失败会抛异常，交由使用方自己的全局异常处理接管。
 */
@Slf4j
@RestControllerAdvice
public class BatchUpdateExceptionHandler {

    /**
     * 参数/业务校验失败。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public BatchUpdateError handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        log.warn("Request rejected as bad argument: {}", e.getMessage());
        return new BatchUpdateError()
                .setMessage(e.getMessage())
                .setPath(request == null ? null : request.getRequestURI());
    }

    /**
     * 请求体 Bean Validation 失败（如 ids / fields 为空）。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BatchUpdateError handleMethodArgumentNotValid(MethodArgumentNotValidException e,
                                                        HttpServletRequest request) {
        return new BatchUpdateError()
                .setMessage(joinFieldErrors(e.getBindingResult().getFieldErrors()))
                .setPath(request == null ? null : request.getRequestURI());
    }

    /**
     * 兼容表单绑定路径的校验失败。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public BatchUpdateError handleBind(BindException e, HttpServletRequest request) {
        return new BatchUpdateError()
                .setMessage(joinFieldErrors(e.getBindingResult().getFieldErrors()))
                .setPath(request == null ? null : request.getRequestURI());
    }

    /**
     * 方法参数上的约束校验失败。
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public BatchUpdateError handleConstraintViolation(ConstraintViolationException e,
                                                      HttpServletRequest request) {
        String message = e.getConstraintViolations() == null || e.getConstraintViolations().isEmpty()
                ? e.getMessage()
                : e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return new BatchUpdateError()
                .setMessage(message)
                .setPath(request == null ? null : request.getRequestURI());
    }

    private String joinFieldErrors(List<FieldError> fieldErrors) {
        if (fieldErrors == null || fieldErrors.isEmpty()) {
            return "请求参数不合法";
        }
        return fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }
}
