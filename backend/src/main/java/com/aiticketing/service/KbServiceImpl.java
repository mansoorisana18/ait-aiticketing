package com.aiticketing.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aiticketing.ai.persistence.KbEmbeddingJdbcRepository;
import com.aiticketing.ai.service.KbEmbeddingGenerationService;
import com.aiticketing.bean.request.CreateKbArticleRequestBean;
import com.aiticketing.bean.request.UpdateKbArticleRequestBean;
import com.aiticketing.bean.response.KbArticleResponseBean;
import com.aiticketing.entity.KbArticle;
import com.aiticketing.entity.User;
import com.aiticketing.entity.enums.KbArticleStatus;
import com.aiticketing.entity.enums.UserRole;
import com.aiticketing.exception.BadRequestException;
import com.aiticketing.exception.NotFoundException;
import com.aiticketing.exception.UnauthorizedException;
import com.aiticketing.repository.KbArticleRepository;
import com.aiticketing.repository.KbSuggestionRepository;
import com.aiticketing.repository.UserRepository;

@Service("KbServiceImpl")
public class KbServiceImpl implements KbService {

    private static final Logger KB_SERVICE_LOG = LoggerFactory.getLogger(KbServiceImpl.class);

    private final KbArticleRepository kbArticleRepo;
    private final KbSuggestionRepository kbSuggestionRepo;
    private final UserRepository userRepo;
    private final KbEmbeddingGenerationService kbEmbeddingGenerationService;
    private final KbEmbeddingJdbcRepository kbEmbeddingJdbcRepository;

    public KbServiceImpl(
            KbArticleRepository kbArticleRepo,
            KbSuggestionRepository kbSuggestionRepo,
            UserRepository userRepo,
            KbEmbeddingGenerationService kbEmbeddingGenerationService,
            KbEmbeddingJdbcRepository kbEmbeddingJdbcRepository
    ) {
        this.kbArticleRepo = kbArticleRepo;
        this.kbSuggestionRepo = kbSuggestionRepo;
        this.userRepo = userRepo;
        this.kbEmbeddingGenerationService = kbEmbeddingGenerationService;
        this.kbEmbeddingJdbcRepository = kbEmbeddingJdbcRepository;
    }

    @Override
    @Transactional
    public KbArticleResponseBean createKbArticle(Long adminUserId, CreateKbArticleRequestBean req) {
        KB_SERVICE_LOG.info("KbServiceImpl :: in createKbArticle() :: adminUserId={} req={}", adminUserId, req);

        User admin = userRepo.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("Only ADMIN can create KB articles");
        }

        OffsetDateTime now = OffsetDateTime.now();
        String finalStatus = Boolean.TRUE.equals(req.publishNow)
                ? KbArticleStatus.PUBLISHED.name()
                : KbArticleStatus.DRAFT.name();

        KbArticle kb = new KbArticle();
        kb.setTitle(req.title.trim());
        kb.setBody(req.body.trim());
        kb.setStatus(finalStatus);
        kb.setCreatedBy(admin);
        kb.setLastModifiedBy(admin);
        kb.setApprovedBy(Boolean.TRUE.equals(req.publishNow) ? admin : null);
        kb.setAiGenerated(false);
        kb.setCreatedAt(now);
        kb.setUpdatedAt(now);
        kb.setAgentSubmittedAt(null);
        kb.setApprovedAt(Boolean.TRUE.equals(req.publishNow) ? now : null);

        KbArticle saved = kbArticleRepo.save(kb);

        if (KbArticleStatus.PUBLISHED.name().equals(saved.getStatus())) {
            refreshKbEmbedding(saved);
        }

        KbArticleResponseBean resp = mapToKbArticleResponse(saved);

        KB_SERVICE_LOG.info("KbServiceImpl :: exit createKbArticle() :: kbId={} status={}",
                resp.kbId, resp.status);

        return resp;
    }

    @Override
    @Transactional
    public KbArticleResponseBean updateKbArticle(Long adminUserId, Long kbId, UpdateKbArticleRequestBean req) {
        KB_SERVICE_LOG.info("KbServiceImpl :: in updateKbArticle() :: adminUserId={} kbId={} req={}",
                adminUserId, kbId, req);

        User admin = userRepo.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new BadRequestException("Only ADMIN can update KB articles");
        }

        KbArticle kb = kbArticleRepo.findById(kbId)
                .orElseThrow(() -> new NotFoundException("KB article not found"));

        OffsetDateTime now = OffsetDateTime.now();

        kb.setTitle(req.title.trim());
        kb.setBody(req.body.trim());
        kb.setLastModifiedBy(admin);
        kb.setUpdatedAt(now);

        if (Boolean.TRUE.equals(req.publishNow)) {
            kb.setStatus(KbArticleStatus.PUBLISHED.name());
            kb.setApprovedBy(admin);
            if (kb.getApprovedAt() == null) {
                kb.setApprovedAt(now);
            }
        } else {
            //Keeping non-published admin edited KBs as DRAFT
            if (!KbArticleStatus.PUBLISHED.name().equals(kb.getStatus())) {
                kb.setStatus(KbArticleStatus.DRAFT.name());
            }
            kb.setApprovedBy(KbArticleStatus.PUBLISHED.name().equals(kb.getStatus()) ? kb.getApprovedBy() : null);
            if (!KbArticleStatus.PUBLISHED.name().equals(kb.getStatus())) {
                kb.setApprovedAt(null);
            }
        }

        KbArticle saved = kbArticleRepo.save(kb);

        if (KbArticleStatus.PUBLISHED.name().equals(saved.getStatus())) {
            refreshKbEmbedding(saved);
        }

        KbArticleResponseBean resp = mapToKbArticleResponse(saved);

        KB_SERVICE_LOG.info("KbServiceImpl :: exit updateKbArticle() :: kbId={} status={}",
                resp.kbId, resp.status);

        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public KbArticleResponseBean getKbArticleById(Long requesterUserId, Long kbId) {
        KB_SERVICE_LOG.info("KbServiceImpl :: in getKbArticleById() :: requesterUserId={} kbId={}",
                requesterUserId, kbId);

        User requester = userRepo.findById(requesterUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        KbArticle kb = kbArticleRepo.findById(kbId)
                .orElseThrow(() -> new NotFoundException("KB article not found"));

        // ADMIN and AGENT can view KB directly
        if (requester.getRole() == UserRole.ADMIN || requester.getRole() == UserRole.AGENT) {
            KbArticleResponseBean resp = mapToKbArticleResponse(kb);
            KB_SERVICE_LOG.info("KbServiceImpl :: exit getKbArticleById() :: role={} kbId={}",
                    requester.getRole(), kbId);
            return resp;
        }

        // USER can only view a KB if it is linked to one of their own tickets
        boolean allowed = kbSuggestionRepo.existsByKbArticle_KbIdAndTicket_CreatedBy_UserId(kbId, requesterUserId);
        if (!allowed) {
            throw new UnauthorizedException("You are not allowed to view this KB article");
        }

        KbArticleResponseBean resp = mapToKbArticleResponse(kb);

        KB_SERVICE_LOG.info("KbServiceImpl :: exit getKbArticleById() :: USER access granted :: kbId={}", kbId);
        return resp;
    }

    @Override
    @Transactional(readOnly = true)
    public List<KbArticleResponseBean> listAllKbArticles() {
        KB_SERVICE_LOG.info("KbServiceImpl :: in listAllKbArticles()");

        List<KbArticleResponseBean> resp = kbArticleRepo.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::mapToKbArticleResponse)
                .toList();

        KB_SERVICE_LOG.info("KbServiceImpl :: exit listAllKbArticles() :: count={}", resp.size());
        return resp;
    }

    private void refreshKbEmbedding(KbArticle kbArticle) {
        KB_SERVICE_LOG.info("KbServiceImpl :: in refreshKbEmbedding() :: kbId={}", kbArticle.getKbId());

        String embeddingVector = kbEmbeddingGenerationService.generateEmbeddingVector(
                kbArticle.getTitle(),
                kbArticle.getBody()
        );

        kbEmbeddingJdbcRepository.upsertEmbedding(kbArticle.getKbId(), embeddingVector);

        KB_SERVICE_LOG.info("KbServiceImpl :: exit refreshKbEmbedding() :: kbId={}", kbArticle.getKbId());
    }

    private KbArticleResponseBean mapToKbArticleResponse(KbArticle kb) {
        KbArticleResponseBean r = new KbArticleResponseBean();
        r.kbId = kb.getKbId();
        r.title = kb.getTitle();
        r.body = kb.getBody();
        r.status = kb.getStatus();
        r.isAiGenerated = kb.getAiGenerated();
        r.sourceTicketId = kb.getSourceTicket() != null ? kb.getSourceTicket().getTicketId() : null;

        if (kb.getCreatedBy() != null) {
            r.createdByUserId = kb.getCreatedBy().getUserId();
            r.createdByName = kb.getCreatedBy().getUsername();
            r.createdByEmail = kb.getCreatedBy().getEmail();
        }

        if (kb.getLastModifiedBy() != null) {
            r.lastModifiedByUserId = kb.getLastModifiedBy().getUserId();
            r.lastModifiedByName = kb.getLastModifiedBy().getUsername();
            r.lastModifiedByEmail = kb.getLastModifiedBy().getEmail();
        }

        if (kb.getApprovedBy() != null) {
            r.approvedByUserId = kb.getApprovedBy().getUserId();
            r.approvedByName = kb.getApprovedBy().getUsername();
            r.approvedByEmail = kb.getApprovedBy().getEmail();
        }

        r.createdAt = kb.getCreatedAt();
        r.updatedAt = kb.getUpdatedAt();
        r.agentSubmittedAt = kb.getAgentSubmittedAt();
        r.approvedAt = kb.getApprovedAt();

        return r;
    }
}