package org.opennms.features.vaadin.dashboard.dashlets;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

/**
 * This class implements a {@link Dashlet} for testing purposes and displays a static map image.
 *
 * @author Christian Pape
 */
public class TopologyDashlet extends VerticalLayout implements Dashlet {
    /**
     * the dashlet's name
     */
    private String m_name;
    /**
     * The {@link DashletSpec} for this instance
     */
    private DashletSpec m_dashletSpec;

    /**
     * Constructor for instantiating new objects.
     *
     * @param dashletSpec the {@link DashletSpec} to be used
     */
    public TopologyDashlet(String name, DashletSpec dashletSpec) {
        /**
         * Setting the member fields
         */
        m_name = name;
        m_dashletSpec = dashletSpec;

        /**
         * Setting up the layout
         */
        setCaption(getName());
        setSizeFull();

        String searchString = "";

        if (m_dashletSpec.getParameters().containsKey("search")) {
            searchString = m_dashletSpec.getParameters().get("search");
        }

        /**
         * creating browser frame to display node-maps
         */
        BrowserFrame browserFrame = new BrowserFrame(null, new ExternalResource("/opennms/topology?" + searchString));
        browserFrame.setSizeFull();
        addComponent(browserFrame);
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public boolean isBoosted() {
        return false;
    }

    /**
     * Updates the dashlet contents and computes new boosted state
     */
    @Override
    public void update() {
        /**
         * do nothing
         */
    }
}
