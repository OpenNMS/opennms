package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;

import org.opennms.features.poller.remote.gwt.client.events.ApplicationDetailsRetrievedEvent;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationDetailsRetrievedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.ApplicationSelectedEvent;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

public class PageableApplicationList extends PageableList implements
        ApplicationDetailsRetrievedEventHandler {

    private ArrayList<ApplicationInfo> m_applications;
    private HandlerManager m_eventBus;

    private class ApplicationDetailView extends Widget {
        
        private String m_name = "";
        final Image m_icon = new Image();
        final Label m_nameLabel = new Label();
        final Label m_statusLabel = new Label();

        @Override
        protected void doAttachChildren() {
            super.doAttachChildren();
            DOM.appendChild(this.getElement(), m_icon.getElement());
            DOM.appendChild(this.getElement(), m_nameLabel.getElement());
            DOM.appendChild(this.getElement(), m_statusLabel.getElement());

        }

        @Override
        protected void onLoad() {
            resizeToFit();
        }

        private void resizeToFit() {
            int calculatedHeight = m_nameLabel.getOffsetHeight() + m_statusLabel.getOffsetHeight();
            int newHeight = calculatedHeight > 60 ? calculatedHeight : 60;
            setHeight(Integer.toString(newHeight + 2));
        }

        public ApplicationDetailView(ApplicationInfo applicationInfo) {
            setElement(Document.get().createDivElement());
            setStyles();
            
            m_name = applicationInfo.getName();
            m_icon.setUrl(applicationInfo.getMarkerState().getImageURL());
            m_nameLabel.setText(applicationInfo.getName());
            m_statusLabel.setText(applicationInfo.getStatusDetails().getReason());
        }

        private void setStyles() {
            setStyleName(locationDetailStyle.detailContainerStyle());
            String iconStyle = locationDetailStyle.iconStyle();
            m_icon.addStyleName(iconStyle);
            m_nameLabel.addStyleName(locationDetailStyle.nameStyle());
            m_statusLabel.addStyleName(locationDetailStyle.statusStyle());

        }

        public String getName() {
            return m_name;
        }

        public void updateDetails(ApplicationDetails appDetails) {
            m_statusLabel.setText(appDetails.getStatusDetails().getReason());
            resizeToFit();
        }

    }

    /**
     * TODO: Maybe enhance this so that it only adds/updates/deletes individual
     * items TODO: Don't skip to the front page on every update
     */
    public void updateList(final ArrayList<ApplicationInfo> applications) {
        setApplications(applications);
        super.showFirstPage();
    }

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

    @Override
    protected int getListSize() {
        return m_applications.size();
    }

    @Override
    public void onItemClickHandler(final ClickEvent event) {
        final Cell cell = getCellForEvent(event);

        final ApplicationInfo appInfo = getApplications().get(cell.getRowIndex());
        m_eventBus.fireEvent(new ApplicationSelectedEvent(appInfo));
    }

    public void setEventBus(final HandlerManager eventBus) {
        m_eventBus = eventBus;
        registerHandlers();
    }

    private void registerHandlers() {
        m_eventBus.addHandler(ApplicationDetailsRetrievedEvent.TYPE, this);
        
        addHandler(new ResizeHandler() {
            
            public void onResize(ResizeEvent arg0) {
                Window.alert("Resizing this stuff");
                
            }
        },  ResizeEvent.getType());
    }

    public void onApplicationDetailsRetrieved(ApplicationDetailsRetrievedEvent event) {

        updateApplicationDisplayItem(event.getApplicationDetails());

    }

    private void updateApplicationDisplayItem(ApplicationDetails appDetails) {
        
        for(int i = 0; i < getDataList().getRowCount(); i++) {
            ApplicationDetailView view = (ApplicationDetailView) getDataList().getWidget(i, 0);
            if(view.getName().equals(appDetails.getApplicationName())) {
                view.updateDetails(appDetails);
            }
        }
        
    }

    public void refreshApplicationListResize() {
        for(int i = 0; i < getDataList().getRowCount(); i++) {
            ApplicationDetailView view = (ApplicationDetailView) getDataList().getWidget(i, 0);
            view.resizeToFit();
        }
    }
    
}
