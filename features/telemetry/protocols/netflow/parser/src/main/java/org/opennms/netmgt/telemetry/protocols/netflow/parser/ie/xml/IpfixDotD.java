package org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name = "ipfix-dot-d")
public class IpfixDotD {
    private List<IpfixElements> ipfixElements = new ArrayList<>();

    public List<IpfixElements> getIpfixElements() {
        return ipfixElements;
    }

    public void setIpfixElements(List<IpfixElements> ipfixElements) {
        this.ipfixElements = ipfixElements;
    }

    @Override
    public String toString() {
        return "IpfixDotD{" +
                "ipfixElements=" + ipfixElements +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IpfixDotD ipfixDotD = (IpfixDotD) o;
        return Objects.equals(ipfixElements, ipfixDotD.ipfixElements);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ipfixElements);
    }
}
