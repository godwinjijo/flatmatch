package com.flatmatch.controller;

import com.flatmatch.dto.SwipeRequest;
import com.flatmatch.dto.SwipeResponse;
import com.flatmatch.service.SwipeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** POST /api/swipes — record a like/pass/super-like. */
@RestController
@RequestMapping("/api/swipes")
public class SwipeController {

    private final SwipeService swipeService;

    public SwipeController(SwipeService swipeService) {
        this.swipeService = swipeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SwipeResponse swipe(@Valid @RequestBody SwipeRequest request) {
        return swipeService.recordSwipe(request);
    }
}
