package com.aiticketing.ai;

import java.util.Set;

public final class Taxonomy {

    private Taxonomy() {}

    public static final Set<String> CATEGORY_OPTIONS = Set.of(
    		"TECHNICAL SUPPORT",
            "BILLING AND PAYMENTS",
            "ORDERS AND RETURNS",
            "SALES AND PRESALES",
            "ACCOUNT AND ACCESS",
            "GENERAL INQUIRY"
    );

    //bullet list form for sending categories in prompt
    public static String categoriesForPrompt() {
        return """
        - TECHNICAL SUPPORT
        - BILLING AND PAYMENTS
        - ORDERS AND RETURNS
        - SALES AND PRESALES
        - ACCOUNT AND ACCESS
        - GENERAL INQUIRY
        """;
    }

    //handle case sensitivity of our allowed values/enums
    public static String normalize(String s) {
        return s == null ? null : s.trim().toUpperCase();
    }
}