package io.github.pengxianggui.crud.demo.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.util.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import io.github.pengxianggui.crud.BaseServiceImpl;
import io.github.pengxianggui.crud.batch.AllowBatchUpdateField;
import io.github.pengxianggui.crud.demo.controller.vo.StudentPageVO;
import io.github.pengxianggui.crud.demo.domain.Student;
import io.github.pengxianggui.crud.demo.domain.StudentSensitive;
import io.github.pengxianggui.crud.demo.mapper.StudentMapper;
import io.github.pengxianggui.crud.demo.mapper.StudentSensitiveMapper;
import io.github.pengxianggui.crud.demo.service.StudentService;
import io.github.pengxianggui.crud.join.MPJLambdaWrapperBuilder;
import io.github.pengxianggui.crud.query.PagerQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
public class StudentServiceImpl extends BaseServiceImpl<StudentMapper, Student> implements StudentService {

    /** 演示用的数据权限开关，仅用于对比开关前后的结果 */
    public static volatile boolean dataPermissionEnabled = false;

    /**
     * 数据权限阈值：只允许操作 id &lt;= 该值的数据。
     * 取 2 是为了演示直观 —— 勾选大乔(1)、小乔(2)、曹操(3) 时，开启后只有前两条会被修改。
     */
    public static final long DATA_PERMISSION_MAX_ID = 2L;

    @Resource
    private StudentSensitiveMapper studentSensitiveMapper;

    @Override
    public IPage<StudentPageVO> pageVO(PagerQuery query) {
        Page<StudentPageVO> pager = new Page<>(query.getCurrent(), query.getSize());
        Map<String, Object> extra = query.getExtra();
        String keyword = (CollectionUtil.isNotEmpty(extra) && extra.containsKey("keyword"))
                ? StrUtil.toStringOrNull(extra.get("keyword"))
                : null;
        MPJLambdaWrapper<Student> wrapper = new MPJLambdaWrapperBuilder<Student>(StudentPageVO.class)
                .query(query)
                .build();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Student::getName, keyword).or().like(Student::getLoveName, keyword));
        }
        return getBaseMapper().selectJoinPage(pager, StudentPageVO.class, wrapper);
    }

    @Override
    public int insert(StudentPageVO model) {
        Student student = BeanUtil.copyProperties(model, Student.class);
        int count = insert(student);
        StudentSensitive studentSensitive = new StudentSensitive();
        studentSensitive.setStudentId(student.getId());
        studentSensitive.setIdCard(model.getIdCard());
        studentSensitive.setAddress(model.getAddress());
        studentSensitive.setPhone(model.getPhone());
        count += studentSensitiveMapper.insertOrUpdate(studentSensitive) ? 1 : 0;
        return count;
    }

    @Override
    public int update(StudentPageVO model, Boolean updateNull) {
        Student entity = getById(model.getId());
        BeanUtil.copyProperties(model, entity);
        int count = updateById(entity) ? 1 : 0;
        StudentSensitive studentSensitive = studentSensitiveMapper.selectJoinOne(
                new MPJLambdaWrapper<>(StudentSensitive.class).eq(StudentSensitive::getStudentId, entity.getId()));
        if (studentSensitive == null) {
            studentSensitive = new StudentSensitive();
            studentSensitive.setStudentId(entity.getId());
        }
        studentSensitive.setIdCard(model.getIdCard());
        studentSensitive.setAddress(model.getAddress());
        studentSensitive.setPhone(model.getPhone());
        count += studentSensitiveMapper.insertOrUpdate(studentSensitive) ? 1 : 0;
        return count;
    }

    @Override
    public String upload(String row, String col, MultipartFile file) throws IOException {
        return fileManager.getFileService().upload(file, DateUtils.format(new Date(), "yyyyMMdd"));
    }

    // 批量修改的两个钩子：审计列填充 + 数据权限

    /**
     * 批量修改不走 updateById，MyBatis-Plus 的字段填充不生效，更新时间需在此注入。
     * key 用数据库列名，需与 {@link AllowBatchUpdateField} 的声明一致。
     */
    @AllowBatchUpdateField("update_time")
    @Override
    protected Map<String, Object> batchUpdateFill() {
        return Collections.singletonMap("update_time", LocalDateTime.now());
    }

    /**
     * 演示数据权限：返回的条件会同时进入统计 SELECT 与执行 UPDATE 的 WHERE，
     * 越权的主键不会被修改，也不计入 affectedRows。
     */
    @Override
    protected Consumer<QueryWrapper<Student>> batchUpdateRowCondition() {
        if (!dataPermissionEnabled) {
            return null;
        }
        log.info("Batch update data permission is ON, only id <= {} can be modified.", DATA_PERMISSION_MAX_ID);
        return w -> w.le("id", DATA_PERMISSION_MAX_ID);
    }
}

