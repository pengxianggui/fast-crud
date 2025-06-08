/**
 * 利用mybatis-plus-join实现适配FastCrud的跨表查询。只需利用注解定义好dto类(或者叫做vo类)即可。
 * <p>
 * PS: 由于mybatis-plus-join跨表构建MPJLambdaWrapper时要求的MethodReference不支持lambda模拟，目前采用的方案是利用APT编译时预注册所有涉及
 * 的Method Reference到注册表，运行时从注册表获取(详见{@link io.github.pengxianggui.crud.join.MethodReferenceScanProcessor},
 * 注册表见{@link io.github.pengxianggui.crud.join.MethodReferenceRegistry}
 * <p>
 * 【验证】
 * <pre>
 * ✅ 一对一关联(a inner/left/right join b on a.x = b.y)
 * ✅ 一对多关联(a inner/left/right join b on a.x = b.y)
 * </pre>
 *
 * @author pengxg
 * @date 2025/6/1 19:56
 * @see io.github.pengxianggui.crud.join.JoinMain
 * @see io.github.pengxianggui.crud.join.InnerJoin
 * @see io.github.pengxianggui.crud.join.LeftJoin
 * @see io.github.pengxianggui.crud.join.RightJoin
 * @see io.github.pengxianggui.crud.join.RelateTo
 */
package io.github.pengxianggui.crud.join;