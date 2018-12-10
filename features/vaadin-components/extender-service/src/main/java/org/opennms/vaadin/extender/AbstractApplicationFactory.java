package org.opennms.vaadin.extender;

import java.util.Collections;
import java.util.Map;

import com.vaadin.ui.UI;

public abstract class AbstractApplicationFactory implements ApplicationFactory {

    @Override
    public Map<String, String> getAdditionalHeaders() {
        return Collections.emptyMap();
    }

    @Override
    public abstract UI createUI();

    @Override
    public abstract Class<? extends UI> getUIClass();

}
