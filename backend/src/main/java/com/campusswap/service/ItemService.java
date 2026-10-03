package com.campusswap.service;

import com.campusswap.dto.ItemCreateRequest;
import com.campusswap.dto.ItemResponse;
import com.campusswap.exception.ApiException;
import com.campusswap.model.Item;
import com.campusswap.model.NotificationType;
import com.campusswap.model.Request;
import com.campusswap.model.RequestStatus;
import com.campusswap.model.User;
import com.campusswap.repository.ItemRepository;
import com.campusswap.repository.RequestRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final RequestRepository requestRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public ItemService(ItemRepository itemRepository,
                       RequestRepository requestRepository,
                       AuthService authService,
                       NotificationService notificationService) {
        this.itemRepository = itemRepository;
        this.requestRepository = requestRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    /**
     * Browse listings. Mirrors the dashboard: optional category ("All" = no filter) and a free-text
     * search over name, category, description, condition, listing type, location and owner name.
     */
    @Transactional(readOnly = true)
    public List<ItemResponse> browse(String category, String search) {
        Specification<Item> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (category != null && !category.isBlank() && !"All".equalsIgnoreCase(category.trim())) {
                predicates.add(cb.equal(cb.lower(root.get("category")),
                        category.trim().toLowerCase(Locale.ROOT)));
            }

            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
                Join<Item, User> owner = root.join("owner", JoinType.INNER);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.<String>get("name")), like),
                        cb.like(cb.lower(root.<String>get("category")), like),
                        cb.like(cb.lower(root.<String>get("description")), like),
                        cb.like(cb.lower(root.<String>get("condition")), like),
                        cb.like(cb.lower(root.<String>get("listingType")), like),
                        cb.like(cb.lower(root.<String>get("location")), like),
                        cb.like(cb.lower(owner.<String>get("name")), like)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return itemRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(ItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> mine(String email) {
        User user = authService.requireUser(email);
        return itemRepository.findByOwnerIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(ItemResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ItemResponse get(Long id) {
        return ItemResponse.from(findItem(id));
    }

    @Transactional
    public ItemResponse create(String email, ItemCreateRequest request) {
        User owner = authService.requireUser(email);

        Item item = new Item();
        item.setOwner(owner);
        item.setName(request.name().trim());
        item.setCategory(request.category());
        item.setDescription(request.description() == null ? "" : request.description().trim());
        item.setCondition(request.condition());
        item.setLocation(request.location().trim());
        item.setListingType(request.listingType());

        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public void delete(String email, Long id) {
        User user = authService.requireUser(email);
        Item item = findItem(id);

        if (!item.getOwner().getId().equals(user.getId())) {
            throw ApiException.forbidden("You can only remove your own listings.");
        }

        List<Request> requests = requestRepository.findByItemId(id);
        for (Request r : requests) {
            if (r.getStatus() == RequestStatus.PENDING) {
                notificationService.create(r.getRequester(), "Listing removed",
                        "\"" + item.getName() + "\" was removed by its owner, so your request was cancelled.",
                        NotificationType.INFO, r.getId());
            }
        }

        requestRepository.deleteAll(requests);
        itemRepository.delete(item);
    }

    private Item findItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Item not found."));
    }
}
