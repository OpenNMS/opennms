package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import java.util.logging.Logger;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.NodeMarker;

public interface MarkerFilter {
    public static enum MatchType {
        // substring search
        SUBSTRING,
        // exact match
        EXACT,
        // search in a comma-separated list
        IN;

        public static final Logger LOG = Logger.getLogger(MatchType.class.getName());

        public static MatchType fromToken(final String token) {
            if ("in".equals(token) || " in ".equals(token)) {
                return MatchType.IN;
            } else if ("=".equals(token)) {
                return MatchType.EXACT;
            } else if (":".equals(token)) {
                return MatchType.SUBSTRING;
            } else {
                LOG.warning("Unknown match token: " + token + ", blowing things up!");
                return null;
            }
        }
    };

    public abstract boolean matches(final NodeMarker marker);
}
