package org.opennms.netmgt.config.collectd.jmx;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a helper class to enable parsing of splitted mbeans-definitions.
 *
 **/
@XmlRootElement(name="mbeans")
@XmlAccessorType(XmlAccessType.FIELD)
public class Mbeans {
    @XmlElement(name="mbean")
    private List<Mbean> mbeanList = new ArrayList<>();

    public List<Mbean> getMbeanList() {
        return Collections.unmodifiableList(mbeanList);
    }
}