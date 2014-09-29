package org.opennms.features.vaadin.nodemaps.internal.gwt.shared;

public abstract class Util {

    public static <T> boolean hasChanged(final T a, final T b) {
        if (a == null && b == null) return false;
        if (a == null && b != null) return true;
        if (a != null && b == null) return true;
        return !a.equals(b);
    }

}
