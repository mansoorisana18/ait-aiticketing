package com.aiticketing.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.aiticketing.bean.request.CreateKbArticleRequestBean;
import com.aiticketing.bean.request.UpdateKbArticleRequestBean;
import com.aiticketing.bean.response.ApiResponseBean;
import com.aiticketing.bean.response.KbArticleResponseBean;
import com.aiticketing.security.AuthUserPrincipal;
import com.aiticketing.service.KbService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/kb")
public class KbController {

    private static final Logger KB_CONTROLLER_LOG = LoggerFactory.getLogger(KbController.class);

    private final KbService kbService;

    public KbController(KbService kbService) {
        this.kbService = kbService;
    }

    @Operation(
        summary = "Admin: create KB article",
        description = "Allows an admin to create a KB article. If publishNow is true, the article is published immediately and its embedding is generated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "KB article created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseBean.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/admin")
    public ResponseEntity<ApiResponseBean<KbArticleResponseBean>> createKbArticle(
            @AuthenticationPrincipal AuthUserPrincipal principal,
            @Valid @RequestBody CreateKbArticleRequestBean req) {

        KB_CONTROLLER_LOG.info("KbController :: in createKbArticle()");
        Long adminUserId = principal.getUserId();
        KbArticleResponseBean resp = kbService.createKbArticle(adminUserId, req);
        KB_CONTROLLER_LOG.info("KbController :: exit createKbArticle() :: kbId={}", resp.kbId);

        return ResponseEntity.status(201).body(ApiResponseBean.success("KB article created", resp));
    }

    @Operation(
        summary = "Admin: update KB article",
        description = "Allows an admin to update a KB article. If publishNow is true, the article becomes published and its embedding is generated or refreshed."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "KB article updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseBean.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "KB article or user not found")
    })
    @PutMapping("/admin/{kbId}")
    public ResponseEntity<ApiResponseBean<KbArticleResponseBean>> updateKbArticle(
            @PathVariable Long kbId,
            @AuthenticationPrincipal AuthUserPrincipal principal,
            @Valid @RequestBody UpdateKbArticleRequestBean req) {

        KB_CONTROLLER_LOG.info("KbController :: in updateKbArticle() :: kbId={}", kbId);
        Long adminUserId = principal.getUserId();
        KbArticleResponseBean resp = kbService.updateKbArticle(adminUserId, kbId, req);
        KB_CONTROLLER_LOG.info("KbController :: exit updateKbArticle() :: kbId={}", kbId);

        return ResponseEntity.ok(ApiResponseBean.success("KB article updated", resp));
    }

    @Operation(
        summary = "Get KB article by id",
        description = "Returns a KB article by id."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "KB article fetched successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseBean.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "KB article not found")
    })
    @GetMapping("/{kbId}")
    public ResponseEntity<ApiResponseBean<KbArticleResponseBean>> getKbArticleById(@PathVariable Long kbId) {
        KB_CONTROLLER_LOG.info("KbController :: in getKbArticleById() :: kbId={}", kbId);
        KbArticleResponseBean resp = kbService.getKbArticleById(kbId);
        KB_CONTROLLER_LOG.info("KbController :: exit getKbArticleById() :: kbId={}", kbId);

        return ResponseEntity.ok(ApiResponseBean.success(resp));
    }

    @Operation(
        summary = "List KB articles",
        description = "Returns all KB articles."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "KB articles fetched successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ApiResponseBean.class)
            )
        )
    })
    @GetMapping("/admin")
    public ResponseEntity<ApiResponseBean<List<KbArticleResponseBean>>> listAllKbArticles() {
        KB_CONTROLLER_LOG.info("KbController :: in listAllKbArticles()");
        List<KbArticleResponseBean> resp = kbService.listAllKbArticles();
        KB_CONTROLLER_LOG.info("KbController :: exit listAllKbArticles() :: count={}", resp.size());

        return ResponseEntity.ok(ApiResponseBean.success(resp));
    }
}