package org.opennms.features.gwt.ksc.add.client;

import java.util.List;

import org.opennms.features.gwt.ksc.add.client.presenter.KscAddGraphPresenter;
import org.opennms.features.gwt.ksc.add.client.presenter.Presenter;
import org.opennms.features.gwt.ksc.add.client.view.KscAddGraphView;
import org.opennms.features.gwt.ksc.add.client.view.KscAddGraphViewImpl;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;

public class AppController implements Presenter {
    private List<KscReport> m_reports;
    private HasWidgets m_container;
    private PopupPanel m_popupPanel;
    private KscAddGraphView<KscReport> m_addGraphView;
    private GraphInfo m_graphInfo;

    public AppController(final List<KscReport> kscReports, final GraphInfo graphInfo) {
        m_reports = kscReports;
        m_graphInfo = graphInfo;
    }
    
    @Override
    public void go(final HasWidgets container) {
        m_container = container;

        if (m_addGraphView == null) {
            m_addGraphView = new KscAddGraphViewImpl();
            m_addGraphView.setTitle(m_graphInfo.getTitle() == null? "" : m_graphInfo.getTitle());
        }

        if (m_popupPanel == null) {
            m_popupPanel = new PopupPanel();
            m_popupPanel.setWidth("300px");
            m_popupPanel.setHeight("54px");
            m_popupPanel.add(m_addGraphView);
            m_popupPanel.setAutoHideEnabled(true);
            m_popupPanel.setAnimationEnabled(false);
            m_popupPanel.setModal(false);
            m_popupPanel.setVisible(false);
            m_popupPanel.hide();
        }

        new KscAddGraphPresenter(m_popupPanel, m_addGraphView, m_reports, m_graphInfo).go(m_container);
    }

}
