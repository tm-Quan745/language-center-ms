package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Staff;
import vn.edu.ute.productmgmt.repo.StaffRepository;

import java.util.List;

public class JpaStaffRepository implements StaffRepository {
    @Override
    public Staff findById(EntityManager em, int id) throws Exception {
        return em.find(Staff.class, id);
    }

    @Override
    public List<Staff> findAll(EntityManager em) throws Exception {
        return em.createQuery("select s from Staff s", Staff.class)
                .getResultList();
    }

    @Override
    public void insert(EntityManager em, Staff staff) throws Exception {
        em.persist(staff);
    }

    @Override
    public void update(EntityManager em, Staff staff) throws Exception {
        em.merge(staff);
    }

    @Override
    public void delete(EntityManager em, int id) throws Exception {
        Staff staff = em.find(Staff.class, id);
        if (staff == null) throw new Exception("Staff not found: " + id);
        em.remove(staff);
    }
}
