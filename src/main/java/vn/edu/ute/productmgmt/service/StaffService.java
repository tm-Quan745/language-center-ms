package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Staff;
import vn.edu.ute.productmgmt.repo.StaffRepository;

import java.util.List;

public class StaffService {

    private final StaffRepository staffRepo;
    private final TransactionManager tx;

    public StaffService(StaffRepository staffRepo, TransactionManager tx) {
        this.staffRepo = staffRepo;
        this.tx = tx;
    }

    /**
     * Lấy danh sách toàn bộ nhân viên.
     */
    public List<Staff> getAll() throws Exception {
        return tx.runInTransaction(em -> staffRepo.findAll(em));
    }

    /**
     * Thêm mới nhân viên.
     * Trả về đối tượng Staff sau khi persist (id sẽ được JPA gán nếu dùng UUID).
     */
    public Staff create(Staff staff) throws Exception {
        return tx.runInTransaction(em -> {
            staffRepo.insert(em, staff);
            return staff;
        });
    }

    /**
     * Cập nhật thông tin nhân viên.
     */
    public Staff update(Staff staff) throws Exception {
        return tx.runInTransaction(em -> {
            staffRepo.update(em, staff);
            return staff;
        });
    }

    /**
     * Xoá nhân viên theo id (kiểu int theo StaffRepository hiện tại).
     */
    public void delete(int id) throws Exception {
        tx.runInTransaction(em -> {
            staffRepo.delete(em, id);
            return null;
        });
    }

    /**
     * Lấy thông tin 1 nhân viên theo id.
     */
    public Staff getById(int id) throws Exception {
        return tx.runInTransaction(em -> staffRepo.findById(em, id));
    }
}

