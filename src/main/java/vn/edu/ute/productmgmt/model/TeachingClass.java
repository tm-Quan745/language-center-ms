package vn.edu.ute.productmgmt.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "teaching_class")
public class TeachingClass {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "class_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "class_name", nullable = false, length = 100)
    private String className;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "max_student", nullable = false)
    private int maxStudent;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ClassStatus status; // Lưu ý: Kiểu dữ liệu là ClassStatus

    @OneToMany(mappedBy = "teachingClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();

    @Override
    public String toString() {
        return className;
    }

    // Default Constructor
    public TeachingClass() {}

    // Full Constructor (Đã sửa String status thành ClassStatus status)
    public TeachingClass(UUID id, String className, Course course, Teacher teacher, Room room,
                         LocalDate startDate, LocalDate endDate, int maxStudent, ClassStatus status) {
        this.id = id;
        this.className = className;
        this.course = course;
        this.teacher = teacher;
        this.room = room;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxStudent = maxStudent;
        this.status = status;
    }

    // Business helper methods
    public void addSchedule(Schedule schedule) {
        schedules.add(schedule);
        schedule.setTeachingClass(this);
    }

    public void removeSchedule(Schedule schedule) {
        schedules.remove(schedule);
        schedule.setTeachingClass(null);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }

    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getMaxStudent() { return maxStudent; }
    public void setMaxStudent(int maxStudent) { this.maxStudent = maxStudent; }

    public ClassStatus getStatus() { return status; }
    public void setStatus(ClassStatus status) { this.status = status; }

    public List<Schedule> getSchedules() { return schedules; }
    public void setSchedules(List<Schedule> schedules) { this.schedules = schedules; }
}