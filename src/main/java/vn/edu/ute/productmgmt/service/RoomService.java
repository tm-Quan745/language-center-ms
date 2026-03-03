package vn.edu.ute.productmgmt.service;

import vn.edu.ute.productmgmt.db.Jpa;
import vn.edu.ute.productmgmt.db.TransactionManager;
import vn.edu.ute.productmgmt.model.Room;
import vn.edu.ute.productmgmt.repo.RoomRepository;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.UUID;

public class RoomService {

    private final RoomRepository roomRepo;
    private final TransactionManager tx;

    public RoomService(RoomRepository roomRepo, TransactionManager tx) {
        this.roomRepo = roomRepo;
        this.tx = tx;
    }

    // ========================
    // CREATE
    // ========================
    public void create(Room room) throws Exception {
        validate(room);

        tx.runInTransaction(em -> {
            roomRepo.insert(em, room);
            return null;
        });
    }

    // ========================
    // UPDATE
    // ========================
    public void update(Room room) throws Exception {
        validate(room);

        tx.runInTransaction(em -> {
            roomRepo.update(em, room);
            return null;
        });
    }

    // ========================
    // DELETE
    // ========================
    public void delete(UUID id) throws Exception {
        tx.runInTransaction(em -> {
            roomRepo.delete(em, id);
            return null;
        });
    }

    // ========================
    // FIND ALL (không cần transaction)
    // ========================
    public List<Room> findAll() {
        EntityManager em = Jpa.em();
        try {
            return roomRepo.findAll(em);
        } finally {
            em.close();
        }
    }

    // ========================
    // VALIDATION
    // ========================
    private void validate(Room room) {

        if (room == null) {
            throw new IllegalArgumentException("Room không được null");
        }

        if (room.getRoomName() == null || room.getRoomName().isBlank()) {
            throw new IllegalArgumentException("Tên phòng không được để trống");
        }

        if (room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Sức chứa phải > 0");
        }
    }
}