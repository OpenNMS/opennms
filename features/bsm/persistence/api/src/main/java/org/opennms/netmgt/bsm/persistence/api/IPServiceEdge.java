package org.opennms.netmgt.bsm.persistence.api;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsMonitoredService;

import com.google.common.collect.Sets;

@Entity
@Table(name = "bsm_service_ifservices")
@PrimaryKeyJoinColumn(name="id")
public class IPServiceEdge extends AbstractBusinessServiceEdge  {
    // TODO: The distributed poller name (now monitoring system name?) should be part of the edge details
    public static final String DEFAULT_DISTRIBUTED_POLLER_NAME = "";

    private int m_ipServiceId;

    private OnmsMonitoredService m_ipService;

    @Column(name = "ifserviceid", nullable = false)
    public int getIpServiceId() {
        return m_ipServiceId;
    }

    public void setIpServiceId(int id) {
        m_ipServiceId = id;
    }

    // TODO: MVR: For some reason these are serializing as byte arrays instead of integers
    @Transient
    public OnmsMonitoredService getIpService() {
        return m_ipService;
    }

    public void setIpService(OnmsMonitoredService ipService) {
        m_ipService = ipService;
    }

    @Override
    @Transient
    public Set<String> getReductionKeys() {
        final String nodeLostServiceReductionKey = String.format("%s:%s:%d:%s:%s",
                EventConstants.NODE_LOST_SERVICE_EVENT_UEI, DEFAULT_DISTRIBUTED_POLLER_NAME,
                m_ipService.getNodeId(), InetAddressUtils.toIpAddrString(m_ipService.getIpAddress()),
                m_ipService.getServiceName());

        // When node processing is enabled, we may get node down instead of node lost service events
        final String nodeDownReductionKey = String.format("%s:%s:%d",
                EventConstants.NODE_DOWN_EVENT_UEI, DEFAULT_DISTRIBUTED_POLLER_NAME,
                m_ipService.getNodeId());

        return Sets.newHashSet(nodeLostServiceReductionKey, nodeDownReductionKey);
    }

    // OnmsMonitoredService objects don't properly support the equals() and hashCode() methods
    // so we resort to comparing their IDs, which is sufficient in the case of the Business Service
}
