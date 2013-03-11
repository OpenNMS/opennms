package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm;

import org.discotools.gwt.leaflet.client.controls.Control;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.map.Map;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.SearchConsumer;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchEventCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.VConsole;

public class AlarmControl extends Control {
    private ListBox m_severityBox;
    private final SearchConsumer m_searchConsumer;

    private SearchEventCallback m_onChange;

    public AlarmControl(final SearchConsumer searchConsumer) {
        this(searchConsumer, new AlarmControlOptions());
    }

    public AlarmControl(final SearchConsumer searchConsumer, final AlarmControlOptions options) {
        super(JSObject.createJSObject());
        setJSObject(AlarmControlImpl.create(this, options.getJSObject()));
        VConsole.log("new AlarmControl()");
        m_searchConsumer = searchConsumer;
    }

    public Element doOnAdd(final JavaScriptObject map) {
        VConsole.log("doOnAdd() called");
        final AlarmControlCss css = AlarmControlBundle.INSTANCE.css();
        css.ensureInjected();

        final Element element = AlarmControlImpl.createElement("leaflet-control-alarm");
        element.addClassName("leaflet-control");

        final Label label = new Label("Show Severity >=");
        label.getElement().setAttribute("for", "alarmControl");
        label.addStyleName(css.label());
        element.appendChild(label.getElement());

        m_severityBox = new ListBox(false);
        m_severityBox.getElement().setId("alarmControl");
        m_severityBox.addItem("Normal", "0");
        m_severityBox.addItem("Warning", "4");
        m_severityBox.addItem("Minor", "5");
        m_severityBox.addItem("Major", "6");
        m_severityBox.addItem("Critical", "7");

        DomEvent.stopEventPropagation(m_severityBox);

        m_onChange = new SearchEventCallback("change", m_severityBox, m_searchConsumer) {
            @Override
            protected void onEvent(final NativeEvent event) {
                final Widget widget = getWidget();
                final SearchConsumer searchConsumer = getSearchConsumer();
                final ListBox severityBox = (ListBox)widget;
                final int selected = severityBox.getSelectedIndex();
                VConsole.log("new selection index = " + selected);
                final String value = severityBox.getValue(selected);
                VConsole.log("new severity = " + value);
                if (value != null && searchConsumer != null) {
                    final int severity = Integer.valueOf(value).intValue();
                    searchConsumer.setMinimumSeverity(severity);
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override public void execute() {
                            searchConsumer.refresh();
                            VConsole.log("successfully set new severity to " + severity);
                        }
                    });
                }
            }
        };
        DomEvent.addListener(m_onChange);

        m_severityBox.addStyleName(css.label());
        element.appendChild(m_severityBox.getElement());

        VConsole.log("doOnAdd() finished, returning: " + element);
        return element;
    }

    public void doOnRemove(final JavaScriptObject map) {
        VConsole.log("doOnRemove() called");
        DomEvent.removeListener(m_onChange);
        if (m_searchConsumer != null) m_searchConsumer.clearSearch();
    }

    public AlarmControl addTo(final Map map) {
        return (AlarmControl)super.addTo(map);
    }
    
    @Override
    public AlarmControl setPosition(final String position) {
        return (AlarmControl)super.setPosition(position);
    }
    
    @Override
    public AlarmControl removeFrom(final Map map) {
        return (AlarmControl)super.removeFrom(map);
    }
}
