package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.enlinkd.model.LldpLink;

public class TimeTetraLldpLink  {
    private Integer tmnxLldpRemLocalDestMACAddress;
    private LldpLink lldpLink = new LldpLink();

    public TimeTetraLldpLink() {
    }

    public LldpLink getLldpLink() {
        return lldpLink;
    }

    public void setLldpLink(LldpLink lldpLink) {
        this.lldpLink = lldpLink;
    }

    public Integer getTmnxLldpRemLocalDestMACAddress() {
        return tmnxLldpRemLocalDestMACAddress;
    }

    public void setTmnxLldpRemLocalDestMACAddress(Integer tmnxLldpRemLocalDestMACAddress) {
        this.tmnxLldpRemLocalDestMACAddress = tmnxLldpRemLocalDestMACAddress;
    }
}
