package com.hotel.ai.context;

import com.hotel.ai.dto.DateRange;
import org.springframework.security.core.Authentication;

public class ToolContext {
    private Authentication auth;
    private String rawUserMessage;
    private DateRange dateRange;

    public ToolContext(Authentication auth, String rawUserMessage) {
        this.auth = auth;
        this.rawUserMessage = rawUserMessage;
    }

    public ToolContext(Authentication auth, String rawUserMessage, DateRange dateRange) {
        this.auth = auth;
        this.rawUserMessage = rawUserMessage;
        this.dateRange = dateRange;
    }

    @Override
    public String toString() {
        String toolContextStr = (dateRange != null) ? " DateRange: [" + dateRange.getFrom() + " to " + dateRange.getTo() + "]" : "";
        toolContextStr += " Auth: [" + auth.getAuthorities() + "]" + " rawUserMessage: [" + rawUserMessage + "]";
        return "ToolContext{" + toolContextStr + "}";
    }

    public Authentication getAuth() {return auth;}
    public void setAuth(Authentication auth) { this.auth = auth; }
    public String getRawUserMessage() { return rawUserMessage; }
    public void setRawUserMessage(String rawUserMessage) { this.rawUserMessage = rawUserMessage; }
    public DateRange getDateRange() { return dateRange;}
    public void setDateRange(DateRange dateRange) { this.dateRange = dateRange;}
}
