package org.opennms.features.vaadin.surveillanceviews.ui;

import org.opennms.osgi.OnmsVaadinUIFactory;
import org.osgi.service.blueprint.container.BlueprintContainer;

public class SurveillanceViewsConfigUIFactory extends OnmsVaadinUIFactory {
    public SurveillanceViewsConfigUIFactory(BlueprintContainer container, String beanName) {
        super(SurveillanceViewsConfigUI.class, container, beanName);
    }

}
