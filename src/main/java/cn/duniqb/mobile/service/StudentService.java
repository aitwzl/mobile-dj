package cn.duniqb.mobile.service;

import cn.duniqb.mobile.domain.Student;

public interface StudentService {

    /**
     * 根据学号查询学生
     *
     * @param no
     * @return
     */
    Student selectOneByNo(String no);
}

















