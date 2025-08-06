package org.opennms.netmgt.model.events;

import java.util.Date;


public class EventConfSourceMetadataDto {
    private String filename;
    private int eventCount;
    private int fileOrder;
    private String username;
    private Date now;
    private String vendor;
    private String description;

    // Private constructor to enforce use of builder
    private EventConfSourceMetadataDto(Builder builder) {
        this.filename = builder.filename;
        this.eventCount = builder.eventCount;
        this.fileOrder = builder.fileOrder;
        this.username = builder.username;
        this.now = builder.now;
        this.vendor = builder.vendor;
        this.description = builder.description;
    }

    // Getters and setters
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getEventCount() {
        return eventCount;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public int getFileOrder() {
        return fileOrder;
    }

    public void setFileOrder(int fileOrder) {
        this.fileOrder = fileOrder;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getNow() {
        return now;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Builder class
    public static class Builder {
        private String filename;
        private int eventCount;
        private int fileOrder;
        private String username;
        private Date now;
        private String vendor;
        private String description;

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder eventCount(int eventCount) {
            this.eventCount = eventCount;
            return this;
        }

        public Builder fileOrder(int fileOrder) {
            this.fileOrder = fileOrder;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder now(Date now) {
            this.now = now;
            return this;
        }

        public Builder vendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public EventConfSourceMetadataDto build() {
            return new EventConfSourceMetadataDto(this);
        }
    }
}
