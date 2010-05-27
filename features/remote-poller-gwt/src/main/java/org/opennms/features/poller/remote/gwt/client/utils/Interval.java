package org.opennms.features.poller.remote.gwt.client.utils;

public class Interval implements Comparable<Interval> {
    private long m_start;
    private long m_end;

    public Interval(long start, long end) {
        m_start = start;
        m_end = end;
    }

    public long getStartMillis() {
        return m_start;
    }

    public void setStartMillis(final long start) {
        m_start = start;
    }

    public long getEndMillis() {
        return m_end;
    }

    public void setEndMillis(final long end) {
        m_end = end;
    }

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

    public int compareTo(final Interval that) {
        if (that == null) return -1;
        return new CompareToBuilder()
        .append(this.getStartMillis(), that.getStartMillis())
        .append(this.getEndMillis(), that.getEndMillis())
        .toComparison();
    }

}
