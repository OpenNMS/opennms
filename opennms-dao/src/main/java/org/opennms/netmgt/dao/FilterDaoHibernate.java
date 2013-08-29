package org.opennms.netmgt.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.FilterDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.OnmsFilter;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.sql.SQLException;
import java.util.List;

public class FilterDaoHibernate extends AbstractDaoHibernate<OnmsFilter, Integer> implements FilterDao {

    public FilterDaoHibernate() {
        super(OnmsFilter.class);
    }

    @Override
    public OnmsFilter findBy(final String userName, final String filterName) {
        return getHibernateTemplate().execute(new HibernateCallback<OnmsFilter>() {
            @Override
            public OnmsFilter doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from OnmsFilter f where f.username = :userName and f.filterName = :filterName");
                query.setParameter("filterName", filterName);
                query.setParameter("userName", userName);
                Object result = query.uniqueResult();
                if (result == null) return null;
                return (OnmsFilter)result;
            }
        });
    }

    @Override
    public List<OnmsFilter> findBy(final String userName, final OnmsFilter.Page page) {
        return getHibernateTemplate().execute(new HibernateCallback<List<OnmsFilter>>() {
            @Override
            public List<OnmsFilter> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from OnmsFilter f where f.username = :userName and f.page = :page");
                query.setParameter("userName", userName);
                query.setParameter("page", page);
                return query.list();
            }
        });
    }
}
