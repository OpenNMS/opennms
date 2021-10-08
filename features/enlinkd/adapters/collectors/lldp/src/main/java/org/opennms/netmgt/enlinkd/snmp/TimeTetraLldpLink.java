package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.enlinkd.model.LldpLink;

public class TimeTetraLldpLink extends LldpLink {
    private Integer tmnxLldpRemLocalDestMACAddress;

    public TimeTetraLldpLink() {
        super();
    }

    public Integer getTmnxLldpRemLocalDestMACAddress() {
        return tmnxLldpRemLocalDestMACAddress;
    }

    public void setTmnxLldpRemLocalDestMACAddress(Integer tmnxLldpRemLocalDestMACAddress) {
        this.tmnxLldpRemLocalDestMACAddress = tmnxLldpRemLocalDestMACAddress;
    }
}
