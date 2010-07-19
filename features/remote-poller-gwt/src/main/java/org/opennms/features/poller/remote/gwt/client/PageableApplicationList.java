package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.events.ApplicationDetailsRetrievedEvent;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationDetailsRetrievedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationSelectedEvent;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * <p>PageableApplicationList class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class PageableApplicationList extends PageableList implements ApplicationDetailsRetrievedEventHandler {

    private ArrayList<ApplicationInfo> m_applications;
    private HandlerManager m_eventBus;
    private Set<ApplicationInfo> m_selected = null;
    private Map<String, ApplicationDetails> m_selectedAppDetails = new HashMap<String, ApplicationDetails>();

    interface ApplicationDetailStyle extends LocationDetailStyle {
        String detailContainerStyle();
        String iconStyle();
        String nameStyle();
        String areaStyle();
        String statusStyle();
        String alternateRowStyle();
    }

    private class ApplicationDetailView extends Widget {
        final Image m_icon = new Image();
        final Label m_nameLabel = new Label();
        final HTML m_statusLabel = new HTML();

        @Override
        protected void doAttachChildren() {
            super.doAttachChildren();
            DOM.appendChild(this.getElement(), m_icon.getElement());
            DOM.appendChild(this.getElement(), m_nameLabel.getElement());
            DOM.appendChild(this.getElement(), m_statusLabel.getElement());
        }

        @Override
        protected void onLoad() {
            super.onLoad();
            resizeToFit();
        }

        private void resizeToFit() {
            final int calculatedHeight = m_nameLabel.getOffsetHeight() + m_statusLabel.getOffsetHeight();
            final int newHeight = calculatedHeight > 60 ? calculatedHeight : 60;
            setHeight(Integer.toString(newHeight + 2));
        }

        public ApplicationDetailView(final ApplicationInfo applicationInfo) {
            setElement(Document.get().createDivElement());
            setStyles();

            m_icon.setUrl(applicationInfo.getMarkerState().getImageURL());
            m_nameLabel.setText(applicationInfo.getName());
            m_statusLabel.setHTML(getApplicationStatusHTML(applicationInfo));
        }

        private String getApplicationStatusHTML(final ApplicationInfo applicationInfo) {
            if (m_selected != null && checkIfApplicationIsSelected(applicationInfo.getName())) {
                return getSelectedApplicationDetailsAsString(applicationInfo.getName());
            } else {
                return applicationInfo.getStatusDetails().getReason();
            }
        }

        private void setStyles() {
            setStyleName(locationDetailStyle.detailContainerStyle());
            final String iconStyle = locationDetailStyle.iconStyle();
            m_icon.addStyleName(iconStyle);
            m_nameLabel.addStyleName(locationDetailStyle.nameStyle());
            m_statusLabel.addStyleName(locationDetailStyle.statusStyle());
        }
    }

    
    /**
     * <p>Constructor for PageableApplicationList.</p>
     */
    public PageableApplicationList() {
        super();
    }
    
    /**
     * <p>getSelectedApplicationDetailsAsString</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getSelectedApplicationDetailsAsString(String name) {
        ApplicationDetails appDetails = m_selectedAppDetails.get(name);
        return appDetails.getDetailsAsString();
    }
    
    /**
     * <p>checkIfApplicationIsSelected</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean checkIfApplicationIsSelected(String name) {
        return findSelectedApplication(name) != null ? true : false;
    }
    
    private ApplicationInfo findSelectedApplication(String name) {
        for(ApplicationInfo appInfo : m_selected) {
            if(appInfo.getName().equals(name)) {
                return appInfo;
            }
        }
        return null;
    }
    /**
     * TODO: Maybe enhance this so that it only adds/updates/deletes individual
     * items TODO: Don't skip to the front page on every update
     *
     * @param applications a {@link java.util.ArrayList} object.
     */
    public void updateList(final ArrayList<ApplicationInfo> applications) {
        setApplications(applications);
        refresh();
    }

    /** {@inheritDoc} */
    @Override
    protected Widget getListItemWidget(final int index) {
        return new ApplicationDetailView(getApplications().get(index));
    }

    private void setApplications(final ArrayList<ApplicationInfo> applications) {
        m_applications = applications;
    }

    private ArrayList<ApplicationInfo> getApplications() {
        return m_applications;
    }

    /** {@inheritDoc} */
    @Override
    protected int getListSize() {
        if (m_applications == null) return 0;
        return m_applications.size();
    }

    /** {@inheritDoc} */
    @Override
    public void onItemClickHandler(final ClickEvent event) {
        final Cell cell = getCellForEvent(event);

        final ApplicationInfo appInfo = getApplications().get(cell.getRowIndex() + (getCurrentPageIndex() * getTotalListItemsPerPage()));
        m_eventBus.fireEvent(new ApplicationSelectedEvent(appInfo.getName()));
    }

    /**
     * <p>setEventBus</p>
     *
     * @param eventBus a {@link com.google.gwt.event.shared.HandlerManager} object.
     */
    public void setEventBus(final HandlerManager eventBus) {
        m_eventBus = eventBus;
        registerHandlers();
    }

    private void registerHandlers() {
        m_eventBus.addHandler(ApplicationDetailsRetrievedEvent.TYPE, this);

        addHandler(new ResizeHandler() {
            public void onResize(final ResizeEvent event) {
                refreshApplicationListResize();
            }
        },  ResizeEvent.getType());
    }

    /** {@inheritDoc} */
    public void onApplicationDetailsRetrieved(final ApplicationDetailsRetrievedEvent event) {
        if(checkIfApplicationIsSelected(event.getApplicationDetails().getApplicationName())) {
            m_selectedAppDetails.put(event.getApplicationDetails().getApplicationName(), event.getApplicationDetails());
        }else {
            m_selectedAppDetails.remove(event.getApplicationDetails().getApplicationName());
        }
        refreshApplicationListResize();
    }

    /**
     * <p>refreshApplicationListResize</p>
     */
    public void refreshApplicationListResize() {
        for(int i = 0; i < getDataList().getRowCount(); i++) {
            ApplicationDetailView view = (ApplicationDetailView) getDataList().getWidget(i, 0);
            view.resizeToFit();
        }
        refresh();
    }

    /**
     * <p>updateSelectedApplications</p>
     *
     * @param selectedApplications a {@link java.util.Set} object.
     */
    public void updateSelectedApplications(Set<ApplicationInfo> selectedApplications) {
        m_selected = selectedApplications;
        refresh();
    }
}
