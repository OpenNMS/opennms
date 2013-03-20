package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search;

import com.google.gwt.user.client.ui.TextBox;

public final class MockTextBox extends TextBox {
    String m_value = "";
    public MockTextBox() {}
    
    @Override public String getValue() {
        return m_value;
    }
    @Override public void setValue(final String value) {
        m_value = value;
    }
}