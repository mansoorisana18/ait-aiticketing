package com.aiticketing.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.aiticketing.bean.request.CreateKbArticleRequestBean;
import com.aiticketing.bean.request.KbReviewDecisionRequestBean;
import com.aiticketing.bean.request.UpdateKbArticleRequestBean;
import com.aiticketing.bean.request.UpdateKbDraftRequestBean;
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
        description = "Allows an admin to create a KB article. Admin-created KB articles are published immediately and their embeddings are generated."
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
        description = "Allows an admin to update a KB article. Admin-updated KB articles remain published and their embeddings are refreshed."
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
        summary = "Agent: edit KB draft",
        description = "Allows the draft creator agent to edit a KB draft while it is still in DRAFT state."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "KB draft updated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseBean.class))),
        @ApiResponse(responseCode = "400", description = "KB draft is not editable"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "KB draft not found")
    })
    @PutMapping("/agent/{kbId}/draft")
    public ResponseEntity<ApiResponseBean<KbArticleResponseBean>> updateDraftByAgent(
            @PathVariable Long kbId,
            @AuthenticationPrincipal AuthUserPrincipal principal,
            @Valid @RequestBody UpdateKbDraftRequestBean req) {

        KB_CONTROLLER_LOG.info("KbController :: in updateDraftByAgent() :: kbId={}", kbId);
        Long agentUserId = principal.getUserId();
        KbArticleResponseBean resp = kbService.updateDraftByAgent(agentUserId, kbId, req);
        KB_CONTROLLER_LOG.info("KbController :: exit updateDraftByAgent() :: kbId={}", kbId);

        return ResponseEntity.ok(ApiResponseBean.success("KB draft updated", resp));
    }

    @Operation(
        summary = "Agent: submit KB draft for review",
        description = "Moves a KB draft from DRAFT to IN_REVIEW."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "KB draft submitted for review",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseBean.class))),
        @ApiResponse(responseCode = "400", description = "KB draft is not in DRAFT state"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "KB draft not found")
    })
    @PostMapping("/agent/{kbId}/submit-review")
    public ResponseEntity<ApiResponseBean<KbArticleResponseBean>> submitDraftForReview(
            @PathVariable Long kbId,
            @AuthenticationPrincipal AuthUserPrincipal principal) {

        KB_CONTROLLER_LOG.info("KbController :: in submitDraftForReview() :: kbId={}", kbId);
        Long agentUserId = principal.getUserId();
        KbArticleResponseBean resp = kbService.submitDraftForReview(agentUserId, kbId);
        KB_CONTROLLER_LOG.info("KbController :: exit submitDraftForReview() :: kbId={}", kbId);

        return ResponseEntity.ok(ApiResponseBean.success("KB draft submitted for review", resp));
    }

    @Operation(
        summary = "Admin: list KB drafts in review",
        description = "Returns KB drafts currently waiting for admin review."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "KB review drafts fetched successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseBean.class)))
    })
    @GetMapping("/admin/review")
    public ResponseEntity<ApiResponseBean<List<KbArticleResponseBean>>> listKbDraftsForReview() {
        KB_CONTROLLER_LOG.info("KbController :: in listKbDraftsForReview()");
        List<KbArticleResponseBean> resp = kbService.listKbDraftsForReview();
        KB_CONTROLLER_LOG.info("KbController :: exit listKbDraftsForReview() :: count={}", resp.size());

        return ResponseEntity.ok(ApiResponseBean.success(resp));
    }

    @Operation(
        summary = "Admin: review decision for KB draft",
        description = "Allows admin to APPROVE or REJECT a KB draft in IN_REVIEW state. APPROVE publishes the KB and refreshes its embedding."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "KB review decision applied successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponseBean.class))),
        @ApiResponse(responseCode = "400", description = "Invalid review action or KB not in review"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "KB draft not found")
    })
    @PostMapping("/admin/{kbId}/review-decision")
    public ResponseEntity<ApiResponseBean<KbArticleResponseBean>> reviewDecision(
            @PathVariable Long kbId,
            @AuthenticationPrincipal AuthUserPrincipal principal,
            @Valid @RequestBody KbReviewDecisionRequestBean req) {

        KB_CONTROLLER_LOG.info("KbController :: in reviewDecision() :: kbId={}", kbId);
        Long adminUserId = principal.getUserId();
        KbArticleResponseBean resp = kbService.reviewDecision(adminUserId, kbId, req);
        KB_CONTROLLER_LOG.info("KbController :: exit reviewDecision() :: kbId={}", kbId);

        return ResponseEntity.ok(ApiResponseBean.success("KB review decision applied", resp));
    }

    @Operation(
        summary = "Get KB article by id",
        description = "Returns a KB article by id. ADMIN and AGENT can view KBs directly. USER can only view KB articles suggested on their own tickets."
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
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "KB article not found")
    })
    @GetMapping("/{kbId}")
    public ResponseEntity<ApiResponseBean<KbArticleResponseBean>> getKbArticleById(
            @PathVariable Long kbId,
            @AuthenticationPrincipal AuthUserPrincipal principal) {

        KB_CONTROLLER_LOG.info("KbController :: in getKbArticleById() :: kbId={}", kbId);
        Long requesterUserId = principal.getUserId();
        KbArticleResponseBean resp = kbService.getKbArticleById(requesterUserId, kbId);
        KB_CONTROLLER_LOG.info("KbController :: exit getKbArticleById() :: kbId={}", kbId);

        return ResponseEntity.ok(ApiResponseBean.success(resp));
    }

    @Operation(
        summary = "Admin: list KB articles",
        description = "Returns all KB articles for admin management."
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
    
    @Operation(
	    summary = "List published KB articles",
	    description = "Returns published KB articles for browsing and manual suggestion."
	)
	@ApiResponses(value = {
	    @ApiResponse(
	        responseCode = "200",
	        description = "Published KB articles fetched successfully",
	        content = @Content(
	            mediaType = "application/json",
	            schema = @Schema(implementation = ApiResponseBean.class)
	        )
	    )
	})
	@GetMapping
	public ResponseEntity<ApiResponseBean<List<KbArticleResponseBean>>> listPublishedKbArticles() {
	    KB_CONTROLLER_LOG.info("KbController :: in listPublishedKbArticles()");
	    List<KbArticleResponseBean> resp = kbService.listPublishedKbArticles();
	    KB_CONTROLLER_LOG.info("KbController :: exit listPublishedKbArticles() :: count={}", resp.size());

	    return ResponseEntity.ok(ApiResponseBean.success(resp));
	}
}