package org.opennms.features.topology.app.internal.gwt.client;

public class SharedVertex {

    private String m_key;
    private int m_initialX;
    private int m_initialY;
    private int m_x;
    private int m_y;
    private boolean m_selected;
    private String m_status;
    private String m_iconUrl;
    private String m_label;
    private String m_tooltipText;
    private String m_statusCount = "0";

    /**
     * @return the statusCount
     */
    public String getStatusCount() {
        return m_statusCount;
    }

    /**
     * @param statusCount the statusCount to set
     */
    public void setStatusCount(String statusCount) {
        this.m_statusCount = statusCount;
    }

    public void setKey(String key) {
        m_key = key;
    }

    public void setInitialX(int x) {
        m_initialX = x;
    }

    public void setInitialY(int y) {
        m_initialY = y;
    }

    public void setX(int x) {
        m_x = x;
    }

    public void setY(int y) {
        m_y = y;
    }

    public void setSelected(boolean selected) {
        m_selected = selected;
    }

    public void setIconUrl(String iconUrl) {
        m_iconUrl = iconUrl;
    }

    public void setLabel(String label) {
        m_label = label;
    }

    public void setTooltipText(String tooltipText) {
        m_tooltipText = tooltipText;
    }

    public String getStatus() {
        return m_status;
    }

    public void setStatus(String status) {
        m_status = status;
    }

    public String getTooltipText() {
        return m_tooltipText;
    }

    public String getKey() {
        return m_key;
    }

    public int getInitialX() {
        return m_initialX;
    }

    public int getInitialY() {
        return m_initialY;
    }

    public int getX() {
        return m_x;
    }

    public int getY() {
        return m_y;
    }

    public boolean isSelected() {
        return m_selected;
    }

    public String getIconUrl() {
        return m_iconUrl;
    }

    public String getLabel() {
        return m_label;
    }

    public boolean getSelected() {
        return m_selected;
    }

}
