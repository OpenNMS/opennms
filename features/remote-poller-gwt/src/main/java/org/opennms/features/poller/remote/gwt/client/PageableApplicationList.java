package org.opennms.features.poller.remote.gwt.client;

import java.util.List;

import org.opennms.features.poller.remote.gwt.client.events.ApplicationSelectedEvent;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

public class PageableApplicationList extends PageableList {
    
    private List<ApplicationInfo> m_applications;
    
    private class ApplicationDetailView extends Widget{
        
        Image m_icon = new Image();
        Label m_nameLabel = new Label();
        Label m_statusLabel = new Label();
        
        @Override
        protected void doAttachChildren() {
            super.doAttachChildren();
            DOM.appendChild(this.getElement(), m_icon.getElement());
            DOM.appendChild(this.getElement(), m_nameLabel.getElement());
            DOM.appendChild(this.getElement(), m_statusLabel.getElement());
        }
        
        
        public ApplicationDetailView(ApplicationInfo applicationInfo) {
            setElement(Document.get().createDivElement());
            setStyles();
            
//            m_icon.setUrl(applicationInfo.get)
            m_nameLabel.setText(applicationInfo.getName());
            m_statusLabel.setText(applicationInfo.getStatus().getReason());
        }
        
        private void setStyles() {
            setStyleName(locationDetailStyle.detailContainerStyle());
            m_icon.addStyleName(locationDetailStyle.iconStyle());
            m_nameLabel.addStyleName(locationDetailStyle.nameStyle());
            m_statusLabel.addStyleName(locationDetailStyle.statusStyle());
            
        }
        
    }
    
    public void updateList(List<ApplicationInfo> applications) {
        setApplications(applications);
        super.showFirstPage();
    }
    
    @Override
    protected Widget getListItemWidget(int index) {
        return new ApplicationDetailView(getApplications().get(index));
    }


    private void setApplications(List<ApplicationInfo> applications) {
        m_applications = applications;
    }


    private List<ApplicationInfo> getApplications() {
        return m_applications;
    }


    @Override
    protected int getListSize() {
        return m_applications.size();
    }


    @Override
    public void onItemClickHandler(ClickEvent event) {
        Cell cell = getCellForEvent(event);
        
        ApplicationInfo appInfo = getApplications().get(cell.getRowIndex());
        fireEvent(new ApplicationSelectedEvent(appInfo));
    }

}
