/**
 * 
 */
package org.opennms.netmgt.provision.adapters.link.endpoint;

import static org.opennms.netmgt.provision.adapters.link.EndPointValidationExpressions.and;
import static org.opennms.netmgt.provision.adapters.link.EndPointValidationExpressions.match;
import static org.opennms.netmgt.provision.adapters.link.EndPointValidationExpressions.ping;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.provision.LinkMonitorValidatorTest;
import org.opennms.netmgt.provision.adapters.link.EndPoint;
import org.opennms.netmgt.provision.adapters.link.EndPointStatusException;

public class EndPointTypeValidator {
    List<EndPointType> m_endPointConfigs = new ArrayList<EndPointType>();
    
    public EndPointTypeValidator() {
        m_endPointConfigs.add(new EndPointType(".1.3.6.1.4.1.7262.1", and( match(LinkMonitorValidatorTest.AIR_PAIR_MODEM_LOSS_OF_SIGNAL, "^1$"), match(LinkMonitorValidatorTest.AIR_PAIR_R3_DUPLEX_MISMATCH, "^1$") )));
        m_endPointConfigs.add(new EndPointType(".1.3.6.1.4.1.7262.1", and( match(LinkMonitorValidatorTest.AIR_PAIR_MODEM_LOSS_OF_SIGNAL, "^1$"), match(LinkMonitorValidatorTest.AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, "^1$") )));
        m_endPointConfigs.add(new EndPointType(".1.3.6.1.4.1.7262.2.2", and( match(LinkMonitorValidatorTest.HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL, "^1$"), match(LinkMonitorValidatorTest.AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, "^1$") )));
        m_endPointConfigs.add(new EndPointType(".1.3.6.1.4.1.7262.2.3", and( match(LinkMonitorValidatorTest.AIR_PAIR_MODEM_LOSS_OF_SIGNAL, "^1$"), match(LinkMonitorValidatorTest.AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, "^1$") )));
        m_endPointConfigs.add(new EndPointType(".1.2.3.4", ping()));
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