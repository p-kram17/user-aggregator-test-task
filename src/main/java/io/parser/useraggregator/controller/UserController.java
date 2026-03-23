package io.parser.useraggregator.controller;

import io.parser.useraggregator.dto.UserFilterRequest;
import io.parser.useraggregator.dto.UserResponse;
import io.parser.useraggregator.service.UserAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "Aggregate users from multiple configured data sources")
public class UserController {

    private final UserAggregationService userAggregationService;

    @GetMapping
    @Operation(summary = "Get aggregated users",
            description = "Returns unique users merged from all configured data sources")
    public ResponseEntity<List<UserResponse>> getUsers(
            @Parameter(description = "Filter by username")
            @RequestParam(required = false)
            @Size(max = 255) String username,
            @Parameter(description = "Filter by name")
            @RequestParam(required = false)
            @Size(max = 255) String name,
            @Parameter(description = "Filter by surname")
            @RequestParam(required = false)
            @Size(max = 255) String surname
    ) {
        return ResponseEntity.ok(userAggregationService
                .getUsers(new UserFilterRequest(username, name, surname)));
    }
}
