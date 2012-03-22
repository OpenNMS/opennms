package org.opennms.features.gwt.ksc.add.client.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opennms.features.gwt.ksc.add.client.view.KscAddGraphView;
import org.opennms.features.gwt.ksc.add.client.view.KscReport;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.HasWidgets;

public class KscAddGraphPresenter implements Presenter, KscAddGraphView.Presenter<KscReport> {

    private static final List<KscReport> EMPTY_KSCREPORT_LIST = Collections.unmodifiableList(new ArrayList<KscReport>());
    private KscAddGraphView<KscReport> m_view;
    private List<KscReport> m_KscReports;
    private String m_reportName;
    private String m_resourceId;
    
    public KscAddGraphPresenter(final KscAddGraphView<KscReport> addGraphView, final List<KscReport> kscReports, final String reportName, final String resourceId) {
        m_view = addGraphView;
        m_view.setPresenter(this);
        m_KscReports = kscReports;
        m_reportName = reportName;
        m_resourceId = resourceId;
    }

    private List<KscReport> filterResultsByName(final String searchText) {
        final List<KscReport> list = new ArrayList<KscReport>();
        for (final KscReport detail : m_KscReports) {
            if (detail.getLabel().toLowerCase().contains(searchText.toLowerCase())) {
                list.add(detail);
            }
        }
        
        return list;
    }

    @Override
    public void onKeyCodeEvent(final KeyCodeEvent<?> event, final String searchText) {
        final int keyCode = event.getNativeEvent().getKeyCode();
        GWT.log("associated type = " + event.getAssociatedType());

        if (keyCode == KeyCodes.KEY_ESCAPE) {
            GWT.log("escape, hiding results");
            m_view.hidePopup();
        } else if (keyCode == KeyCodes.KEY_BACKSPACE && searchText.length() == 0) {
            m_view.hidePopup();
            m_view.setDataList(EMPTY_KSCREPORT_LIST);
            m_view.clearSelection();
        } else if (keyCode == KeyCodes.KEY_ENTER && m_view.getSelectedReport() != null && m_view.getTitle() != null && event instanceof KeyDownEvent && !m_view.isPopupShowing()) {
            onAddButtonClicked();
        } else {
            if (searchText.length() == 0) {
                GWT.log("search text is empty");
                m_view.setDataList(EMPTY_KSCREPORT_LIST);
            } else {
                GWT.log("search text is not empty");
                final List<KscReport> results = filterResultsByName(searchText);
                if (keyCode == KeyCodes.KEY_ENTER && results.size() == 1) {
                    m_view.hidePopup();
                    m_view.select(results.get(0));
                } else {
                    m_view.setDataList(results);
                    m_view.showPopup();
                    m_view.clearSelection();
                }
            }
        }
    }

    @Override
    public void onKscReportSelected() {
        GWT.log("selected report " + m_view.getSelectedReport().getId());
        /*
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseHref() + "KSC/customView.htm");
        urlBuilder.append("?type=custom");
        urlBuilder.append("&report=" + m_view.getSelectedReport().getId());
        Location.assign(urlBuilder.toString());
        */
    }

    @Override
    public void onAddButtonClicked() {
        GWT.log("adding resource '" + m_resourceId + "' from graph report '" + m_reportName + "' to KSC report '" + m_view.getSelectedReport().getLabel() + "' with title '" + m_view.getTitle() + "'");
    }

    @Override
    public void go(final HasWidgets container) {
        container.clear();
        container.add(m_view.asWidget());
    }
    
    public native final String getBaseHref() /*-{
        try{
            return $wnd.getBaseHref();
        }catch(err){
            return "";
        }
    }-*/;

}
