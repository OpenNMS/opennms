package org.opennms.netmgt.dao.hibernate;

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.opennms.netmgt.dao.LinkStateDao;
import org.opennms.netmgt.model.OnmsLinkState;
import org.springframework.orm.hibernate3.HibernateCallback;

public class LinkStateDaoHibernate extends AbstractDaoHibernate<OnmsLinkState, Integer> implements LinkStateDao {
    public LinkStateDaoHibernate() {
        super(OnmsLinkState.class);
    }

    @SuppressWarnings("unchecked")
    public Collection<OnmsLinkState> findAll(final Integer offset, final Integer limit) {
        return (Collection<OnmsLinkState>)getHibernateTemplate().execute(new HibernateCallback() {

            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsLinkState.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .list();
            }
        });
    }

    public OnmsLinkState findById(Integer id) {
        return findUnique("from OnmsLinkState as ls where ls.id = ?", id);
    }

    public OnmsLinkState findByDataLinkInterfaceId(final Integer interfaceId) {
        return findUnique("from OnmsLinkState as ls where ls.dataLinkInterface.id = ?", interfaceId);
    }

    public Collection<OnmsLinkState> findByNodeId(Integer nodeId) {
        return find("from OnmsLinkState as ls where ls.dataLinkInterface.nodeId = ?", nodeId);
    }

    public Collection<OnmsLinkState> findByNodeParentId(Integer nodeParentId) {
        return find("from OnmsLinkState as ls where ls.dataLinkInterface.nodeParentId = ?", nodeParentId);
    }

}
