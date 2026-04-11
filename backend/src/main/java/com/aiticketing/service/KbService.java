package com.aiticketing.service;

import java.util.List;

import com.aiticketing.bean.request.CreateKbArticleRequestBean;
import com.aiticketing.bean.request.UpdateKbArticleRequestBean;
import com.aiticketing.bean.response.KbArticleResponseBean;

public interface KbService {

    KbArticleResponseBean createKbArticle(Long adminUserId, CreateKbArticleRequestBean req);

    KbArticleResponseBean updateKbArticle(Long adminUserId, Long kbId, UpdateKbArticleRequestBean req);

    KbArticleResponseBean getKbArticleById(Long kbId);

    List<KbArticleResponseBean> listAllKbArticles();
}