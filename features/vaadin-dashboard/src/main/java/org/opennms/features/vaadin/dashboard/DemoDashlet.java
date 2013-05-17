package org.opennms.features.vaadin.dashboard;

import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class DemoDashlet extends VerticalLayout{
    public DemoDashlet(String caption) {
        setCaption(caption);
        setMargin(true);
        setSpacing(true);

        addComponent(new Label("Welcome to the OpenNMS Dashboard proof-of-concept. You can rearrange the dashlets by dragging and dropping them."));
    }
}
