package com.aiticketing.ai.dto;

import java.math.BigDecimal;

public class DuplicateCheckResult {
    
	public String duplicateState;  // NONE / POTENTIAL / CONFIRMED
    public Long primaryTicketId;
    public BigDecimal confidence;
    public BigDecimal similarity;
    public BigDecimal threshold;
    public String reason;
}