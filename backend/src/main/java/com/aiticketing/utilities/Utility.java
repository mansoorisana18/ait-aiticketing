package com.aiticketing.utilities;

import com.aiticketing.entity.enums.TicketStatus;

public class Utility {

	public static String mapinternalTicketStatustoUserStatus(TicketStatus internalStatus, String duplicateState) {
        if (internalStatus == null) return "OPEN";

        switch (internalStatus) {
	        case NEW:
	        case AI_PROCESSING:
	        case READY:
	        case DUPLICATE_REVIEW:
	        case DUPLICATE: //just a fallback for duplicate
	            return "OPEN";
	
	        case VAGUE:
	        case KB_SUGGESTED:
	            return "WAITING FOR YOUR INPUT";
	
	        case IN_PROGRESS:
	            return "IN PROGRESS";
	
	        case RESOLVED:
	            return "RESOLVED";
	
	        case CLOSED:
	            return "CLOSED";
	
	        default:
	            return "OPEN";
	    }
    }
}
