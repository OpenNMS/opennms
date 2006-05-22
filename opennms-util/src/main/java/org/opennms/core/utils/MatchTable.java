/**
 * 
 */
package org.opennms.core.utils;

import java.util.regex.Matcher;


public class MatchTable implements PropertiesUtils.SymbolTable {
    
    private Matcher m_matcher;

    public MatchTable(Matcher m) {
        m_matcher = m;
    }

    public String getSymbolValue(String symbol) {
        try {
            int groupNum = Integer.parseInt(symbol);
            if (groupNum > m_matcher.groupCount())
                return null;
            return m_matcher.group(groupNum);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
}