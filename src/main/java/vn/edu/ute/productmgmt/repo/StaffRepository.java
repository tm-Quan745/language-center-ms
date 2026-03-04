package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Staff;

import java.util.List;

public interface StaffRepository {
    Staff findById(EntityManager em, Long id) throws Exception;
    List<Staff> findAll(EntityManager em) throws Exception;
    void insert(EntityManager em, Staff staff) throws Exception;
    void update(EntityManager em, Staff staff) throws Exception;
    void delete(EntityManager em, Long id) throws Exception;
}
