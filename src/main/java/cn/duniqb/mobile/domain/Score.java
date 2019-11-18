package cn.duniqb.mobile.domain;

import java.io.Serializable;
import javax.persistence.*;
import lombok.Data;

@Data
@Table(name = "score")
public class Score implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "JDBC")
    private Integer id;

    /**
     * 学号
     */
    @Column(name = "stu_no")
    private String stuNo;

    /**
     * 课程号
     */
    @Column(name = "course_id")
    private String courseId;

    /**
     * 学年
     */
    @Column(name = "`year`")
    private Integer year;

    /**
     * 学期  0-春、1-秋
     */
    @Column(name = "term")
    private Boolean term;

    /**
     * 平时成绩
     */
    @Column(name = "usual_score")
    private String usualScore;

    /**
     * 期末成绩
     */
    @Column(name = "end_score")
    private String endScore;

    /**
     * 总评
     */
    @Column(name = "total_score")
    private String totalScore;

    /**
     * 是否缓考  0-是、1-否
     */
    @Column(name = "slow_exam")
    private Boolean slowExam;

    /**
     * 考试性质
     */
    @Column(name = "exam_type")
    private String examType;

    /**
     * 备注
     */
    @Column(name = "`comment`")
    private String comment;

    private static final long serialVersionUID = 1L;
}