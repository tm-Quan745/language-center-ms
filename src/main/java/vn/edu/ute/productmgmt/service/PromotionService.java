package vn.edu.ute.productmgmt.service;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Promotion;
import vn.edu.ute.productmgmt.model.enums.ActiveStatus;
import vn.edu.ute.productmgmt.model.enums.DiscountType;
import vn.edu.ute.productmgmt.repo.PromotionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

public class PromotionService {

    private final PromotionRepository promotionRepo;
    private final TransactionManager tx;

    public PromotionService(PromotionRepository promotionRepo, TransactionManager tx) {
        this.promotionRepo = promotionRepo;
        this.tx = tx;
    }

    public void create(Promotion promotion) throws Exception {
        validate(promotion);
        System.out.println("Creating promotion: " + promotion.getPromoName());
        tx.runInTransaction(em -> {
            promotionRepo.insert(em, promotion);
            return null;
        });
    }

    public void update(Promotion promotion) throws Exception {
        validate(promotion);
        tx.runInTransaction(em -> {
            promotionRepo.update(em, promotion);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            promotionRepo.delete(em, id);
            return null;
        });
    }

    public Promotion findById(Long id) {
        EntityManager em = Jpa.em();
        try {
            return promotionRepo.findById(em, id);
        } finally {
            em.close();
        }
    }

    public List<Promotion> findAll() {
        EntityManager em = Jpa.em();
        try {
            return promotionRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    /**
     * Kiểm tra khuyến mãi còn hiệu lực: Active, ngày forDate nằm trong [start_date, end_date].
     */
    public boolean isPromotionValid(Promotion p, LocalDate forDate) {
        if (p == null || forDate == null) return false;
        if (p.getStatus() != ActiveStatus.Active) return false;
        if (p.getStartDate() != null && forDate.isBefore(p.getStartDate())) return false;
        if (p.getEndDate() != null && forDate.isAfter(p.getEndDate())) return false;
        return true;
    }

    /**
     * Tính số tiền sau giảm: base - discount (Percent hoặc Amount).
     */
    public BigDecimal calculateDiscountedAmount(BigDecimal baseAmount, Promotion p) {
        if (baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) < 0)
            baseAmount = BigDecimal.ZERO;
        if (p == null || p.getDiscountValue() == null)
            return baseAmount;
        BigDecimal discount = switch (p.getDiscountType()) {
            case Percent -> baseAmount.multiply(p.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case Amount -> p.getDiscountValue().min(baseAmount);
        };
        return baseAmount.subtract(discount).max(BigDecimal.ZERO);
    }

    private void validate(Promotion promotion) {
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion không được null");
        }
        if (promotion.getPromoName() == null || promotion.getPromoName().isBlank()) {
            throw new IllegalArgumentException("Tên khuyến mãi không được để trống");
        }
        if (promotion.getDiscountValue() == null ||
                promotion.getDiscountValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá trị giảm giá phải >= 0");
        }
        if (promotion.getDiscountType() == DiscountType.Percent &&
                promotion.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Giảm theo % không được vượt quá 100");
        }
        if (promotion.getStartDate() != null && promotion.getEndDate() != null &&
                promotion.getStartDate().isAfter(promotion.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }
}
