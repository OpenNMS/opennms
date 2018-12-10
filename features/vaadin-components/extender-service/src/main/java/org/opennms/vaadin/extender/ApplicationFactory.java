package org.opennms.vaadin.extender;

import java.util.Map;

import com.vaadin.ui.UI;

public interface ApplicationFactory {

    Class<? extends UI> getUIClass();

    UI createUI();

    Map<String,String> getAdditionalHeaders();
}
