package org.opennms.features.topology.app.internal.gwt.client;

public class SharedEdge {

    private String m_key;
    private String m_sourceKey;
    private String m_targetKey;
    private boolean m_selected;
    private String m_styleName;
    private String m_tooltipText;

    public void setKey(String key) {
        m_key = key;
    }

    public void setSourceKey(String sourceKey) {
        m_sourceKey = sourceKey;
    }

    public void setTargetKey(String targetKey) {
        m_targetKey = targetKey;
    }

    public void setSelected(boolean selected) {
        m_selected = selected;
    }

    public void setCssClass(String styleName) {
        m_styleName = styleName;
    }
    
    public String getCssClass() {
        return m_styleName;
    }

    public void setTooltipText(String tooltipText) {
        m_tooltipText = tooltipText;
    }

    public String getStyleName() {
        return m_styleName;
    }

    public void setStyleName(String styleName) {
        m_styleName = styleName;
    }

    public String getKey() {
        return m_key;
    }

    public String getSourceKey() {
        return m_sourceKey;
    }

    public String getTargetKey() {
        return m_targetKey;
    }

    public boolean isSelected() {
        return m_selected;
    }

    public String getTooltipText() {
        return m_tooltipText;
    }

    public boolean getSelected() {
        return m_selected;
    }

}
