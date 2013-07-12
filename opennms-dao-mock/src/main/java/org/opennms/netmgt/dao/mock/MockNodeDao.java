package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.SurveillanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockNodeDao extends AbstractMockDao<OnmsNode, Integer> implements NodeDao {
    private static final Logger LOG = LoggerFactory.getLogger(MockNodeDao.class);
    private AtomicInteger m_id = new AtomicInteger(0);

    @Override
    protected void generateId(final OnmsNode node) {
        node.setId(m_id.incrementAndGet());
    }

    @Override
    protected Integer getId(final OnmsNode node) {
        return node.getId();
    }

    @Override
    public void delete(final OnmsNode node) {
        for (final OnmsIpInterface iface : node.getIpInterfaces()) {
            getIpInterfaceDao().delete(iface);
        }
        for (final OnmsSnmpInterface iface : node.getSnmpInterfaces()) {
            getSnmpInterfaceDao().delete(iface);
        }
        super.delete(node);
    }

    @Override
    public void update(final OnmsNode node) {
        if (node == null) return;
        super.update(node);
        updateSubObjects(node);
    }
    
    @Override
    public void save(final OnmsNode node) {
        if (node == null) return;
        super.save(node);
        updateSubObjects(node);
    }

    @Override
    public void flush() {
        super.flush();
        for (final OnmsNode node : findAll()) {
            updateSubObjects(node);
        }
    }

    private void updateSubObjects(final OnmsNode node) {
        getAssetRecordDao().saveOrUpdate(node.getAssetRecord());
        for (final OnmsCategory cat : node.getCategories()) {
            getCategoryDao().saveOrUpdate(cat);
        }
        getDistPollerDao().saveOrUpdate(node.getDistPoller());
        final SnmpInterfaceDao snmpInterfaceDao = getSnmpInterfaceDao();
        for (final OnmsSnmpInterface iface : node.getSnmpInterfaces()) {
            snmpInterfaceDao.saveOrUpdate(iface);
        }
        /* not sure if this is necessary */
        for (final OnmsIpInterface iface : getIpInterfaceDao().findAll()) {
            final OnmsSnmpInterface snmpInterface = iface.getSnmpInterface();
            if (snmpInterface != null && snmpInterface.getId() != null) {
                if (snmpInterfaceDao.get(snmpInterface.getId()) == null) {
                    getIpInterfaceDao().delete(iface);
                }
            }
        }
        for (final OnmsIpInterface iface : node.getIpInterfaces()) {
            getIpInterfaceDao().saveOrUpdate(iface);
        }
    }

    @Override
    public OnmsNode get(final String lookupCriteria) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public String getLabelForId(final Integer id) {
        final OnmsNode node = get(id);
        return node == null? null : node.getLabel();
    }

    @Override
    public List<OnmsNode> findByLabel(final String label) {
        final List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        for (final OnmsNode node : findAll()) {
            if (label.equals(node.getLabel())) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @Override
    public List<OnmsNode> findNodes(final OnmsDistPoller dp) {
        final List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        for (final OnmsNode node : findAll()) {
            if (node.getDistPoller().equals(dp)) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @Override
    public OnmsNode getHierarchy(final Integer id) {
        return get(id);
    }

    @Override
    public Map<String, Integer> getForeignIdToNodeIdMap(final String foreignSource) {
        final Map<String,Integer> nodes = new HashMap<String,Integer>();
        for (final OnmsNode node : findAll()) {
            if (foreignSource.equals(node.getForeignSource())) {
                nodes.put(node.getForeignId(), node.getId());
            }
        }
        return nodes;
    }

    @Override
    public List<OnmsNode> findAllByVarCharAssetColumn(final String columnName, final String columnValue) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsNode.class);
        builder.alias("assetRecord", "assets");
        builder.eq("assets." + columnName, columnValue);
        return findMatching(builder.toCriteria());
    }

    @Override
    public List<OnmsNode> findAllByVarCharAssetColumnCategoryList(final String columnName, final String columnValue, final Collection<OnmsCategory> categories) {
        final List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        for (final OnmsNode node : findAllByVarCharAssetColumn(columnName, columnValue)) {
            for (final OnmsCategory cat : categories) {
                if (node.hasCategory(cat.getName())) {
                    nodes.add(node);
                    break;
                }
            }
        }
        return nodes;
    }

    @Override
    public List<OnmsNode> findByCategory(final OnmsCategory category) {
        final List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        for (final OnmsNode node : findAll()) {
            if (node.getCategories().contains(category)) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @Override
    public List<OnmsNode> findAllByCategoryList(final Collection<OnmsCategory> categories) {
        final List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        for (final OnmsNode node : findAll()) {
            for (final OnmsCategory category : categories) {
                if (node.getCategories().contains(category)) {
                    nodes.add(node);
                }
            }
        }
        return nodes;
    }

    @Override
    public List<OnmsNode> findAllByCategoryLists(final Collection<OnmsCategory> rowCatNames, final Collection<OnmsCategory> colCatNames) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public List<OnmsNode> findByForeignSource(final String foreignSource) {
        final List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        for (final OnmsNode node : findAll()) {
            if (foreignSource.equals(node.getForeignSource())) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    @Override
    public OnmsNode findByForeignId(final String foreignSource, final String foreignId) {
        for (final OnmsNode node : findByForeignSource(foreignSource)) {
            if (foreignId.equals(node.getForeignId())) {
                return node;
            }
        }
        return null;
    }

    @Override
    public int getNodeCountForForeignSource(final String foreignSource) {
        return findByForeignSource(foreignSource).size();
    }

    @Override
    public List<OnmsNode> findAllProvisionedNodes() {
        final List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        for (final OnmsNode node : findAll()) {
            if (node.getForeignSource() != null) nodes.add(node);
        }
        return nodes;
    }

    @Override
    public List<OnmsIpInterface> findObsoleteIpInterfaces(final Integer nodeId, final Date scanStamp) {
        final List<OnmsIpInterface> ifaces = new ArrayList<OnmsIpInterface>();
        final OnmsNode node = get(nodeId);
        if (node == null) return ifaces;
        
        for (final OnmsIpInterface iface : node.getIpInterfaces()) {
            if (iface.isPrimary()) continue;
            if (iface.getIpLastCapsdPoll() == null
                    || iface.getIpLastCapsdPoll().before(scanStamp)) ifaces.add(iface);
        }

        return ifaces;
    }

    @Override
    public void deleteObsoleteInterfaces(final Integer nodeId, final Date scanStamp) {
        final OnmsNode node = get(nodeId);
        if (node == null) return;

        for (final OnmsIpInterface iface : findObsoleteIpInterfaces(nodeId, scanStamp)) {
            LOG.debug("Deleting obsolete IP interface: {}", iface);
            node.getIpInterfaces().remove(iface);
            getIpInterfaceDao().delete(iface.getId());
        }
        final Collection<OnmsSnmpInterface> snmpInterfaces = Collections.unmodifiableCollection(node.getSnmpInterfaces());
        for (final OnmsSnmpInterface iface : snmpInterfaces) {
            if (iface.getLastCapsdPoll() == null
                    || iface.getLastCapsdPoll().before(scanStamp)) {
                LOG.debug("Deleting obsolete SNMP interface: {}", iface);
                snmpInterfaces.remove(iface);
                getSnmpInterfaceDao().delete(iface.getId());
            }
        }
    }

    @Override
    public void updateNodeScanStamp(final Integer nodeId, final Date scanStamp) {
        get(nodeId).setLastCapsdPoll(scanStamp);
    }

    @Override
    public Collection<Integer> getNodeIds() {
        final List<Integer> ids = new ArrayList<Integer>();
        for (final OnmsNode node : findAll()) {
            ids.add(node.getId());
        }
        return ids;
    }

    @Override
    public List<OnmsNode> findByForeignSourceAndIpAddress(final String foreignSource, final String ipAddress) {
        final List<OnmsNode> nodes = new ArrayList<OnmsNode>();
        for (final OnmsNode node : findAll()) {
            if (foreignSource.equals(node.getForeignSource())) {
                final OnmsIpInterface iface = node.getIpInterfaceByIpAddress(ipAddress);
                if (iface != null) nodes.add(node);
                continue;
            }
        }
        return nodes;
    }

    @Override
    public SurveillanceStatus findSurveillanceStatusByCategoryLists(final Collection<OnmsCategory> rowCategories, final Collection<OnmsCategory> columnCategories) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Integer getNextNodeId(final Integer nodeId) {
        Integer next = null;
        for (final OnmsNode node : findAll()) {
            if (node.getId() > nodeId) {
                if (next == null || (node.getId() < next)) {
                    next = node.getId();
                }
            }
        }
        return next;
    }

    @Override
    public Integer getPreviousNodeId(final Integer nodeId) {
        Integer previous = null;
        for (final OnmsNode node : findAll()) {
            if (node.getId() < nodeId) {
                if (previous == null || (previous < node.getId())) {
                    previous = node.getId();
                }
            }
        }
        return previous;
    }

    public int getNextNodeId() {
        return m_id.get() + 1;
    }

}
