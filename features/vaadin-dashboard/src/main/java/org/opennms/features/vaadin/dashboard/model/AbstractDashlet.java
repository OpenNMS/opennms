package org.opennms.features.vaadin.dashboard.model;

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class AbstractDashlet implements Dashlet {
    protected String m_name;
    /**
     * The {@link DashletSpec} to be used
     */
    protected DashletSpec m_dashletSpec;

    @Override
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    @Override
    public DashletSpec getDashletSpec() {
        return m_dashletSpec;
    }

    public void setDashletSpec(DashletSpec dashletSpec) {
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
