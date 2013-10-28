package org.opennms.features.vaadin.nodemaps.internal.gwt.shared;

public abstract class Util {

    public static boolean hasChanged(final String a, final String b) {
        if (a == null && b == null) return false;
        if (a == null && b != null) return true;
        if (a != null && b == null) return true;
        return !a.equals(b);
    }

    public static boolean hasChanged(final Integer a, final Integer b) {
        if (a == null && b == null) return false;
        if (a == null && b != null) return true;
        if (a != null && b == null) return true;
        return !a.equals(b);
    }

    public static boolean hasChanged(final Boolean a, final Boolean b) {
        if (a == null && b == null) return false;
        if (a == null && b != null) return true;
        if (a != null && b == null) return true;
        return !a.equals(b);
    }

}
