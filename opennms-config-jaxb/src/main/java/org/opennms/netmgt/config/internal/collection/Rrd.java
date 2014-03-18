package org.opennms.netmgt.config.internal.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.config.api.collection.IRrd;

@XmlRootElement(name="rrd")
@XmlAccessorType(XmlAccessType.NONE)
public class Rrd implements IRrd {
    @XmlAttribute(name="step")
    private int m_step;

    @XmlElement(name="rra")
    private String[] m_rras;

    public Rrd() {
    }

    public Rrd(final int step) {
        m_step = step;
    }

    @Override
    public int getStep() {
        return m_step;
    }

    @Override
    public String[] getRras() {
        return m_rras;
    }

    public void addRra(final String rra) {
        final List<String> rras = m_rras == null? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(m_rras));
        rras.add(rra);
        m_rras = rras.toArray(new String[rras.size()]);
    }

    @Override
    public String toString() {
        return "Rrd [step=" + m_step + ", rras=" + Arrays.toString(m_rras) + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(m_rras);
        result = prime * result + m_step;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Rrd)) {
            return false;
        }
        final Rrd other = (Rrd) obj;
        if (!Arrays.equals(m_rras, other.m_rras)) {
            return false;
        }
        if (m_step != other.m_step) {
            return false;
        }
        return true;
    }

}
