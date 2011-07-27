package org.opennms.features.gwt.snmpselect.list.client;

import org.opennms.features.gwt.snmpselect.list.client.presenter.Presenter;
import org.opennms.features.gwt.snmpselect.list.client.presenter.SnmpSelectListPresenter;
import org.opennms.features.gwt.snmpselect.list.client.rest.SnmpInterfaceRestService;
import org.opennms.features.gwt.snmpselect.list.client.view.SnmpSelectListViewImpl;

import com.google.gwt.user.client.ui.HasWidgets;

public class AppController implements Presenter{

    private SnmpSelectListPresenter m_presenter;
    
    public AppController(SnmpInterfaceRestService service) {
        m_presenter = new SnmpSelectListPresenter(new SnmpSelectListViewImpl(), service);
    }

    @Override
    public void go(HasWidgets widget) {
        m_presenter.go(widget);
    }

}
