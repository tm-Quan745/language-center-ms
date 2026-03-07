package vn.edu.ute.productmgmt.repo.jpa;

import jakarta.persistence.EntityManager;
import vn.edu.ute.productmgmt.model.Room;
import vn.edu.ute.productmgmt.repo.RoomRepository;

import java.util.List;
import java.util.UUID;

public class JpaRoomRepository implements RoomRepository {

    @Override
    public void insert(EntityManager em, Room room) {
        em.persist(room);
    }

    @Override
    public void update(EntityManager em, Room room) {
        em.merge(room);
    }

    @Override
    public void delete(EntityManager em, UUID id) {
        Room room = em.find(Room.class, id);
        if (room != null) {
            em.remove(room);
        }
    }

    @Override
    public Room findById(EntityManager em, UUID id) {
        return em.find(Room.class, id);
    }

    @Override
    public List<Room> findAll(EntityManager em) {
        return em.createQuery("SELECT r FROM Room r", Room.class)
                .getResultList();
    }
}