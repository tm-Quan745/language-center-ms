package vn.edu.ute.productmgmt.repo;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Room;

import java.util.List;

public interface RoomRepository {

    void insert(EntityManager em, Room room);

    void update(EntityManager em, Room room);

    void delete(EntityManager em, Long id);

    Room findById(EntityManager em, Long id);

    List<Room> findAll(EntityManager em);
}