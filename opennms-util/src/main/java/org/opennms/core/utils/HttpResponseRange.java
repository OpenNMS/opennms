package org.opennms.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpResponseRange {
    private static final Pattern RANGE_PATTERN = Pattern.compile("([1-5][0-9][0-9])(?:-([1-5][0-9][0-9]))?");
    private final int m_begin;
    private final int m_end;

    public HttpResponseRange(String rangeSpec) {
        Matcher matcher = RANGE_PATTERN.matcher(rangeSpec);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid range spec: " + rangeSpec);
        }

        String beginSpec = matcher.group(1);
        String endSpec = matcher.group(2);

        m_begin = Integer.parseInt(beginSpec);

        if (endSpec == null) {
            m_end = m_begin;
        } else {
            m_end = Integer.parseInt(endSpec);
        }
    }

    public boolean contains(int responseCode) {
        return (m_begin <= responseCode && responseCode <= m_end);
    }

    public String toString() {
        if (m_begin == m_end) {
            return Integer.toString(m_begin);
        } else {
            return Integer.toString(m_begin) + '-' + Integer.toString(m_end);
        }
    }
}