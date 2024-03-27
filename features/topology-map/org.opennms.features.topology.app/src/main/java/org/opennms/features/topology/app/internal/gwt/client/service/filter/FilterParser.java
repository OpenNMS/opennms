/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.app.internal.gwt.client.service.filter;

import java.util.LinkedList;
import java.util.List;

import org.opennms.features.topology.app.internal.gwt.client.service.Filter;

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
