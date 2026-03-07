package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Room;

import java.util.List;
import java.util.UUID;

public interface RoomRepository {

    void insert(EntityManager em, Room room);

    void update(EntityManager em, Room room);

    void delete(EntityManager em, UUID id);

    Room findById(EntityManager em, UUID id);

    List<Room> findAll(EntityManager em);
}