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
package org.hawkular.agent.prometheus.text;

import java.util.LinkedHashMap;
import java.util.Map;

import org.hawkular.agent.prometheus.Util;

class LineParser {
    
    private enum ParseState {
        NAME,
        ENDOFNAME,
        STARTOFLABELNAME,
        LABELNAME,
        LABELVALUEEQUALS,
        LABELVALUEQUOTE,
        LABELVALUE,
        LABELVALUESLASH,
        NEXTLABEL,
        ENDOFLABELS,
        VALUE,
        TIMESTAMP
    }
    
    private final StringBuilder name = new StringBuilder();
    private final StringBuilder labelname = new StringBuilder();
    private final StringBuilder labelvalue = new StringBuilder();
    private final StringBuilder value = new StringBuilder();
    private final StringBuilder timestamp = new StringBuilder();
    private final Map<String, String> labels = new LinkedHashMap<>();
    private ParseState state;

    public void reset() {
        name.setLength(0);
        labelname.setLength(0);
        labelvalue.setLength(0);
        value.setLength(0);
        timestamp.setLength(0);
        labels.clear();
        state = ParseState.NAME;
    }

    public TextSample parse(String line) {
        reset();
        
        for (int c = 0; c < line.length(); c++) {
            char charAt = line.charAt(c);
            
            switch (state) {
                case NAME:
                    if (charAt == '{') {
                        state = ParseState.STARTOFLABELNAME;
                    } else if (charAt == ' ' || charAt == '\t') {
                        state = ParseState.ENDOFNAME;
                    } else {
                        name.append(charAt);
                    }
                    break;
                case ENDOFNAME:
                    if (charAt == ' ' || charAt == '\t') {
                        // do nothing
                    } else if (charAt == '{') {
                        state = ParseState.STARTOFLABELNAME;
                    } else {
                        value.append(charAt);
                        state = ParseState.VALUE;
                    }
                    break;
                case STARTOFLABELNAME:
                    if (charAt == ' ' || charAt == '\t') {
                        // do nothing
                    } else if (charAt == '}') {
                        state = ParseState.ENDOFLABELS;
                    } else {
                        labelname.append(charAt);
                        state = ParseState.LABELNAME;
                    }
                    break;
                case LABELNAME:
                    if (charAt == '=') {
                        state = ParseState.LABELVALUEQUOTE;
                    } else if (charAt == '}') {
                        state = ParseState.ENDOFLABELS;
                    } else if (charAt == ' ' || charAt == '\t') {
                        state = ParseState.LABELVALUEEQUALS;
                    } else {
                        labelname.append(charAt);
                    }
                    break;
                case LABELVALUEEQUALS:
                    if (charAt == '=') {
                        state = ParseState.LABELVALUEQUOTE;
                    } else if (charAt == ' ' || charAt == '\t') {
                        // do nothing
                    } else {
                        throw new IllegalStateException("Invalid line: " + line);
                    }
                    break;
                case LABELVALUEQUOTE:
                    if (charAt == '"') {
                        state = ParseState.LABELVALUE;
                    } else if (charAt == ' ' || charAt == '\t') {
                        // do nothing
                    } else {
                        throw new IllegalStateException("Invalid line: " + line);
                    }
                    break;
                case LABELVALUE:
                    if (charAt == '\\') {
                        state = ParseState.LABELVALUESLASH;
                    } else if (charAt == '"') {
                        labels.put(labelname.toString(), labelvalue.toString());
                        labelname.setLength(0);
                        labelvalue.setLength(0);
                        state = ParseState.NEXTLABEL;
                    } else {
                        labelvalue.append(charAt);
                    }
                    break;
                case LABELVALUESLASH:
                    state = ParseState.LABELVALUE;
                    if (charAt == '\\') {
                        labelvalue.append('\\');
                    } else if (charAt == 'n') {
                        labelvalue.append('\n');
                    } else if (charAt == '"') {
                        labelvalue.append('"');
                    } else {
                        labelvalue.append('\\').append(charAt);
                    }
                    break;
                case NEXTLABEL:
                    if (charAt == ',') {
                        state = ParseState.LABELNAME;
                    } else if (charAt == '}') {
                        state = ParseState.ENDOFLABELS;
                    } else if (charAt == ' ' || charAt == '\t') {
                        // do nothing
                    } else {
                        throw new IllegalStateException("Invalid line: " + line);
                    }
                    break;
                case ENDOFLABELS:
                    if (charAt == ' ' || charAt == '\t') {
                        // do nothing
                    } else {
                        value.append(charAt);
                        state = ParseState.VALUE;
                    }
                    break;
                case VALUE:
                    if (charAt == ' ' || charAt == '\t') {
                        state = ParseState.TIMESTAMP;                       
                    } else {
                        value.append(charAt);
                    }
                    break;
                case TIMESTAMP:
                    if (charAt == ' ' || charAt == '\t') {
                        return buildSample(line);
                    }
                    else {
                        timestamp.append(charAt);
                    }
                    break;
            }
        }

        return buildSample(line);
    }

    private TextSample buildSample(String line) {
        return new TextSample.Builder()
                .setLine(line)
                .setName(name.toString())
                .setValue(value.toString())
                .setTimestamp(Util.tryConvertTimestamp(timestamp))
                .addLabels(new LinkedHashMap<>(labels))
                .build();
    }
    
}