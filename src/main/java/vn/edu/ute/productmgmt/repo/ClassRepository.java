package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.TeachingClass;

import java.util.List;
import java.util.UUID;

public interface ClassRepository {

    void insert(EntityManager em, TeachingClass teachingClass);

    TeachingClass findById(EntityManager em, UUID id);

    List<TeachingClass> findAll(EntityManager em);

    List<TeachingClass> findByStatus(EntityManager em, String status);

    void update(EntityManager em, TeachingClass teachingClass);

    void delete(EntityManager em, UUID id);
}