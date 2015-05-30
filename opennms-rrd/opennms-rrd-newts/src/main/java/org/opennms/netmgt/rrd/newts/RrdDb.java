package org.opennms.netmgt.rrd.newts;

import com.google.common.base.Objects;

/**
 * Wrapper for holding the path to an RRD file.
 *
 * Used to convert the path to a resource id.
 *
 * @author jwhite
 */
public class RrdDb {
    private final String m_path;

    public RrdDb(String path) {
        m_path = path;
    }

    public String getPath() {
        return m_path;
    }

    public String toString() {
        return Objects.toStringHelper(this.getClass())
                .add("path", m_path)
                .toString();
    }
}
