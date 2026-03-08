package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Certificate;
import vn.edu.ute.productmgmt.repo.CertificateRepository;

import java.util.List;

public class CertificateService {

    private final CertificateRepository certificateRepo;
    private final TransactionManager tx;

    public CertificateService(CertificateRepository certificateRepo, TransactionManager tx) {
        this.certificateRepo = certificateRepo;
        this.tx = tx;
    }

    public void create(Certificate certificate) throws Exception {
        validate(certificate);
        tx.runInTransaction(em -> {
            certificateRepo.insert(em, certificate);
            return null;
        });
    }

    public void update(Certificate certificate) throws Exception {
        validate(certificate);
        tx.runInTransaction(em -> {
            certificateRepo.update(em, certificate);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            certificateRepo.delete(em, id);
            return null;
        });
    }

    public Certificate findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return certificateRepo.findById(em, id);
        } finally {
            em.close();
        }
    }

    public List<Certificate> findAll() {
        EntityManager em = Jpa.em();
        try {
            return certificateRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    private void validate(Certificate c) {
        if (c == null) {
            throw new IllegalArgumentException("Chứng chỉ không được null");
        }
        if (c.getStudent() == null) {
            throw new IllegalArgumentException("Học viên không được để trống");
        }
        if (c.getCertName() == null || c.getCertName().isBlank()) {
            throw new IllegalArgumentException("Tên chứng chỉ không được để trống");
        }
        if (c.getIssueDate() == null) {
            throw new IllegalArgumentException("Ngày cấp không được để trống");
        }
    }
}
