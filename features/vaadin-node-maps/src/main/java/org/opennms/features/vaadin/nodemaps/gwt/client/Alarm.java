package org.opennms.features.vaadin.nodemaps.gwt.client;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.vaadin.terminal.gwt.client.UIDL;

public class Alarm extends JavaScriptObject {
    protected Alarm() {}

    public static final Alarm create(final UIDL uidl) {
        final Alarm alarm = Alarm.create();

        alarm.setNumberProperty("id", Integer.valueOf(uidl.getStringAttribute("id")));
        alarm.setNumberProperty("nodeId", Integer.valueOf(uidl.getStringAttribute("nodeId")));
        if (uidl.hasAttribute("ackTime")) {
            alarm.setNumberProperty("ackTime", Long.valueOf(uidl.getStringAttribute("ackTime")));
        }
        if (uidl.hasAttribute("ackUser")) {
            alarm.setStringProperty("ackUser", uidl.getStringAttribute("ackUser"));
        }
        alarm.setStringProperty("severityLabel", uidl.getStringAttribute("severityLabel"));
        alarm.setNumberProperty("severity", Integer.valueOf(uidl.getStringAttribute("severity")));
        alarm.setStringProperty("uei", uidl.getStringAttribute("uei"));
        alarm.setStringProperty("logMsg", uidl.getStringAttribute("logMsg"));
        if (uidl.hasAttribute("lastEventTime")) {
            alarm.setNumberProperty("lastEventTime", Long.valueOf(uidl.getStringAttribute("lastEventTime")));
        }

        return alarm;
    }

    protected final native Integer getIntegerProperty(final String key) /*-{
        return this[key];
    }-*/;

    protected final native Long getLongProperty(final String key) /*-{
        return this[key];
    }-*/;

    protected final native String getStringProperty(final String key) /*-{
        return this[key];
    }-*/;

    protected final native void setNumberProperty(final String key, final Number value) /*-{
        this[key] = value;
    }-*/;

    protected final native void setStringProperty(final String key, final String value) /*-{
        this[key] = value;
    }-*/;

    public static final Alarm create() {
        return JavaScriptObject.createObject().cast();
    }

    public final Integer getId() {
        return getIntegerProperty("id");
    }

    public final Integer getNodeId() {
        return getIntegerProperty("nodeId");
    }

    public final Date getAckTime() {
        final Long date = getLongProperty("ackTime");
        return date == null? null : new Date(date);
    }

    public final String getAckUser() {
        return getStringProperty("ackUser");
    }

    public final String getSeverityLabel() {
        return getStringProperty("severityLabel");
    }

    public final Integer getSeverity() {
        final Integer severity = getIntegerProperty("severity");
        return severity == null? 0 : severity;
    }

    public final String getUei() {
        return getStringProperty("uei");
    }

    public final String getLogMsg() {
        return getStringProperty("logMsg");
    }

    public final Date getLastEventTime() {
        final Long date = getLongProperty("lastEventTime");
        return date == null? null : new Date(date);
    }
}
