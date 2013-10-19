package org.opennms.features.vaadin.dashboard.model;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class AbstractDashlet implements Dashlet {
    private String m_name;
    /**
     * The {@link DashletSpec} to be used
     */
    private DashletSpec m_dashletSpec;

    public AbstractDashlet(String name, DashletSpec dashletSpec) {
        m_name = name;
        m_dashletSpec = dashletSpec;
    }

    @Override
    final public String getName() {
        return m_name;
    }

    final public void setName(String name) {
        m_name = name;
    }

    @Override
    final public DashletSpec getDashletSpec() {
        return m_dashletSpec;
    }

    final public void setDashletSpec(DashletSpec dashletSpec) {
        m_dashletSpec = dashletSpec;
    }

    @Override
    public void updateWallboard() {
    }

    @Override
    public void updateDashboard() {
    }

    @Override
    public boolean isBoosted() {
        return false;
    }

    @Override
    public Component getWallboardComponent() {
        return new Label(m_name + " wallboard view");
    }

    @Override
    public Component getDashboardComponent() {
        return new Label(m_name + " dashboard view");
    }
}
