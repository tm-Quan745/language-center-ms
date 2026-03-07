package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Result;
import vn.edu.ute.productmgmt.model.TeachingClass;

import java.util.List;

public interface ResultRepository {

    void insert(EntityManager em, Result result);

    void update(EntityManager em, Result result);

    Result findById(EntityManager em, Long id);

    List<Result> findByClass(EntityManager em, TeachingClass teachingClass);
}