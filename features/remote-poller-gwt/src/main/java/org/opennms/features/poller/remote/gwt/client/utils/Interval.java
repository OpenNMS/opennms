package org.opennms.features.poller.remote.gwt.client.utils;

/**
 * <p>Interval class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Interval implements Comparable<Interval> {
    private long m_start;
    private long m_end;

    /**
     * <p>Constructor for Interval.</p>
     *
     * @param start a long.
     * @param end a long.
     */
    public Interval(long start, long end) {
        m_start = start;
        m_end = end;
    }

    /**
     * <p>getStartMillis</p>
     *
     * @return a long.
     */
    public long getStartMillis() {
        return m_start;
    }

    /**
     * <p>setStartMillis</p>
     *
     * @param start a long.
     */
    public void setStartMillis(final long start) {
        m_start = start;
    }

    /**
     * <p>getEndMillis</p>
     *
     * @return a long.
     */
    public long getEndMillis() {
        return m_end;
    }

    /**
     * <p>setEndMillis</p>
     *
     * @param end a long.
     */
    public void setEndMillis(final long end) {
        m_end = end;
    }

    /**
     * <p>overlaps</p>
     *
     * @param that a {@link org.opennms.features.poller.remote.gwt.client.utils.Interval} object.
     * @return a boolean.
     */
    public boolean overlaps(final Interval that) {
        if (this.getStartMillis() <= that.getStartMillis() && this.getEndMillis() >= that.getEndMillis()) {
            // completely surrounds
            return true;
        } else if (this.getStartMillis() >= that.getStartMillis() && this.getEndMillis() <= that.getEndMillis()) {
            // completely inside
            return true;
        } else if (this.getStartMillis() <= that.getStartMillis() && this.getEndMillis() >= that.getStartMillis()) {
            // overlaps start
            return true;
        } else if (this.getEndMillis() >= that.getEndMillis() && this.getStartMillis() <= that.getEndMillis()) {
            // overlaps end
            return true;
        }
        return false;
    }

    /**
     * <p>compareTo</p>
     *
     * @param that a {@link org.opennms.features.poller.remote.gwt.client.utils.Interval} object.
     * @return a int.
     */
    public int compareTo(final Interval that) {
        if (that == null) return -1;
        return new CompareToBuilder()
        .append(this.getStartMillis(), that.getStartMillis())
        .append(this.getEndMillis(), that.getEndMillis())
        .toComparison();
    }

}
