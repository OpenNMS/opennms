package org.opennms.netmgt.dao.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.opennms.netmgt.dao.api.FilterFavoriteDao;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.sql.SQLException;
import java.util.List;

// TODO MVR move to org.opennms.netmgt.dao.hibernate package
public class FilterFavoriteDaoHibernate extends AbstractDaoHibernate<OnmsFilterFavorite, Integer> implements FilterFavoriteDao {

    public FilterFavoriteDaoHibernate() {
        super(OnmsFilterFavorite.class);
    }

    @Override
    public void delete(OnmsFilterFavorite entity) throws org.springframework.dao.DataAccessException {
        // TODO MVR remove setCheckWriteOperations(false) operation and
        // do it the right way -> Ask Matt what the right way is.
        // Without this a "InvalidDataAccessException is thrown"
        getHibernateTemplate().setCheckWriteOperations(false);
        super.delete(entity);
    }
    
    @Override
    public void save(OnmsFilterFavorite entity) throws org.springframework.dao.DataAccessException {
    	// TODO MVR remove setCheckWriteOperations(false) operation and
    	// do it the right way -> Ask Matt what the right way is.
    	// Without this a "InvalidDataAccessException is thrown"
    	getHibernateTemplate().setCheckWriteOperations(false);  
    	super.save(entity);
    }

    @Override
    public OnmsFilterFavorite findBy(final String userName, final String filterName) {
        return getHibernateTemplate().execute(new HibernateCallback<OnmsFilterFavorite>() {
            @Override
            public OnmsFilterFavorite doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from OnmsFilterFavorite f where f.username = :userName and f.filterName = :filterName");
                query.setParameter("filterName", filterName);
                query.setParameter("userName", userName);
                Object result = query.uniqueResult();
                if (result == null) return null;
                return (OnmsFilterFavorite)result;
            }
        });
    }

    @Override
    public List<OnmsFilterFavorite> findBy(final String userName, final OnmsFilterFavorite.Page page) {
        return getHibernateTemplate().execute(new HibernateCallback<List<OnmsFilterFavorite>>() {
            @Override
            public List<OnmsFilterFavorite> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from OnmsFilterFavorite f where f.username = :userName and f.page = :page");
                query.setParameter("userName", userName);
                query.setParameter("page", page);
                return query.list();
            }
        });
    }


    @Override
    public boolean existsFilter(final String userName, final String filterName, final OnmsFilterFavorite.Page page) {
        List<OnmsFilterFavorite> favorites = getHibernateTemplate().execute(new HibernateCallback<List<OnmsFilterFavorite>>() {
            @Override
            public List<OnmsFilterFavorite> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery("from OnmsFilterFavorite f where f.username = :userName and f.page = :page and f.name = :filterName");
                query.setParameter("userName", userName);
                query.setParameter("page", page);
                query.setParameter("filterName", filterName);
                return query.list();
            }
        });
        return !favorites.isEmpty();
    }
}
