package org.opennms.netmgt.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EventConfSourceDto {

    private Long id;
    private String name;
    private String description;
    private String vendor;
    private Integer fileOrder;
    private Boolean enabled;
    private Integer eventCount;
    private Date createdTime;
    private Date lastModified;
    private String uploadedBy;

    // All-args constructor
    public EventConfSourceDto(Long id, String name, String description, String vendor,
                              Integer fileOrder, Boolean enabled, Integer eventCount,
                              Date createdTime, Date lastModified, String uploadedBy) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.vendor = vendor;
        this.fileOrder = fileOrder;
        this.enabled = enabled;
        this.eventCount = eventCount;
        this.createdTime = createdTime;
        this.lastModified = lastModified;
        this.uploadedBy = uploadedBy;
    }

    // Getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public Integer getFileOrder() { return fileOrder; }
    public void setFileOrder(Integer fileOrder) { this.fileOrder = fileOrder; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Integer getEventCount() { return eventCount; }
    public void setEventCount(Integer eventCount) { this.eventCount = eventCount; }

    public Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }

    public Date getLastModified() { return lastModified; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    // Mapper for single entity
    public static EventConfSourceDto fromEntity(EventConfSource entity) {
        return new EventConfSourceDto(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getVendor(),
                entity.getFileOrder(),
                entity.getEnabled(),
                entity.getEventCount(),
                entity.getCreatedTime(),
                entity.getLastModified(),
                entity.getUploadedBy()
        );
    }

    // Mapper for list
    public static List<EventConfSourceDto> fromEntity(List<EventConfSource> entityList) {
        if (entityList == null ) return  Collections.emptyList();
        return entityList.stream()
                .map(EventConfSourceDto::fromEntity)
                .collect(Collectors.toList());
    }
}
