package com.campusswap.controller;

import com.campusswap.dto.ItemCreateRequest;
import com.campusswap.dto.ItemResponse;
import com.campusswap.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /** GET /api/items?category=Books&search=calculus  (both optional; category "All" = everything) */
    @GetMapping
    public List<ItemResponse> browse(@RequestParam(required = false) String category,
                                     @RequestParam(required = false) String search) {
        return itemService.browse(category, search);
    }

    @GetMapping("/mine")
    public List<ItemResponse> mine(Authentication authentication) {
        return itemService.mine(authentication.getName());
    }

    @GetMapping("/{id}")
    public ItemResponse get(@PathVariable Long id) {
        return itemService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse create(Authentication authentication,
                               @Valid @RequestBody ItemCreateRequest request) {
        return itemService.create(authentication.getName(), request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Authentication authentication, @PathVariable Long id) {
        itemService.delete(authentication.getName(), id);
    }
}
