package vn.edu.ute.productmgmt.model;

import jakarta.persistence.*;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "branches", uniqueConstraints = @UniqueConstraint(columnNames = "branch_name"))
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "branch_name", nullable = false, length = 150)
    private String branchName;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ActiveStatus status = ActiveStatus.Active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Branch() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public ActiveStatus getStatus() { return status; }
    public void setStatus(ActiveStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return branchName;
    }
}
