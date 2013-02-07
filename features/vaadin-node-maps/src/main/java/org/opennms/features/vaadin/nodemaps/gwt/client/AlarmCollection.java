package org.opennms.features.vaadin.nodemaps.gwt.client;

import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class AlarmCollection extends JavaScriptObject {
    protected AlarmCollection() {}

    public static native final AlarmCollection create() /*-{
        var alarmCollection = {};
        alarmCollection.getHighestSeverityAlarm = function(nodeId) {
            if (alarmCollection.hasOwnProperty(nodeId)) {
                return alarmCollection[nodeId][0];
            } else {
                return null;
            }
        };
        alarmCollection.getUnackedCount = function(nodeId) {
            var count = 0;
            if (alarmCollection.hasOwnProperty(nodeId)) {
                var alarm;
                for (var i = 0; i < alarmCollection[nodeId].length; i++) {
                    alarm = alarmCollection[nodeId][i];
                    if (!alarm.ackUser) {
                        count++;
                    }
                }
            }
            return count;
        };
        return alarmCollection;
    }-*/;

    public static final AlarmCollection create(final Map<Integer, List<Alarm>> alarms) {
        final AlarmCollection collection = AlarmCollection.create();

        for (final Map.Entry<Integer,List<Alarm>> entry : alarms.entrySet()) {
            for (final Alarm alarm : entry.getValue()) {
                collection.addAlarm(entry.getKey(), alarm);
            }
        }

        return collection;
    }

    protected final native void addAlarm(final int nodeId, final Alarm alarm) /*-{
        if (!this.hasOwnProperty(nodeId)) {
            this[nodeId] = [];
        }
        this[nodeId].push(alarm);
    }-*/;

    public final native JsArray<Alarm> getAlarms(final int nodeId) /*-{
        if (this.hasOwnProperty(nodeId)) {
            return this[nodeId];
        } else {
            return null;
        }
    }-*/;

    public final native Alarm getHighestSeverityAlarm(final int nodeId) /*-{
        return this.getHighestSeverityAlarm(nodeId);
    }-*/;

    public final native int getUnackedCount(final int nodeId) /*-{
        return this.getUnackedCount(nodeId);
    }-*/;
}
