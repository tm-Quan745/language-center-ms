package vn.edu.ute.productmgmt.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "result")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "result_id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private TeachingClass teachingClass;

    @Column(nullable = false)
    private double score;

    @Column(length = 20)
    private String grade;

    @Column(columnDefinition = "TEXT")
    private String comment;

    public Result() {
    }

    public Result(UUID id, Student student, TeachingClass teachingClass,
                  double score, String grade, String comment) {
        this.id = id;
        this.student = student;
        this.teachingClass = teachingClass;
        this.score = score;
        this.grade = grade;
        this.comment = comment;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public TeachingClass getTeachingClass() {
        return teachingClass;
    }

    public void setTeachingClass(TeachingClass teachingClass) {
        this.teachingClass = teachingClass;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
