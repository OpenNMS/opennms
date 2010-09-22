package org.opennms.netmgt.model;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class CountedObject<T> implements Comparable<CountedObject<T>> {
    private T m_object;
    private long m_count;

    public CountedObject() {
    }

    public CountedObject(final T object, final Long count) {
        m_object = object;
        m_count = count;
    }

    public void setObject(final T object) {
        m_object = object;
    }

    public T getObject() {
        return m_object;
    }
    
    public void setCount(final int count) {
        m_count = count;
    }
    
    public Long getCount() {
        return m_count;
    }

    public int compareTo(final CountedObject<T> o) {
        return new CompareToBuilder()
            .append(this.getCount(), (o == null? null:o.getCount()))
            .append(this.getObject(), (o == null? null:o.getObject()))
            .toComparison();
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append(this.getObject())
            .append(this.getCount())
            .toString();
    }
}
