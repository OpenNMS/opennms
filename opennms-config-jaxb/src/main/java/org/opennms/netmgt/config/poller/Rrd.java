/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.1.2.1</a>, using an XML
 * Schema.
 * $Id$
 */

package org.opennms.netmgt.config.poller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * RRD parameters
 */

@XmlRootElement(name="rrd")
@XmlAccessorType(XmlAccessType.NONE)
public class Rrd implements Serializable {
    private static final long serialVersionUID = -2267805562120742467L;

    /**
     * Step size for the RRD, in seconds.
     */
    @XmlAttribute(name="step")
    private Integer m_step;

    /**
     * Round Robin Archive definitions
     */
    @XmlElement(name="rra")
    private List<String> m_rras = new ArrayList<String>();

    public Rrd() {
        super();
    }

    public Rrd(int step, final String... rras) {
        this();
        setStep(step);
        if (rras != null) {
            for (final String rra : rras) {
                addRra(rra);
            }
        }
    }

    /**
     * Step size for the RRD, in seconds.
     */
    public Integer getStep() {
        return m_step == null? 0 : m_step;
    }

    public void setStep(final Integer step) {
        m_step = step;
    }

    public List<String> getRras() {
        if (m_rras == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_rras);
        }
    }

    public void setRras(final List<String> rras) {
        m_rras = new ArrayList<String>(rras);
    }

    public void addRra(final String rra) throws IndexOutOfBoundsException {
        m_rras.add(rra);
    }

    public boolean removeRra(final String rra) {
        return m_rras.remove(rra);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_rras == null) ? 0 : m_rras.hashCode());
        result = prime * result + ((m_step == null) ? 0 : m_step.hashCode());
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
        if (m_rras == null) {
            if (other.m_rras != null) {
                return false;
            }
        } else if (!m_rras.equals(other.m_rras)) {
            return false;
        }
        if (m_step == null) {
            if (other.m_step != null) {
                return false;
            }
        } else if (!m_step.equals(other.m_step)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Rrd[step=" + m_step + ",rras=" + m_rras + "]";
    }
}
