package org.opennms.web.notification.filter;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.opennms.web.filter.OneArgFilter;
import org.opennms.web.filter.SQLType;



/**
 * Encapsulates all user filtering functionality.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class UserFilter extends OneArgFilter<String> {
    /** Constant <code>TYPE="user"</code> */
    public static final String TYPE = "user";

    /**
     * <p>Constructor for UserFilter.</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public UserFilter(String user) {
        super(TYPE, SQLType.STRING, "NOTIFICATIONS.NOTIFYID", "notifyId", user);
    }
    
    
    /** {@inheritDoc} */
    @Override
    public String getSQLTemplate() {
        return " " + getSQLFieldName() + " in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=%s)";
    }


    /** {@inheritDoc} */
    @Override
    public Criterion getCriterion() {
        
            
        return Restrictions.sqlRestriction(" {alias}.notifyId in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)", getValue(), Hibernate.STRING);
    }


    /** {@inheritDoc} */
    @Override
    public String getTextDescription() {
        return getValue() + " was notified";
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return ("<NoticeFactory.UserFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return getValue();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }

}
