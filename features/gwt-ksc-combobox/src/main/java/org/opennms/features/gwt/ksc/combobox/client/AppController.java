package org.opennms.features.gwt.ksc.combobox.client;

import org.opennms.features.gwt.ksc.combobox.client.presenter.KscComboboxPresenter;
import org.opennms.features.gwt.ksc.combobox.client.presenter.Presenter;
import org.opennms.features.gwt.ksc.combobox.client.view.KscComboboxViewImpl;
import org.opennms.features.gwt.ksc.combobox.client.view.KscReportDetail;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HasWidgets;

public class AppController implements Presenter {
    
    
    private JsArray<KscReportDetail> m_kscReportDetails;

    public AppController(JsArray<KscReportDetail> kscReportDetails) {
        m_kscReportDetails = kscReportDetails;
    }
    
    @Override
    public void go(HasWidgets container) {
        new KscComboboxPresenter(new KscComboboxViewImpl(), m_kscReportDetails).go(container);
    }

}
