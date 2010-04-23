package org.opennms.features.poller.remote.gwt.client;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class FilterPanel extends Composite {

    interface Binder extends UiBinder<Widget, FilterPanel> { }
    private static final Binder BINDER = GWT.create(Binder.class);

    private transient HandlerManager m_eventBus;

    @UiField(provided = true)
    SuggestBox applicationNameSuggestBox;
    @UiField
    Panel applicationFilters;
    @UiField
    ToggleButton upButton;
    @UiField
    ToggleButton marginalButton;
    @UiField
    ToggleButton downButton;
    @UiField
    ToggleButton unknownButton;

    private final MultiWordSuggestOracle applicationNames = new MultiWordSuggestOracle();

    public interface FiltersChangedEventHandler extends EventHandler {
        public void onFiltersChanged(Filters filters);
    }

    public static class Filters {}

    public static class FiltersChangedEvent extends GwtEvent<FiltersChangedEventHandler>
    {
        public static Type<FiltersChangedEventHandler> TYPE = new Type<FiltersChangedEventHandler>();

        private final Filters m_filters;

        public FiltersChangedEvent(Filters filters) {
            m_filters = filters;
        }

        protected void dispatch(FiltersChangedEventHandler handler) {
            handler.onFiltersChanged(m_filters);
        }

        public GwtEvent.Type<FiltersChangedEventHandler> getAssociatedType() {
            return TYPE;
        }
    }

    public interface StatusSelectionChangedEventHandler extends EventHandler {
        public void onStatusSelectionChanged(Status status, boolean selected);
    }

    public static class StatusSelectionChangedEvent extends GwtEvent<StatusSelectionChangedEventHandler>
    {
        public static Type<StatusSelectionChangedEventHandler> TYPE = new Type<StatusSelectionChangedEventHandler>();

        private final Status m_status;
        private final boolean m_selected;

        public StatusSelectionChangedEvent(Status status, boolean selected) {
            m_status = status;
            m_selected = selected;
        }

        protected void dispatch(StatusSelectionChangedEventHandler handler) {
            handler.onStatusSelectionChanged(m_status, m_selected);
        }

        public GwtEvent.Type<StatusSelectionChangedEventHandler> getAssociatedType() {
            return TYPE;
        }
    }

    public FilterPanel() {
        super();
        applicationNameSuggestBox = new SuggestBox(applicationNames);
        initWidget(BINDER.createAndBindUi(this));
    }

    @UiHandler("applicationNameSuggestBox") 
    public void onApplicationSelect(final SelectionEvent<Suggestion> event) {
        Suggestion item = event.getSelectedItem();
        Filters filters = new Filters();

        // TODO: Create a new filters object

        m_eventBus.fireEvent(new FiltersChangedEvent(filters));
    }

    @UiHandler("upButton") 
    public void onUpButtonClick(ClickEvent event) {
        m_eventBus.fireEvent(new StatusSelectionChangedEvent(Status.UP, upButton.isDown()));
    }

    @UiHandler("marginalButton") 
    public void onMarginalButtonClick(ClickEvent event) {
        m_eventBus.fireEvent(new StatusSelectionChangedEvent(Status.MARGINAL, marginalButton.isDown()));
    }

    @UiHandler("downButton") 
    public void onDownButtonClick(ClickEvent event) {
        m_eventBus.fireEvent(new StatusSelectionChangedEvent(Status.DOWN, downButton.isDown()));
    }

    @UiHandler("unknownButton") 
    public void onUnknownButtonClick(ClickEvent event) {
        m_eventBus.fireEvent(new StatusSelectionChangedEvent(Status.UNKNOWN, unknownButton.isDown()));
    }

    public void updateApplicationNames(Collection<String> names) {
        applicationNames.clear();
        applicationNames.addAll(names);
    }

    public void showApplicationFilters(boolean showMe) {
        applicationFilters.setVisible(showMe);
    }

    public void setEventBus(final HandlerManager eventBus) {
        m_eventBus = eventBus;
    }
}
