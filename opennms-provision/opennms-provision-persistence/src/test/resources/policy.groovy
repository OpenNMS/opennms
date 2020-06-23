import org.opennms.netmgt.model.OnmsIpInterface
import org.opennms.netmgt.model.PrimaryType
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation

for (OnmsIpInterface iface : node.getIpInterfaces()) {
    if (iface.getIpAddressAsString().matches("^172\\.1[67]\\..*")) {
        LOG.warn(iface.getIpAddressAsString() + " set to NOT_ELIGIBLE")
        iface.setIsSnmpPrimary(PrimaryType.NOT_ELIGIBLE)
    } else {
        LOG.warn(iface.getIpAddressAsString() + " set to PRIMARY")
        iface.setIsSnmpPrimary(PrimaryType.PRIMARY)
    }
}

node.setLocation(new OnmsMonitoringLocation("custom-location", ""));

return node;