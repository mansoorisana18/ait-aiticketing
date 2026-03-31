package com.aiticketing.service;

import com.aiticketing.bean.response.AiSummaryMetricsResponseBean;
import com.aiticketing.bean.response.TicketSummaryMetricsResponseBean;

public interface MetricsService {
	
	AiSummaryMetricsResponseBean getAdminAiSummaryMetrics();
	TicketSummaryMetricsResponseBean getAdminTicketSummaryMetrics();
	TicketSummaryMetricsResponseBean getAgentTicketSummaryMetrics(Long agetUserId);
}