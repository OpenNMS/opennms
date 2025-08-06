package org.opennms.netmgt.model.events;

import java.io.Serializable;

public class ParsedEventEntry implements Serializable {

    private final String uei;
    private final String eventLabel;
    private final String description;
    private final Boolean enabled;
    private final String xmlContent;
    private final String vendor;

    private ParsedEventEntry(Builder builder) {
        this.uei = builder.uei;
        this.eventLabel = builder.eventLabel;
        this.description = builder.description;
        this.enabled = builder.enabled;
        this.xmlContent = builder.xmlContent;
        this.vendor = builder.vendor;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String uei;
        private String eventLabel;
        private String description;
        private Boolean enabled = true;
        private String xmlContent;
        private String vendor = "unknown";

        public Builder uei(String uei) {
            this.uei = uei;
            return this;
        }

        public Builder eventLabel(String eventLabel) {
            this.eventLabel = eventLabel;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder xmlContent(String xmlContent) {
            this.xmlContent = xmlContent;
            return this;
        }

        public Builder vendor (String vendor) {
            this.vendor = vendor;
            return this;
        }

        public ParsedEventEntry build() {
            return new ParsedEventEntry(this);
        }
    }

    public String getUei() {
        return uei;
    }

    public String getEventLabel() {
        return eventLabel;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getXmlContent() {
        return xmlContent;
    }
    public String getVendor() {
        return vendor;
    }
}
