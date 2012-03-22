package org.opennms.features.gwt.ksc.add.client;

import java.util.List;

import org.opennms.features.gwt.ksc.add.client.presenter.KscAddGraphPresenter;
import org.opennms.features.gwt.ksc.add.client.presenter.Presenter;
import org.opennms.features.gwt.ksc.add.client.view.KscAddGraphView;
import org.opennms.features.gwt.ksc.add.client.view.KscAddGraphViewImpl;
import org.opennms.features.gwt.ksc.add.client.view.KscReport;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;

public class AppController implements Presenter {
    private List<KscReport> m_reports;
    private HasWidgets m_container;
    private PopupPanel m_popupPanel;
    private KscAddGraphView<KscReport> m_addGraphView;
    private String m_reportName;
    private String m_resourceId;

    public AppController(final List<KscReport> kscReports, final String reportName, final String resourceId) {
        m_reports = kscReports;
        m_reportName = reportName;
        m_resourceId = resourceId;
    }
    
    @Override
    public void go(final HasWidgets container) {
        m_container = container;

        if (m_addGraphView == null) {
            m_addGraphView = new KscAddGraphViewImpl();
        }

        if (m_popupPanel == null) {
            m_popupPanel = new PopupPanel();
            m_popupPanel.setWidth("300px");
            m_popupPanel.setHeight("54px");
            m_popupPanel.add(m_addGraphView);
            m_popupPanel.setAutoHideEnabled(false);
            m_popupPanel.setAnimationEnabled(false);
            m_popupPanel.setModal(false);
            m_popupPanel.setVisible(false);
            m_popupPanel.hide();
        }

        new KscAddGraphPresenter(m_popupPanel, m_addGraphView, m_reports, m_reportName, m_resourceId).go(m_container);
    }

}
