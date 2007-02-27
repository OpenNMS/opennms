package org.opennms.dashboard.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;

public class DashletLoader extends Composite {
    
    public static final int COMPLETE = 0;
    public static final int LOADING = 1;
    public static final int ERROR = 2;

    SimplePanel m_panel = new SimplePanel();
    Image m_progressIcon = new Image(GWT.getModuleBaseURL()+"images/progress.gif");
    Image m_errorIcon = new Image(GWT.getModuleBaseURL()+"images/error.png");
    
    DashletLoader() {
        m_panel.addStyleName("dashletLoader");
        initWidget(m_panel);
    }

    public void setStatus(int status, String description) {
        switch( status ) {
        case ERROR:
            m_errorIcon.setTitle(description);
            m_panel.setWidget(m_errorIcon);
            break;
        case LOADING:
            m_progressIcon.setTitle(description);
            m_panel.setWidget(m_progressIcon);
            break;
        case COMPLETE:
            m_panel.remove(m_panel.getWidget());
            break;
        }
    }
    
    public void loading(String msg) {
        setStatus(LOADING, msg);
    }
    
    public void loading() {
        loading("Loading...");
    }
    
    public void loadError(Throwable caught) {
        setStatus(ERROR, "Error");
    }
    
    public void complete() {
        setStatus(COMPLETE, "");
    }
    
}
