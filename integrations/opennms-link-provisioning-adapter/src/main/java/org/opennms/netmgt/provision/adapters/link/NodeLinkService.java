package org.opennms.netmgt.provision.adapters.link;

import java.util.Collection;

import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsLinkState;

/**
 * <p>NodeLinkService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface NodeLinkService {
    
    /**
     * <p>getNodeLabel</p>
     *
     * @param nodeId a int.
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel(int nodeId);
    /**
     * <p>createLink</p>
     *
     * @param nodeParentId a int.
     * @param nodeId a int.
     */
    public void createLink(int nodeParentId, int nodeId);
    /**
     * <p>saveLinkState</p>
     *
     * @param state a {@link org.opennms.netmgt.model.OnmsLinkState} object.
     */
    public void saveLinkState(OnmsLinkState state);
    /**
     * <p>getNodeId</p>
     *
     * @param endPoint a {@link java.lang.String} object.
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getNodeId(String endPoint);
    /**
     * <p>getLinkContainingNodeId</p>
     *
     * @param nodeId a int.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<DataLinkInterface> getLinkContainingNodeId(int nodeId);
    /**
     * <p>updateLinkStatus</p>
     *
     * @param nodeParentId a int.
     * @param nodeId a int.
     * @param status a {@link java.lang.String} object.
     */
    public void updateLinkStatus(int nodeParentId, int nodeId, String status);
    /**
     * <p>getLinkStateForInterface</p>
     *
     * @param dataLinkInterface a {@link org.opennms.netmgt.model.DataLinkInterface} object.
     * @return a {@link org.opennms.netmgt.model.OnmsLinkState} object.
     */
    public OnmsLinkState getLinkStateForInterface(DataLinkInterface dataLinkInterface);
    /**
     * <p>getPrimaryAddress</p>
     *
     * @param nodeId a int.
     * @return a {@link java.lang.String} object.
     */
    public String getPrimaryAddress(int nodeId);
    /**
     * <p>nodeHasEndPointService</p>
     *
     * @param nodeId a int.
     * @return a boolean.
     */
    public boolean nodeHasEndPointService(int nodeId);
    /**
     * <p>getEndPointStatus</p>
     *
     * @param nodeId a int.
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getEndPointStatus(int nodeId);
}
