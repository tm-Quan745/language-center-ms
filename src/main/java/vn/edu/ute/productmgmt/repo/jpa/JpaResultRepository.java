package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.edu.ute.productmgmt.model.Result;
import vn.edu.ute.productmgmt.model.TeachingClass;
import vn.edu.ute.productmgmt.repo.ResultRepository;

import java.util.List;

public class JpaResultRepository implements ResultRepository {

    @Override
    public void insert(EntityManager em, Result result) {
        em.persist(result);
    }

    @Override
    public void update(EntityManager em, Result result) {
        em.merge(result);
    }

    @Override
    public Result findById(EntityManager em, Long id) {
        return em.find(Result.class, id);
    }

    @Override
    public List<Result> findByClass(EntityManager em, TeachingClass teachingClass) {

        TypedQuery<Result> query = em.createQuery(
                "SELECT r FROM Result r " +
                        "JOIN FETCH r.student " +
                        "JOIN FETCH r.teachingClass " +
                        "WHERE r.teachingClass = :cls",
                Result.class
        );

        query.setParameter("cls", teachingClass);

        return query.getResultList();
    }
}