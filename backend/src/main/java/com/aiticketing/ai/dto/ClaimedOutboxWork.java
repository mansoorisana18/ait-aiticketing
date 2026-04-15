package com.aiticketing.ai.dto;

import java.util.List;

public class ClaimedOutboxWork {

	//This class is returned by the claim transaction so that we can call llm outside of a db transaction
	//Basically for rollback safety & tracking ai pipeline state
    
	public Long outboxId;
    public String eventType;
    public Long aggregateId;
    public Integer textVersion;

    public String ticketTitle;
    public String ticketDescription;
    public String ticketAiCategory;
    
    public List<Long> selectedCommentIds;
}