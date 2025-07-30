package org.opennms.netmgt.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "eventconf_events")
public class EventConfEvents implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_seq")
    @SequenceGenerator(name = "event_seq", sequenceName = "eventconf_events_id_seq", allocationSize = 1)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private EventConfSource source;

    @Column(nullable = false, length = 256)
    private String uei;

    @Column(name = "event_label", columnDefinition = "text")
    private String eventLabel;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "xml_content", columnDefinition = "text", nullable = false)
    private String xmlContent;

    @Column(name = "created_time", nullable = false)
    private ZonedDateTime createdTime;

    @Column(name = "last_modified")
    private ZonedDateTime lastModified;

    @Column(name = "modified_by", length = 256)
    private String modifiedBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EventConfSource getSource() {
        return source;
    }

    public void setSource(EventConfSource source) {
        this.source = source;
    }

    public String getUei() {
        return uei;
    }

    public void setUei(String uei) {
        this.uei = uei;
    }

    public String getEventLabel() {
        return eventLabel;
    }

    public void setEventLabel(String eventLabel) {
        this.eventLabel = eventLabel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getXmlContent() {
        return xmlContent;
    }

    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
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

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }
}
