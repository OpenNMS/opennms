package org.opennms.netmgt.snmp.proxy.common.utils;

public class LocationUtils {

    public static String LOCATION_OVERRIDE_KEY = "org.opennms.snmp.proxy.location.override";

    public static boolean isLocationOverrideEnabled() {
        return getLocationOverride() != null;
    }

    public static String getLocationOverride() {
        return System.getProperty(LOCATION_OVERRIDE_KEY);
    }
}
