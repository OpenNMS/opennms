package org.opennms.netmgt.dao;

import java.util.Collection;

import org.opennms.netmgt.model.OnmsLinkState;

/**
 * <p>LinkStateDao interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface LinkStateDao extends OnmsDao<OnmsLinkState, Integer> {
    /**
     * <p>findAll</p>
     *
     * @param offset a {@link java.lang.Integer} object.
     * @param limit a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLinkState> findAll(Integer offset, Integer limit);
    /**
     * <p>findById</p>
     *
     * @param id a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsLinkState} object.
     */
    OnmsLinkState findById(Integer id);
    /**
     * <p>findByDataLinkInterfaceId</p>
     *
     * @param interfaceId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsLinkState} object.
     */
    OnmsLinkState findByDataLinkInterfaceId(Integer interfaceId);
    /**
     * <p>findByNodeId</p>
     *
     * @param nodeId a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLinkState> findByNodeId(Integer nodeId);
    /**
     * <p>findByNodeParentId</p>
     *
     * @param nodeParentId a {@link java.lang.Integer} object.
     * @return a {@link java.util.Collection} object.
     */
    Collection<OnmsLinkState> findByNodeParentId(Integer nodeParentId);
}
