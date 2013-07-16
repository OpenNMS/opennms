/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.tools.spectrum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

public class EventTableReader {
	
    private static final Logger LOG = LoggerFactory.getLogger(EventTableReader.class);

    private Resource m_resource;
    private Reader m_reader;
    private StreamTokenizer m_tokenizer;
    
    private static final String keyExpr = "^(0[Xx][0-9A-Fa-f]+)|([0-9]+)$";
    
    /**
     * 0x00000001 monitor
     * 0x00000002 manager
     * 0x00000003 sip
     * 0x00000004 media
     * 0x00000005 auth
     * 0x00000006 reg
     * 0x00000007 h323
     * 0x00000008 dir
     * 0x00000009 web
     * 0x0000000a ws
     * 0x0000000b acct
     * 0x0000000c dos
     * 0x0000000d mxp
     * 0x0000000e ssh
     * 0x0000000f asterisk
     * 0x000000010 av
     * 
     */
    
    public EventTableReader(Resource rsrc) throws IOException {
        m_resource = rsrc;
        m_reader = new BufferedReader(new InputStreamReader(m_resource.getInputStream()));
        m_tokenizer = new StreamTokenizer(m_reader);
        m_tokenizer.resetSyntax();
        m_tokenizer.commentChar('#');
        m_tokenizer.eolIsSignificant(true);
        m_tokenizer.whitespaceChars('\n', '\n');
        m_tokenizer.whitespaceChars('\r', '\r');
        m_tokenizer.whitespaceChars(' ', ' ');
        m_tokenizer.whitespaceChars('\t', '\t');
        m_tokenizer.wordChars('0', '9');
        m_tokenizer.wordChars('a', 'z');
        m_tokenizer.wordChars('A', 'Z');
    }
    
    public EventTable getEventTable() throws IOException {
        String tableName = m_resource.getFilename();
        
        EventTable eventTable = new EventTable(tableName);
        String thisKey = null;
        StringBuilder thisValueBuilder = null;
        
        boolean justHitEol = true;
        boolean gotKey = false;
        boolean gotValue = false;
        
        while (m_tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
            if (justHitEol && m_tokenizer.ttype == StreamTokenizer.TT_WORD && m_tokenizer.sval.matches(keyExpr)) {
                LOG.trace("Found a key [{}] on line {}, creating a new event-table entry", m_tokenizer.sval, m_tokenizer.lineno());
                thisKey = m_tokenizer.sval;
                justHitEol = false;
                gotKey = true;
                gotValue = false;
                m_tokenizer.nextToken();
            }

            if (m_tokenizer.ttype == StreamTokenizer.TT_EOL) {
                LOG.trace("Hit EOL on line {}", m_tokenizer.lineno());
                if (gotKey) {
                    LOG.trace("At EOL for key [{}]", thisKey);
                }
                if (! gotValue) {
                    LOG.warn("No value for key [{}] in table [{}] read from [{}]; setting it to literal string [null]", thisKey, tableName, m_resource);
                    thisValueBuilder = new StringBuilder("[null]");
                }
                LOG.trace("Setting key [{}] to value [{}]", thisKey, thisValueBuilder.toString());
                eventTable.put(thisKey, thisValueBuilder.toString());
                justHitEol = true;
                gotKey = false;
                gotValue = false;
            }
            
            if (gotKey && m_tokenizer.ttype == StreamTokenizer.TT_WORD) {
                if (!gotValue) {
                    LOG.trace("Found first post-key token [{}] on line {}; initializing string builder with it", m_tokenizer.sval, m_tokenizer.lineno());
                    thisValueBuilder = new StringBuilder(m_tokenizer.sval);
                    gotValue = true;
                } else {
                    LOG.trace("Found subsequent value token [{}] on line {}; appending it to the string builder", m_tokenizer.sval, m_tokenizer.lineno());
                    thisValueBuilder.append(" ").append(m_tokenizer.sval);
                }
            }
        }
        
        LOG.debug("Loaded {} entries for table [{}] from [{}]", eventTable.keySet().size(), tableName, m_resource);
        return eventTable;
    }
    
}
