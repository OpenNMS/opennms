package org.opennms.features.gwt.combobox.client;

import org.opennms.features.gwt.combobox.client.presenter.Presenter;
import org.opennms.features.gwt.combobox.client.presenter.SuggestionComboboxPresenter;
import org.opennms.features.gwt.combobox.client.rest.DefaultNodeService;
import org.opennms.features.gwt.combobox.client.view.NodeDetail;
import org.opennms.features.gwt.combobox.client.view.SuggestionComboboxView;
import org.opennms.features.gwt.combobox.client.view.SuggestionComboboxViewImpl;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.HasWidgets;

public class AppController implements Presenter {
    private final SimpleEventBus m_eventBus;
    private SuggestionComboboxView<NodeDetail> m_suggestionComboboxView;
    private HasWidgets m_container;
    
    public AppController(SimpleEventBus eventBus) {
        m_eventBus = eventBus;
    }

    @Override
    public void go(HasWidgets container) {
        m_container = container;

        if(m_suggestionComboboxView == null) {
            m_suggestionComboboxView = new SuggestionComboboxViewImpl();
        }
        
        new SuggestionComboboxPresenter(m_eventBus, m_suggestionComboboxView, new DefaultNodeService()).go(m_container);
    }

}
