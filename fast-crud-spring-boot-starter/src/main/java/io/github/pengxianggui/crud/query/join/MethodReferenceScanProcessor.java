package io.github.pengxianggui.crud.query.join;

import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * 方法引用自动扫描处理器
 *
 * @author pengxg
 * @date 2025/5/24 11:44
 */
@AutoService(Processor.class)
public class MethodReferenceScanProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // TODO 扫描所有 @JoinMain修饰的dto类，生成注册表 注册到注册中心
        return false;
    }
}
