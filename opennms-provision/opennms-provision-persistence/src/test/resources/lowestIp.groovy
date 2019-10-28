import org.opennms.core.utils.ByteArrayComparator
import org.opennms.netmgt.model.OnmsIpInterface

import java.util.stream.Collectors


List<byte[]> ipAddresses = node.getIpInterfaces().stream().map({ ipIface -> ipIface.getIpAddress().getAddress() }).collect(Collectors.toList());
byte[] lowestIp = Collections.min(ipAddresses, new ByteArrayComparator());
OnmsIpInterface ipInterface = interfacesWithSNMP.stream().filter({ ipIface -> ipIface.getIpAddress().getAddress().equals(lowestIp) }).findFirst().orElse(null);

if(ipInterface != null) {
    node.setLabel(ipInterface.getIpHostName());
}
return node;