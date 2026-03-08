package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.PlacementTest;
import vn.edu.ute.productmgmt.repo.PlacementTestRepository;

import java.util.List;

public class JpaPlacementTestRepository implements PlacementTestRepository {

    @Override
    public void insert(EntityManager em, PlacementTest placementTest) {
        em.persist(placementTest);
    }

    @Override
    public void update(EntityManager em, PlacementTest placementTest) {
        em.merge(placementTest);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        PlacementTest pt = em.find(PlacementTest.class, id);
        if (pt != null) {
            em.remove(pt);
        }
    }

    @Override
    public PlacementTest findById(EntityManager em, Long id) {
        return em.find(PlacementTest.class, id);
    }

    @Override
    public List<PlacementTest> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT DISTINCT p FROM PlacementTest p " +
                "LEFT JOIN FETCH p.student " +
                "ORDER BY p.testDate DESC, p.id DESC",
                PlacementTest.class
        ).getResultList();
    }

    @Override
    public List<PlacementTest> findByStudentId(EntityManager em, Long studentId) {
        return em.createQuery(
                "SELECT p FROM PlacementTest p " +
                "WHERE p.student.id = :studentId " +
                "ORDER BY p.testDate DESC, p.id DESC",
                PlacementTest.class
        ).setParameter("studentId", studentId).getResultList();
    }
}
