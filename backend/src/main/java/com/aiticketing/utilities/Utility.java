package com.aiticketing.utilities;

import com.aiticketing.entity.enums.TicketStatus;

public class Utility {

	public static String mapinternalTicketStatustoUserStatus(TicketStatus internalStatus, String duplicateState) {
        if (internalStatus == null) return "OPEN";

        switch (internalStatus) {
	        case NEW:
	        case AI_PROCESSING:
	        case READY:
	            return "OPEN";
	
	        case VAGUE:
	            return "WAITING FOR YOUR INPUT";
	
	        case IN_PROGRESS:
	            return "IN PROGRESS";
	
	        case DUPLICATE:
	            if ("CONFIRMED".equalsIgnoreCase(duplicateState))
	                return "CLOSED (DUPLICATE)";
	            return "OPEN";
	
	        case RESOLVED:
	            return "RESOLVED";
	
	        case CLOSED:
	            return "CLOSED";
	
	        default:
	            return "OPEN";
	    }
    }
}
