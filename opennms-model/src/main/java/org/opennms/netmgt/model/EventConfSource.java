package org.opennms.netmgt.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Entity
@Table(name = "eventconf_sources")
public class EventConfSource implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_seq")
    @SequenceGenerator(name = "source_seq", sequenceName = "eventconf_sources_id_seq", allocationSize = 1)
    private Integer id;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 128)
    private String vendor;

    @Column(name = "file_order", nullable = false)
    private Integer fileOrder;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "event_count")
    private Integer eventCount = 0;

    @Column(name = "created_time", nullable = false)
    private ZonedDateTime createdTime;

    @Column(name = "last_modified")
    private ZonedDateTime lastModified;

    @Column(name = "uploaded_by", length = 256)
    private String uploadedBy;

    // One-to-Many mapping to events
    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventConfEvents> events;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Integer getFileOrder() {
        return fileOrder;
    }

    public void setFileOrder(Integer fileOrder) {
        this.fileOrder = fileOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getEventCount() {
        return eventCount;
    }

    public void setEventCount(Integer eventCount) {
        this.eventCount = eventCount;
    }

    public ZonedDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(ZonedDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public ZonedDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(ZonedDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public List<EventConfEvents> getEvents() {
        return events;
    }

    public void setEvents(List<EventConfEvents> events) {
        this.events = events;
    }
}
