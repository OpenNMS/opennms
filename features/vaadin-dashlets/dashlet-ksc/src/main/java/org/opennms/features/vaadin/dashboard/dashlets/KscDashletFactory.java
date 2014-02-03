package org.opennms.features.vaadin.dashboard.dashlets;

import org.opennms.features.vaadin.dashboard.model.AbstractDashletFactory;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Created with IntelliJ IDEA.
 * User: chris
 * Date: 09.10.13
 * Time: 22:29
 * To change this template use File | Settings | File Templates.
 */
public class KscDashletFactory extends AbstractDashletFactory {
    private NodeDao m_nodeDao;
    private ResourceDao m_resourceDao;
    private TransactionOperations m_transactionOperations;

    /**
     * Method for creating a new {@link org.opennms.features.vaadin.dashboard.model.Dashlet} instance.
     *
     * @param dashletSpec the {@link org.opennms.features.vaadin.dashboard.model.DashletSpec} to use
     * @return a new {@link org.opennms.features.vaadin.dashboard.model.Dashlet} instance
     */
    public Dashlet newDashletInstance(DashletSpec dashletSpec) {
        return new KscDashlet(getName(), dashletSpec, m_nodeDao, m_resourceDao, m_transactionOperations);
    }

    /**
     * Returns the help content {@link String}
     *
     * @return the help content
     */
    @Override
    public String getHelpContentHTML() {
        return "This Dashlet provides a view to the OpenNMS Rrd graphs. It is configurable via a custom configuration dialog.";
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.m_nodeDao = nodeDao;
    }

    public void setResourceDao(ResourceDao resourceDao) {
        this.m_resourceDao = resourceDao;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        this.m_transactionOperations = transactionOperations;
    }

    /**
     * Returns a custom configuration window.
     *
     * @param dashletSpec the {@link DashletSpec} to use
     * @return the configuration window
     */
    @Override
    public DashletConfigurationWindow configurationWindow(DashletSpec dashletSpec) {
        return new KscDashletConfigurationWindow(dashletSpec);
    }
}
