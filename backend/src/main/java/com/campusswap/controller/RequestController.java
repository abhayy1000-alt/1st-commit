package com.campusswap.controller;

import com.campusswap.dto.CreateRequestDto;
import com.campusswap.dto.RequestResponse;
import com.campusswap.dto.StatusUpdateRequest;
import com.campusswap.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    /** POST /api/requests  {"itemId": 5} */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponse create(Authentication authentication,
                                  @Valid @RequestBody CreateRequestDto body) {
        return requestService.create(authentication.getName(), body.itemId());
    }

    /** Requests for items I own ("Incoming Requests" section). */
    @GetMapping("/incoming")
    public List<RequestResponse> incoming(Authentication authentication) {
        return requestService.incoming(authentication.getName());
    }

    /** Requests I sent ("My Requests" section). */
    @GetMapping("/outgoing")
    public List<RequestResponse> outgoing(Authentication authentication) {
        return requestService.outgoing(authentication.getName());
    }

    /** PATCH /api/requests/{id}/status  {"status": "Accepted"} or {"status": "Rejected"} */
    @PatchMapping("/{id}/status")
    public RequestResponse respond(Authentication authentication,
                                   @PathVariable Long id,
                                   @Valid @RequestBody StatusUpdateRequest body) {
        return requestService.respond(authentication.getName(), id, body.status());
    }
}
