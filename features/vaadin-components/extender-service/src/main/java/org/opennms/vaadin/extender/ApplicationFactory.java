package org.opennms.vaadin.extender;

import com.vaadin.ui.UI;

import java.util.Map;

public interface ApplicationFactory {

    Class<? extends UI> getUIClass();

    UI createUI();

    Map<String,String> getAdditionalHeaders();
}
