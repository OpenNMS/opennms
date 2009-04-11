package org.opennms.web.outage.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.opennms.netmgt.model.OnmsCriteria;

public class ConditionalFilter implements Filter {

    public static final String TYPE = "conditionalFilter";
    
    private Filter[] m_filters;
    private String m_conditionType;
    
    public ConditionalFilter(String conditionType, Filter... filters){
        m_filters = filters;
        m_conditionType = conditionType;
    }
    
    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        return bindFilterParams(ps, parameterIndex);
    }

    public String getDescription() {

        return TYPE + " = " + getFilterDescriptions();
    }

    public String getParamSql() {

        return getFilterParamSql();
    }

    public String getSql() {
        return getFilterSql();
    }

    public String getTextDescription() {
        return TYPE + " = " + getFilterTextDescriptions();
    }

    private String getFilterDescriptions() {
        String desc = " (";
        
        for(int i = 0; i < m_filters.length; i++){
              desc += m_filters[i].getDescription();
              if(i < m_filters.length - 1){
                  desc += " " + m_conditionType + " "; 
              }
        }
        desc += ") ";
        return desc;
    }
    
    private String getFilterParamSql() {
        String param = " (";
        
        for(int i = 0; i < m_filters.length; i++){
            param += " " + m_filters[i].getParamSql();
            if(i < m_filters.length - 1){
                param += " " + m_conditionType;
            }
        }
        param += ") ";
        return param;
    }
    
    private String getFilterSql(){
        String sql = " (";
        
        for(int i = 0; i < m_filters.length; i++){
            sql += " " + m_filters[i].getSql();
            if(i < m_filters.length - 1){
                sql += " " + m_conditionType;
            }
        }
        sql += ") ";
        return sql;
    }

    private String getFilterTextDescriptions() {
        String tDescriptions = " (";
        
        for(int i = 0; i < m_filters.length; i++){
            tDescriptions += " " + m_filters[i].getTextDescription();
            if(i < m_filters.length - 1){
                tDescriptions += " " + m_conditionType;
            }
        }
        tDescriptions += ") ";
        return tDescriptions;
    }
    
    private int bindFilterParams(PreparedStatement ps, int parameterIndex) throws SQLException {
        int retVal = 0;
        for (Filter mFilter : m_filters) {
            retVal += mFilter.bindParam(ps, parameterIndex + retVal);
        }
        
        return retVal;
    }

    public void applyCriteria(OnmsCriteria criteria) {
        // TODO Auto-generated method stub
        
    }

}
