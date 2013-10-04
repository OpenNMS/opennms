package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.features.geocoder.Coordinates;
public class SimpleNodeMarker implements NodeMarker {

    private Integer m_nodeId;
    private String m_foreignSource;
    private String m_foreignId;
    private String m_nodeLabel;
    private String m_description;
    private String m_ipAddress;
    private Integer m_severity;
    private String m_severityLabel;
    private Integer m_unackedCount;
    private String m_maintContract;
    private Coordinates m_coordinates;
    private List<String> m_categoryList = new ArrayList<String>();

    public final Map<String,String> getProperties() {
        final Map<String,String> props = new HashMap<String,String>();

        final Integer nodeId = getNodeId();
        if (nodeId != null) props.put("nodeid", nodeId.toString());

        final Integer severity = getSeverity();
        if (severity != null) props.put("severity", severity.toString());

        final Integer unackedCount = getUnackedCount();
        if (unackedCount != null) props.put("unackedcount", unackedCount.toString());

        final Coordinates coordinates = getCoordinates();
        if (coordinates != null) props.put("coordinates", coordinates.toString());

        if (m_categoryList != null && m_categoryList.size() > 0) {
            final StringBuilder sb = new StringBuilder();
            final Iterator<String> i = getCategoryList().iterator();
            while (i.hasNext()) {
                sb.append(i.next());
                if (i.hasNext()) sb.append(",");
            }
            props.put("categories", sb.toString());
        }

        addIfExists(props, "foreignsource", getForeignSource());
        addIfExists(props, "foreignid", getForeignId());
        addIfExists(props, "nodelabel", getNodeLabel());
        addIfExists(props, "description", getDescription());
        addIfExists(props, "ipaddress", getIpAddress());
        addIfExists(props, "severitylabel", getSeverityLabel());
        addIfExists(props, "maintcontract", getMaintContract());
        return props;
    }

    private final void addIfExists(final Map<String,String> props, final String key, final String value) {
        if (value != null) {
            props.put(key, value);
        }
    }

    @Override
    public Integer getNodeId() {
        return m_nodeId;
    }

    public void setNodeId(final Integer nodeId) {
        m_nodeId = nodeId;
    }

    @Override
    public String getForeignSource() {
        return m_foreignSource;
    }

    public void setForeignSource(final String foreignSource) {
        m_foreignSource = foreignSource;
    }

    @Override
    public String getForeignId() {
        return m_foreignId;
    }

    public void setForeignId(final String foreignId) {
        m_foreignId = foreignId;
    }

    @Override
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public void setNodeLabel(final String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }

    @Override
    public String getDescription() {
        return m_description;
    }

    public void setDescription(final String description) {
        m_description = description;
    }

    @Override
    public String getIpAddress() {
        return m_ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        m_ipAddress = ipAddress;
    }

    @Override
    public Integer getSeverity() {
        return m_severity;
    }

    public void setSeverity(final Integer severity) {
        m_severity = severity;
    }

    @Override
    public String getSeverityLabel() {
        return m_severityLabel;
    }

    public void setSeverityLabel(final String severityLabel) {
        m_severityLabel = severityLabel;
    }

    @Override
    public Integer getUnackedCount() {
        return m_unackedCount;
    }

    public void setUnackedCount(final Integer unackedCount) {
        m_unackedCount = unackedCount;
    }

    @Override
    public String getMaintContract() {
        return m_maintContract;
    }

    public void setMaintContract(final String maintContract) {
        m_maintContract = maintContract;
    }

    @Override
    public Coordinates getCoordinates() {
        return m_coordinates;
    }

    public void setCoordinates(final Coordinates coordinates) {
        m_coordinates = coordinates;
    }

    @Override
    public List<String> getCategoryList() {
        return Collections.unmodifiableList(new ArrayList<String>(m_categoryList));
    }

    public void setCategoryList(final List<String> categoryList) {
        m_categoryList = new ArrayList<String>(categoryList);
    }

    public void addCategory(final String category) {
        m_categoryList.add(category);
    }
}
