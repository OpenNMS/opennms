/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.soa.filter;

import java.util.LinkedList;
import java.util.List;

import org.opennms.core.soa.Filter;

/**
 * FilterUtils
 *
 * @author brozow
 */
public class FilterParser {
    
    private class Lexer {
        private String m_input;
        private int m_ptr;
        private String m_peekedToken;
        
        Lexer(String input) {
            m_input = input;
            m_ptr = 0;
            m_peekedToken = null;
        }
        
        Character nextChar() {
            if (m_ptr >= m_input.length()) {
                return null;
            }
            
            return m_input.charAt(m_ptr++);
            
        }
        
        Character peekChar() {
            if (m_ptr >= m_input.length()) {
                return null;
            }
            
            return m_input.charAt(m_ptr);
        }
        
        /*
         * TOKENS:
         * '('
         * ')'
         * '&'
         * '|'
         * '!'
         * '='
         * '*'
         * '>='
         * '<='
         * text == '[^()&|!=<>*]|\[()&|!=<>*\]'
         * 
         */
        
        boolean isTokenStart(Character ch) {
            if (ch == null) {
                return true;
            }
            switch(ch) {
            case '(':
            case ')':
            case '&':
            case '|':
            case '!':
            case '=':
            case '*':
            case '>':
            case '<':
                return true;
            default:
                return false;
            }
            
        }

        String readText() {
            final StringBuilder bldr = new StringBuilder();
            Character ch = peekChar();
            while(!isTokenStart(ch)) {
                if (ch == '\\') {
                    // skip backslash
                    nextChar();
                    // read next char and append it
                    ch = nextChar();
                    if (ch == null) {
                        parseError("End of input reached after '\\'");
                    }
                }
                bldr.append(nextChar());
                ch = peekChar();
            }
            return bldr.toString();
        }
        
        String peekToken() {
            if (m_peekedToken == null) {
                m_peekedToken = nextToken();
            }
            return m_peekedToken;
        }

        String nextToken() {
            // return a peeked token first
            if (m_peekedToken != null) {
                String token = m_peekedToken;
                m_peekedToken = null;
                return token;
            }

            Character ch = nextChar();
            if (ch == null) {
                return null;
            }

            switch(ch) {
            case '(':
            case ')':
            case '&':
            case '|':
            case '!':
            case '=':
            case '*':
                return ch.toString();
            case '>':
            case '<':
                Character eq = nextChar();
                if ( eq == null || '=' != eq ) {
                    parseError("Expected '=' following '" + ch + "'. Note strict '>' and '<' not supported");
                    return null;
                }
                return String.valueOf(new char[] { ch, eq });
            default:
                final StringBuilder bldr = new StringBuilder();
                bldr.append(ch);
                bldr.append(readText());
                return bldr.toString();
            }
            
        }
        
        String charsTil(char token) {
            if (m_peekedToken != null) {
                throw new IllegalStateException("Cannot compute charTil while a peeked token exists.");
            }
            
            final StringBuilder buf = new StringBuilder();
            boolean escaped = false;
            
            Character ch = peekChar();
            while (ch != null && (ch != token || escaped) ) {
                buf.append(nextChar()); // use next char to move ptr forward
                escaped = ch == '\\' ? !escaped : false;
                ch = peekChar();
            } 

            return buf.toString();

        }
        
        
    }

    
    private Lexer m_lexer;
    public FilterParser() {
        
    }
    
    
    public Filter parse(String filterString) {
        m_lexer = new Lexer(filterString);
        return filter();
    }
    
    private Filter filter() {
        skipWhitespace();
        match("(");
        Filter filter = filterComp();
        skipWhitespace();
        match(")");
        return filter;
    }
    
    private Filter filterComp() {
        skipWhitespace();
        String token = m_lexer.peekToken();
        if ("&".equals(token)) {
            return and();
        } else if ("|".equals(token)) {
            return or();
        } else if ("!".equals(token)) {
            return not();
        } else {
            return operation();
        }
    }
    
    private Filter and() {
        match("&");
        List<Filter> filters = filterList();
        return new AndFilter(filters);
    }
    private Filter or() {
        match("|");
        List<Filter> filters = filterList();
        return new OrFilter(filters);
    }
    private Filter not() {
        match("!");
        Filter filter = filter();
        return new NotFilter(filter);
    }
    private LinkedList<Filter> filterList() {
        LinkedList<Filter> filters;
        Filter filter = filter();
        skipWhitespace();
        String token = m_lexer.peekToken();
        if ("(".equals(token)) {
            filters = filterList();
        } else {
            filters = new LinkedList<>();
        }
        filters.addFirst(filter);
        return filters;
    }

    private Filter operation() {
        
        String attribute = matchAttribute();
        
        skipWhitespace();
        String operation = m_lexer.peekToken();
        if (">=".equals(operation)) {
            return greaterThan(attribute);
        } else if ("<=".equals(operation)) {
            return lessThan(attribute);
        } else if ("=".equals(operation)) {
            return eq(attribute);
        } else {
            parseError("Unsupported operation " + operation);
            return null;
        }
    }


    private Filter eq(String attribute) {
        match("=");
        
        
        String value = m_lexer.charsTil(')');
        
        // a presence filter
        if ("*".equals(value.trim())) {
            return new PresenceFilter(attribute);
        }
        
        // a simple equals filter
        if (!value.replace("\\*", "").contains("*")) {
            return new EqFilter(attribute, value.replaceAll("\\\\(.)", "$1"));
        }
        
        return new PatternMatchingFilter(attribute, value);
        
    }


    private void assertNotEnd(String token, String msg) {
        if (token == null) {
            parseError("Unexpected end of input. " + msg);
        }
    }
    
    private Filter lessThan(String attribute) {
        match("<=");
        
        String value = m_lexer.nextToken();
        
        assertNotEnd(value, "Expected a value following <=");
        
        return new LessThanFilter(attribute, value);
    }
    
    


    private Filter greaterThan(String attribute) {
        match(">=");
        
        String value = m_lexer.nextToken();

        assertNotEnd(value, "Expected a value following >=");
        
        return new GreaterThanFilter(attribute, value);
    }


    private String matchAttribute() {
        String token = m_lexer.nextToken();
        assertNotEnd(token, "Expected an attribute name.");
        String attr = token.trim();
        ensureAttrDoesNotContain(attr, "()&|!*=<>~");
        return attr;
    }
    
    private void ensureAttrDoesNotContain(String attr, String invalidChars) {
        for(int i = 0; i < invalidChars.length(); i++) {
            char ch = invalidChars.charAt(i);
            if (attr.contains(String.valueOf(ch))) {
                parseError("Attributes may not contain the '" + ch + "' characters");
            }
        }
    }
    
    private String match(String expected) {
        String actual = m_lexer.nextToken();
        assertNotEnd(actual, "Expected " + expected);
        if (!expected.equals(actual)) {
            parseError("Unexpected token " + actual + ".  Expected " + expected);
            return null;
        }
        return actual;
    }


    private void skipWhitespace() {
        String token = m_lexer.peekToken();
        if (token != null && "".equals(token.trim())) {
            m_lexer.nextToken(); // skip whitespace token
            token = m_lexer.peekToken();
        }
    }
    

    void parseError(String msg) {
        throw new IllegalArgumentException(msg);
    }
    

    
}
