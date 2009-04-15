package org.opennms.web.notification.filter;

import org.hibernate.Hibernate;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.opennms.web.filter.OneArgFilter;
import org.opennms.web.filter.SQLType;



/** Encapsulates all user filtering functionality. */
public class UserFilter extends OneArgFilter<String> {
    public static final String TYPE = "user";

    public UserFilter(String user) {
        super(TYPE, SQLType.STRING, "NOTIFICATIONS.NOTIFYID", "notifyId", user);
    }
    
    
    @Override
    public String getSQLTemplate() {
        return " " + getSQLFieldName() + " in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=%s)";
    }


    @Override
    public Criterion getCriterion() {
        
            
        return Restrictions.sqlRestriction(" {alias}.notifyId in (SELECT DISTINCT usersnotified.notifyid FROM usersnotified WHERE usersnotified.userid=?)", getValue(), Hibernate.STRING);
    }


    @Override
    public String getTextDescription() {
        return getValue() + " was notified";
    }

    public String toString() {
        return ("<NoticeFactory.UserFilter: " + this.getDescription() + ">");
    }

    public String getUser() {
        return getValue();
    }

    public boolean equals(Object obj) {
        return (this.toString().equals(obj.toString()));
    }

}