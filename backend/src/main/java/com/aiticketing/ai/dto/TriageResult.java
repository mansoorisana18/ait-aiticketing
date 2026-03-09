package com.aiticketing.ai.dto;

import java.math.BigDecimal;
import com.aiticketing.entity.enums.TicketPriority;

public class TriageResult {
    public String category;           	//string from CATEGORY_OPTIONS
    public TicketPriority priority;   	//priority enum
    public BigDecimal confidence;     	//between 0..1
    public Boolean isVague;			  	//ticket is vague or not
    public String vagueReason;		  	//reason given by llm why 
    public String clarificationPrompt;	//extra details to be asked to the end user
}