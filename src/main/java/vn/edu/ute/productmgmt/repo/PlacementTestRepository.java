package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.PlacementTest;

import java.util.List;

public interface PlacementTestRepository {

    void insert(EntityManager em, PlacementTest placementTest);

    void update(EntityManager em, PlacementTest placementTest);

    void delete(EntityManager em, Long id);

    PlacementTest findById(EntityManager em, Long id);

    List<PlacementTest> findAll(EntityManager em);

    List<PlacementTest> findByStudentId(EntityManager em, Long studentId);
}
