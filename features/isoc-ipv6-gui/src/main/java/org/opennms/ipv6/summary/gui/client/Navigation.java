package org.opennms.ipv6.summary.gui.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

public class Navigation extends Composite implements HasHandlers {
    
    private static NavigationUiBinder uiBinder = GWT.create(NavigationUiBinder.class);
    
    private EventBus m_handlerManager;

    @UiField
    RadioButton m_allLocations;
    
    @UiField
    RadioButton m_singleLocation;
    
    @UiField
    ListBox m_locationList;
    
    @UiField
    RadioButton m_allHosts;
    
    @UiField
    RadioButton m_singleHost;
    
    @UiField
    ListBox m_hostList;
    
    interface NavigationUiBinder extends UiBinder<Widget, Navigation> {
    }

    public Navigation() {
        initWidget(uiBinder.createAndBindUi(this));
        m_handlerManager = new SimpleEventBus();
        m_allHosts.setValue(true);
        m_allLocations.setValue(true);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        m_handlerManager.fireEvent(event);
    }
    
    public HandlerRegistration addLocationUpdateEventHandler(LocationUpdateEventHandler handler) {
        return m_handlerManager.addHandler(LocationUpdateEvent.TYPE, handler);
    }
    
    public HandlerRegistration addHostUpdateEventHandler(HostUpdateEventHandler handler) {
        return m_handlerManager.addHandler(HostUpdateEvent.TYPE, handler);
    }
    
    @UiHandler("m_allLocations")
    public void allLocationsClick(ClickEvent event) {
        hideLocationSelection();
        fireEvent(new LocationUpdateEvent("All"));
    }
    
    @UiHandler("m_singleLocation")
    public void singleLocationClick(ClickEvent event) {
        showLocationSelection();
        fireEvent(new LocationUpdateEvent(m_locationList.getItemText(m_locationList.getSelectedIndex())));
    }
    
    
    @UiHandler("m_locationList")
    public void locationListClick(ClickEvent event) {
        fireEvent(new LocationUpdateEvent(m_locationList.getItemText(m_locationList.getSelectedIndex())));
    }
    
    @UiHandler("m_allHosts")
    public void allHostsClick(ClickEvent event) {
        hideHostSelection();
        fireEvent(new HostUpdateEvent("All"));
    }
    
    

    @UiHandler("m_singleHost")
    public void singleHostClick(ClickEvent event) {
        showHostSelection();
        fireEvent(new HostUpdateEvent(m_hostList.getItemText(m_hostList.getSelectedIndex())));
    }
    
    @UiHandler("m_hostList")
    public void hostListClick(ClickEvent event) {
        fireEvent(new HostUpdateEvent(m_hostList.getItemText(m_hostList.getSelectedIndex())));
    }
    
    public void loadLocations(List<String> locations) {
        m_locationList.clear();
        for(String location : locations) {
            m_locationList.addItem(location);
        }
    }
    
    public void loadHosts(List<String> hosts) {
        m_hostList.clear();
        for(String host : hosts) {
            m_hostList.addItem(host);
        }
    }
    
    private void showHostSelection() {
        m_hostList.setVisible(true);
    }

    private void hideLocationSelection() {
        m_locationList.setVisible(false);
    }
    
    private void showLocationSelection() {
        m_locationList.setVisible(true);
    }
    
    private void hideHostSelection() {
        m_hostList.setVisible(false);
    }

}
