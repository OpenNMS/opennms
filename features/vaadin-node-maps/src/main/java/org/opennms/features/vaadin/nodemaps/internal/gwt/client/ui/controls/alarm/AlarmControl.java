package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.alarm;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.discotools.gwt.leaflet.client.controls.Control;
import org.discotools.gwt.leaflet.client.jsobject.JSObject;
import org.discotools.gwt.leaflet.client.map.Map;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.AlarmSeverityUpdatedEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.AlarmSeverityUpdatedEventHandler;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.DomEvent;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.event.SearchEventCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class AlarmControl extends Control {
    Logger logger = Logger.getLogger(getClass().getName());

    private ListBox m_severityBox;

    private SearchEventCallback m_onChange;

    private AlarmSeverityUpdatedEventHandler m_alarmSeverityUpdated;

    public AlarmControl() {
        super(JSObject.createJSObject());
        setJSObject(AlarmControlImpl.create(this, new AlarmControlOptions().getJSObject()));
    }

    public Element doOnAdd(final JavaScriptObject map) {
        logger.log(Level.INFO, "doOnAdd() called");
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

        m_alarmSeverityUpdated = new AlarmSeverityUpdatedEventHandler() {
            @Override public void onEvent(final NativeEvent nativeEvent) {
                final AlarmSeverityUpdatedEvent event = nativeEvent.cast();
                m_severityBox.setItemSelected(event.getMinimumSeverity(), true);
            }
        };

        m_onChange = new SearchEventCallback("change", m_severityBox) {
            @Override
            public void onEvent(final NativeEvent event) {
                final Widget widget = getWidget();
                final ListBox severityBox = (ListBox)widget;
                final int selected = severityBox.getSelectedIndex();
                logger.log(Level.INFO, "new selection index = " + selected);
                final String value = severityBox.getValue(selected);
                logger.log(Level.INFO, "new severity = " + value);
                final int intValue = value == null? 0 : Integer.valueOf(value);
                DomEvent.send(AlarmSeverityUpdatedEvent.createEvent(intValue));
            }
        };
        DomEvent.addListener(m_onChange);

        m_severityBox.addStyleName(css.label());
        element.appendChild(m_severityBox.getElement());

        logger.log(Level.INFO, "doOnAdd() finished, returning: " + element);
        return element;
    }

    public void doOnRemove(final JavaScriptObject map) {
        logger.log(Level.INFO, "doOnRemove() called");
        if (m_onChange != null) DomEvent.removeListener(m_onChange);
        if (m_alarmSeverityUpdated != null) DomEvent.removeListener(m_alarmSeverityUpdated);
        DomEvent.send(AlarmSeverityUpdatedEvent.createEvent(0));
    }

    @Override
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
