package com.campusswap.repository;

import com.campusswap.model.Request;
import com.campusswap.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    /** Requests I made. */
    List<Request> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);

    /** Requests other students made for items I own. */
    List<Request> findByItemOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Request> findByItemId(Long itemId);

    List<Request> findByItemIdAndStatus(Long itemId, RequestStatus status);

    boolean existsByItemIdAndRequesterIdAndStatusNot(Long itemId, Long requesterId, RequestStatus status);
}
