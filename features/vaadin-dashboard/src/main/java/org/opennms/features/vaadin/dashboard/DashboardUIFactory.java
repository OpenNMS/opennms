package org.opennms.features.vaadin.dashboard;

import com.vaadin.ui.UI;
import org.ops4j.pax.vaadin.AbstractApplicationFactory;
import org.osgi.service.blueprint.container.BlueprintContainer;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class DashboardUIFactory  extends AbstractApplicationFactory {
    private final BlueprintContainer m_blueprintContainer;
    private final String m_beanName;

    public DashboardUIFactory(BlueprintContainer container, String beanName) {
        m_blueprintContainer = container;
        m_beanName = beanName;
    }

    @Override
    public UI getUI() {
        return (UI) m_blueprintContainer.getComponentInstance(m_beanName);
    }

    @Override
    public Class<? extends UI> getUIClass() {
        return DashboardUI.class;
    }
}
