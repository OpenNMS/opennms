package org.opennms.features.reporting.model.basicreport;

public interface BasicReportDefinition {
    
    public abstract String getDescription();

    public abstract String getDisplayName();

    public abstract String getId();

    public abstract boolean getOnline();

    public abstract String getReportService();

    public abstract void setId(String id);

    public abstract void setDisplayName(String displayName);

    public abstract void setReportService(String reportService);

    public abstract void setDescription(String description);

    public abstract void setOnline(boolean online);
}
