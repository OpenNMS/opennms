package org.opennms.features.gwt.ksc.combobox.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.ksc.combobox.client.view.KscComboboxView;
import org.opennms.features.gwt.ksc.combobox.client.view.KscComboboxViewImpl;
import org.opennms.features.gwt.ksc.combobox.client.view.KscReportDetail;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;

public class KscComboboxPresenter implements Presenter, KscComboboxView.Presenter<KscReportDetail> {

    private KscComboboxView<KscReportDetail> m_view;
    private List<KscReportDetail> m_kscReportDetails;

    
    public KscComboboxPresenter(KscComboboxViewImpl view, JsArray<KscReportDetail> kscReportDetails) {
        m_view = view;
        m_view.setPresenter(this);
        m_kscReportDetails = convertJsArrayToList(kscReportDetails);
    }

    @Override
    public void onSearchButtonClicked() {
        m_view.setDataList(filterResultsByName(m_view.getSearchText()));
    }

    private List<KscReportDetail> filterResultsByName(String searchText) {
        List<KscReportDetail> list = new ArrayList<KscReportDetail>();
        for(KscReportDetail detail : m_kscReportDetails) {
            if(detail.getLabel().contains(searchText)) {
                list.add(detail);
            }
        }
        
        return list;
    }

    @Override
    public void onEnterKeyEvent() {
        m_view.setDataList(filterResultsByName(m_view.getSearchText()));
    }

    @Override
    public void onKscReportSelected() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseHref() + "KSC/customView.htm");
        urlBuilder.append("?type=custom");
        urlBuilder.append("&report=" + m_view.getSelectedReport().getId());
        Location.assign(urlBuilder.toString());
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(m_view.asWidget());
    }
    
    private List<KscReportDetail> convertJsArrayToList(JsArray<KscReportDetail> kscReportDetails) {
        List<KscReportDetail> m_list = new ArrayList<KscReportDetail>();
        
        for(int i = 0; i < kscReportDetails.length(); i++) {
            m_list.add(kscReportDetails.get(i));
        }
        return m_list;
    }
    
    public native final String getBaseHref() /*-{
        try{
            return $wnd.getBaseHref();
        }catch(err){
            return "";
        }
    }-*/;

}
