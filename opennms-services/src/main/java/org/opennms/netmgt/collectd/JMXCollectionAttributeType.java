package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.StringTokenizer;

import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.Persister;

class JMXCollectionAttributeType implements CollectionAttributeType {
    JMXDataSource m_dataSource;
    AttributeGroupType m_groupType;
    String m_name;

    protected JMXCollectionAttributeType(JMXDataSource dataSource, String key, String substitutions,  AttributeGroupType groupType) {
        m_groupType=groupType;
        m_dataSource=dataSource;
        m_name=createName(key,substitutions);
    }

    private String createName(String key, String substitutions) {
        String name=m_dataSource.getName();
        if(key!=null && !key.equals("")) {
            name=fixKey(key, m_dataSource.getName(),substitutions)+"_"+name;
        }
        return name;
    }

    @Override
    public AttributeGroupType getGroupType() {
        return m_groupType;
    }

    @Override
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        //Only numeric data comes back from JMX in data collection
        persister.persistNumericAttribute(attribute);
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public String getType() {
        return m_dataSource.getType();
    }

    /*
     * This method strips out the illegal character '/' and attempts to keep
     * the length of the key plus ds name to 19 or less characters. The slash
     * character cannot be in the name since it is an illegal character in
     * file names.
     */
    private String fixKey(String key, String attrName, String substitutions) {
        String newKey = key;
        if (key.startsWith(File.separator)) {
            newKey = key.substring(1);
        }
        if (substitutions != null && substitutions.length() > 0) {
            StringTokenizer st = new StringTokenizer(substitutions, ",");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int index = token.indexOf('|');
                if (newKey.equals(token.substring(0, index))) {
                    newKey = token.substring(index + 1);
                }
            }
        }
        return newKey;
    }
}