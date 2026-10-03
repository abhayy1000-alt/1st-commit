package com.campusswap.service;

import com.campusswap.dto.RequestResponse;
import com.campusswap.exception.ApiException;
import com.campusswap.model.Item;
import com.campusswap.model.NotificationType;
import com.campusswap.model.Request;
import com.campusswap.model.RequestStatus;
import com.campusswap.model.User;
import com.campusswap.repository.ItemRepository;
import com.campusswap.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public RequestService(RequestRepository requestRepository,
                          ItemRepository itemRepository,
                          AuthService authService,
                          NotificationService notificationService) {
        this.requestRepository = requestRepository;
        this.itemRepository = itemRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    /** A student requests someone else's item. Notifies the owner. */
    @Transactional
    public RequestResponse create(String requesterEmail, Long itemId) {
        User requester = authService.requireUser(requesterEmail);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> ApiException.notFound("Item not found."));

        if (item.getOwner().getId().equals(requester.getId())) {
            throw ApiException.badRequest("You cannot request your own item.");
        }
        if (!item.isAvailable()) {
            throw ApiException.conflict("This item is no longer available.");
        }
        // Same rule as the frontend: a rejected request may be re-sent, anything else blocks a duplicate.
        if (requestRepository.existsByItemIdAndRequesterIdAndStatusNot(
                itemId, requester.getId(), RequestStatus.REJECTED)) {
            throw ApiException.conflict("You have already requested this item.");
        }

        Request request = new Request();
        request.setItem(item);
        request.setRequester(requester);
        request.setStatus(RequestStatus.PENDING);
        Request saved = requestRepository.save(request);

        notificationService.create(item.getOwner(), "New Item Request",
                requester.getName() + " requested your item \"" + item.getName() + "\".",
                NotificationType.INFO, saved.getId());

        return RequestResponse.from(saved);
    }

    /** Requests other students sent for items I own. */
    @Transactional(readOnly = true)
    public List<RequestResponse> incoming(String ownerEmail) {
        User owner = authService.requireUser(ownerEmail);
        return requestRepository.findByItemOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .map(RequestResponse::from)
                .toList();
    }

    /** Requests I sent. */
    @Transactional(readOnly = true)
    public List<RequestResponse> outgoing(String requesterEmail) {
        User requester = authService.requireUser(requesterEmail);
        return requestRepository.findByRequesterIdOrderByCreatedAtDesc(requester.getId())
                .stream()
                .map(RequestResponse::from)
                .toList();
    }

    /**
     * The item owner accepts or rejects a pending request. The requester is notified.
     * Accepting also marks the item unavailable and auto-rejects any other pending requests for it.
     */
    @Transactional
    public RequestResponse respond(String ownerEmail, Long requestId, String statusLabel) {
        User owner = authService.requireUser(ownerEmail);
        RequestStatus newStatus = RequestStatus.fromLabel(statusLabel);

        if (newStatus == RequestStatus.PENDING) {
            throw ApiException.badRequest("Invalid status. Use 'Accepted' or 'Rejected'.");
        }

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("Request could not be found."));
        Item item = request.getItem();

        if (!item.getOwner().getId().equals(owner.getId())) {
            throw ApiException.forbidden("Only the item's owner can respond to this request.");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw ApiException.conflict("This request has already been " + request.getStatus().getLabel().toLowerCase() + ".");
        }
        if (newStatus == RequestStatus.ACCEPTED && !item.isAvailable()) {
            throw ApiException.conflict("This item is no longer available.");
        }

        request.setStatus(newStatus);
        request.setUpdatedAt(Instant.now());
        requestRepository.save(request);

        boolean accepted = newStatus == RequestStatus.ACCEPTED;
        notificationService.create(request.getRequester(),
                "Request " + newStatus.getLabel(),
                "Your request for \"" + item.getName() + "\" was " + newStatus.getLabel().toLowerCase() + ".",
                accepted ? NotificationType.SUCCESS : NotificationType.ERROR,
                request.getId());

        if (accepted) {
            item.setAvailable(false);
            itemRepository.save(item);
            rejectOtherPending(item, request);
        }

        return RequestResponse.from(request);
    }

    private void rejectOtherPending(Item item, Request accepted) {
        List<Request> others = requestRepository.findByItemIdAndStatus(item.getId(), RequestStatus.PENDING);
        for (Request other : others) {
            if (other.getId().equals(accepted.getId())) {
                continue;
            }
            other.setStatus(RequestStatus.REJECTED);
            other.setUpdatedAt(Instant.now());
            requestRepository.save(other);

            notificationService.create(other.getRequester(), "Request Rejected",
                    "\"" + item.getName() + "\" was given to another student, so your request was rejected.",
                    NotificationType.ERROR, other.getId());
        }
    }
}
