package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;

import org.opennms.features.poller.remote.gwt.client.events.ApplicationSelectedEvent;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

public class PageableApplicationList extends PageableList {
    
    private ArrayList<ApplicationInfo> m_applications;
    private HandlerManager m_eventBus;
    
    private class ApplicationDetailView extends Widget{
        
        final Image m_icon = new Image();
        final Label m_nameLabel = new Label();
        final Label m_statusLabel = new Label();
        
        @Override
        protected void doAttachChildren() {
            super.doAttachChildren();
            DOM.appendChild(this.getElement(), m_icon.getElement());
            DOM.appendChild(this.getElement(), m_nameLabel.getElement());
            DOM.appendChild(this.getElement(), m_statusLabel.getElement());
            
            int calculatedHeight = m_statusLabel.getOffsetHeight();
            int newHeight = calculatedHeight > 60 ? calculatedHeight : 60;
            setHeight(Integer.toString(newHeight));
        }
        
        public ApplicationDetailView(ApplicationInfo applicationInfo) {
            setElement(Document.get().createDivElement());
            setStyles();

            m_icon.setUrl(applicationInfo.getMarkerState().getImageURL());
            m_nameLabel.setText(applicationInfo.getName());
            m_statusLabel.setText(applicationInfo.getStatus().getReason());
        }
        
        private void setStyles() {
            setStyleName(locationDetailStyle.detailContainerStyle());
            String iconStyle = locationDetailStyle.iconStyle();
            m_icon.addStyleName(iconStyle);
            m_nameLabel.addStyleName(locationDetailStyle.nameStyle());
            m_statusLabel.addStyleName(locationDetailStyle.statusStyle());
            
        }
        
    }
    
    /**
     * TODO: Maybe enhance this so that it only adds/updates/deletes individual items
     * TODO: Don't skip to the front page on every update
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
    }
}
