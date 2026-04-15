package com.aiticketing.service;

import java.util.List;

import com.aiticketing.bean.request.CreateKbArticleRequestBean;
import com.aiticketing.bean.request.KbReviewDecisionRequestBean;
import com.aiticketing.bean.request.UpdateKbArticleRequestBean;
import com.aiticketing.bean.request.UpdateKbDraftRequestBean;
import com.aiticketing.bean.response.KbArticleResponseBean;

public interface KbService {

    KbArticleResponseBean createKbArticle(Long adminUserId, CreateKbArticleRequestBean req);

    KbArticleResponseBean updateKbArticle(Long adminUserId, Long kbId, UpdateKbArticleRequestBean req);

    KbArticleResponseBean getKbArticleById(Long requestedUserId, Long kbId);

    List<KbArticleResponseBean> listAllKbArticles();
    
    List<KbArticleResponseBean> listPublishedKbArticles();
    
    KbArticleResponseBean updateDraftByAgent(Long agentUserId, Long kbId, UpdateKbDraftRequestBean req);

    KbArticleResponseBean submitDraftForReview(Long agentUserId, Long kbId);

    List<KbArticleResponseBean> listKbDraftsForReview();

    KbArticleResponseBean reviewDecision(Long adminUserId, Long kbId, KbReviewDecisionRequestBean req);
}