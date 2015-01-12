package org.opennms.features.vaadin.surveillanceviews.ui;

import org.opennms.osgi.OnmsVaadinUIFactory;
import org.osgi.service.blueprint.container.BlueprintContainer;

public class SurveillanceViewsUIFactory extends OnmsVaadinUIFactory {
    public SurveillanceViewsUIFactory(BlueprintContainer container, String beanName) {
        super(SurveillanceViewsUI.class, container, beanName);
    }

}
