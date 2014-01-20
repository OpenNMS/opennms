@XmlSchema(
    namespace="http://xmlns.opennms.org/xsd/config/collectd",
    elementFormDefault=javax.xml.bind.annotation.XmlNsForm.QUALIFIED,
    xmlns={
        @XmlNs(prefix="", namespaceURI="http://xmlns.opennms.org/xsd/config/collectd")
    }
)
@XmlPrefix("collectd")
package org.opennms.netmgt.config.collectd;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;

import org.opennms.core.xml.XmlPrefix;

