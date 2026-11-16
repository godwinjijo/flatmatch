package com.flatmatch.controller;

import com.flatmatch.dto.MatchItem;
import com.flatmatch.service.MatchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** GET /api/matches/{userId} — a user's mutual matches, newest first. */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/{userId}")
    public List<MatchItem> getMatches(@PathVariable Long userId) {
        return matchService.getMatchesFor(userId);
    }
}
