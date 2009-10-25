/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;

@XmlRootElement(name="endpoint-types")
public class EndPointTypeValidator {
    @XmlElement(name="endpoint-type")
    List<EndPointType> m_endPointConfigs = Collections.synchronizedList(new ArrayList<EndPointType>());
    
    public EndPointTypeValidator() {
        /*
        m_endPointConfigs.add(new EndPointType(".1.3.6.1.4.1.7262.1", and( match(LinkMonitorValidatorTest.AIR_PAIR_MODEM_LOSS_OF_SIGNAL, "^1$"), match(LinkMonitorValidatorTest.AIR_PAIR_R3_DUPLEX_MISMATCH, "^1$") )));
        m_endPointConfigs.add(new EndPointType(".1.3.6.1.4.1.7262.1", and( match(LinkMonitorValidatorTest.AIR_PAIR_MODEM_LOSS_OF_SIGNAL, "^1$"), match(LinkMonitorValidatorTest.AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, "^1$") )));
        m_endPointConfigs.add(new EndPointType(".1.3.6.1.4.1.7262.2.2", and( match(LinkMonitorValidatorTest.HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL, "^1$"), match(LinkMonitorValidatorTest.AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, "^1$") )));
        m_endPointConfigs.add(new EndPointType(".1.3.6.1.4.1.7262.2.3", and( match(LinkMonitorValidatorTest.AIR_PAIR_MODEM_LOSS_OF_SIGNAL, "^1$"), match(LinkMonitorValidatorTest.AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, "^1$") )));
        m_endPointConfigs.add(new EndPointType(".1.2.3.4", ping()));
        */
    }

    public List<EndPointType> getConfigs() {
        return m_endPointConfigs;
    }
    
    public void setConfigs(List<EndPointType> configs) {
        synchronized(m_endPointConfigs) {
            m_endPointConfigs.clear();
            m_endPointConfigs.addAll(configs);
        }
    }

    public void validate(EndPoint ep) throws EndPointStatusException {
        for (EndPointType config : m_endPointConfigs) {
            if (config.matches(ep)) {
                config.validate(ep);
                return;
            }
        }
        throw new EndPointStatusException(String.format("unable to find matching endpoint type config for endpoint %s", ep));
    }
}