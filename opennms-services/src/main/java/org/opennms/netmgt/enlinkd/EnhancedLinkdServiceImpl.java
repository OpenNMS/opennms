package org.opennms.netmgt.enlinkd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.dao.support.UpsertTemplate;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OspfElement;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.PrimaryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
public class EnhancedLinkdServiceImpl implements EnhancedLinkdService {
		
//	private final static Logger LOG = LoggerFactory.getLogger(EnhancedLinkdServiceImpl.class);

    @Autowired
    private PlatformTransactionManager m_transactionManager;
	
	private NodeDao m_nodeDao;

	private LldpLinkDao m_lldpLinkDao;
	
	private OspfLinkDao m_ospfLinkDao;

    @Override
	public List<LinkableNode> getSnmpNodeList() {
		final List<LinkableNode> nodes = new ArrayList<LinkableNode>();
		
		final Criteria criteria = new Criteria(OnmsNode.class);
		criteria.setAliases(Arrays.asList(new Alias[] {
	            new Alias("ipInterfaces", "iface", JoinType.LEFT_JOIN)
	        }));    
        criteria.addRestriction(new EqRestriction("type", NodeType.ACTIVE));
        criteria.addRestriction(new EqRestriction("iface.isSnmpPrimary", PrimaryType.PRIMARY));
        for (final OnmsNode node : m_nodeDao.findMatching(criteria)) {
            final String sysObjectId = node.getSysObjectId();
            nodes.add(new LinkableNode(node.getId(), node.getPrimaryInterface().getIpAddress(), sysObjectId == null? "-1" : sysObjectId));
        }
        return nodes;
	}

	@Override
	public LinkableNode getSnmpNode(final int nodeid) {
		final Criteria criteria = new Criteria(OnmsNode.class);
		criteria.setAliases(Arrays.asList(new Alias[] {
	            new Alias("ipInterfaces", "iface", JoinType.LEFT_JOIN)
	        }));    
        criteria.addRestriction(new EqRestriction("type", NodeType.ACTIVE));
        criteria.addRestriction(new EqRestriction("iface.isSnmpPrimary", PrimaryType.PRIMARY));
        criteria.addRestriction(new EqRestriction("id", nodeid));
        final List<OnmsNode> nodes = m_nodeDao.findMatching(criteria);

        if (nodes.size() > 0) {
        	final OnmsNode node = nodes.get(0);
        	final String sysObjectId = node.getSysObjectId();
			return new LinkableNode(node.getId(), node.getPrimaryInterface().getIpAddress(), sysObjectId == null? "-1" : sysObjectId);
        } else {
        	return null;
        }
	}

	@Override
	public void delete(int nodeId) {
		Date now = new Date();
		reconcileLldp(nodeId, now);
		reconcileCdp(nodeId, now);
		reconcileOspf(nodeId, now);
		reconcileIpNetToMedia(nodeId, now);
		reconcileBridge(nodeId, now);
	}

	@Override
	public void reconcileLldp(int nodeId, Date now) {
		m_lldpLinkDao.deleteByNodeIdOlderThen(nodeId, now);
		m_lldpLinkDao.flush();
	}

	@Override
	public void reconcileCdp(int nodeId, Date now) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconcileIsis(int nodeId, Date now) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconcileOspf(int nodeId, Date now) {
		m_ospfLinkDao.deleteByNodeIdOlderThen(nodeId, now);
		m_ospfLinkDao.flush();
	}

	@Override
	public void reconcileIpNetToMedia(int nodeId, Date now) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reconcileBridge(int nodeId, Date now) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void store(int nodeId, LldpLink link) {
		if (link == null)
			return;
		saveLldpLink(nodeId, link);
	}

	@Transactional
    protected synchronized void saveLldpLink(final int nodeId, final LldpLink saveMe) {
		new UpsertTemplate<LldpLink, LldpLinkDao>(m_transactionManager,m_lldpLinkDao) {

			@Override
			protected LldpLink query() {
				return m_dao.get(nodeId, saveMe.getLldpLocalPortNum());
			}

			@Override
			protected LldpLink doUpdate(LldpLink dbLldpLink) {
				dbLldpLink.merge(saveMe);
				m_dao.update(dbLldpLink);
				m_dao.flush();
				return dbLldpLink;
			}

			@Override
			protected LldpLink doInsert() {
				final OnmsNode node = m_nodeDao.get(nodeId);
				if ( node == null )
					return null;
				saveMe.setNode(node);
				saveMe.setLldpLinkLastPollTime(saveMe.getLldpLinkCreateTime());
				m_dao.saveOrUpdate(saveMe);
				m_dao.flush();
				return saveMe;
			}
			
		}.execute();
	}

	@Override
	@Transactional
	public void store(int nodeId, LldpElement element) {
		if (element ==  null)
			return;
		final OnmsNode node = m_nodeDao.get(nodeId);
		if ( node == null )
			return;
		
		LldpElement dbelement = node.getLldpElement();
		if (dbelement != null) {
			dbelement.merge(element);
			node.setLldpElement(dbelement);
		} else {
			element.setNode(node);
			element.setLldpNodeLastPollTime(element.getLldpNodeCreateTime());
			node.setLldpElement(element);
		}

        m_nodeDao.saveOrUpdate(node);
		m_nodeDao.flush();
		
	}

	@Override
	@Transactional
	public void store(int nodeId, OspfLink link) {
		if (link == null)
			return;
		saveOspfLink(nodeId, link);
	}
	
	private void saveOspfLink(final int nodeId, final OspfLink saveMe) {
		new UpsertTemplate<OspfLink, OspfLinkDao>(m_transactionManager,m_ospfLinkDao) {

			@Override
			protected OspfLink query() {
				return m_dao.get(nodeId, saveMe.getOspfRemRouterId(),saveMe.getOspfRemIpAddr(),saveMe.getOspfRemAddressLessIndex());
			}

			@Override
			protected OspfLink doUpdate(OspfLink dbLldpLink) {
				dbLldpLink.merge(saveMe);
				m_dao.update(dbLldpLink);
				m_dao.flush();
				return dbLldpLink;
			}

			@Override
			protected OspfLink doInsert() {
				final OnmsNode node = m_nodeDao.get(nodeId);
				if ( node == null )
					return null;
				saveMe.setNode(node);
				saveMe.setOspfLinkLastPollTime(saveMe.getOspfLinkCreateTime());
				m_dao.saveOrUpdate(saveMe);
				m_dao.flush();
				return saveMe;
			}
			
		}.execute();
		
	}

	@Override
	@Transactional
	public void store(int nodeId, OspfElement element) {
		if (element ==  null)
			return;
		final OnmsNode node = m_nodeDao.get(nodeId);
		if ( node == null )
			return;
		
		OspfElement dbelement = node.getOspfElement();
		if (dbelement != null) {
			dbelement.merge(element);
			node.setOspfElement(dbelement);
		} else {
			element.setNode(node);
			element.setOspfNodeLastPollTime(element.getOspfNodeCreateTime());
			node.setOspfElement(element);
		}

        m_nodeDao.saveOrUpdate(node);
		m_nodeDao.flush();
		
	}

	@Override
	@Transactional
	public void store(int nodeId, IsIsElement element) {
		if (element ==  null)
			return;
		final OnmsNode node = m_nodeDao.get(nodeId);
		if ( node == null )
			return;
		
		IsIsElement dbelement = node.getIsisElement();
		if (dbelement != null) {
			dbelement.merge(element);
			node.setIsisElement(dbelement);
		} else {
			element.setNode(node);
			element.setIsisNodeLastPollTime(element.getIsisNodeCreateTime());
			node.setIsisElement(element);
		}

        m_nodeDao.saveOrUpdate(node);
		m_nodeDao.flush();
		
	}

	public LldpLinkDao getLldpLinkDao() {
		return m_lldpLinkDao;
	}

	public void setLldpLinkDao(LldpLinkDao lldpLinkDao) {
		m_lldpLinkDao = lldpLinkDao;
	}

	public NodeDao getNodeDao() {
		return m_nodeDao;
	}

	public void setNodeDao(NodeDao nodeDao) {
		m_nodeDao = nodeDao;
	}

	public OspfLinkDao getOspfLinkDao() {
		return m_ospfLinkDao;
	}

	public void setOspfLinkDao(OspfLinkDao ospfLinkDao) {
		m_ospfLinkDao = ospfLinkDao;
	}
	
}
