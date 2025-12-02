package io.github.pengxianggui.crud.join;

/**
 * 方法引用加载器(借助SPI实现)
 *
 * @author pengxg
 * @date 2025/12/2 15:48
 */
public interface MethodReferenceLoader {
    /**
     * 加载方法引用，注册到静态注册表中
     */
    void load();
}
