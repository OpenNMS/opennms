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
package org.opennms.reporting.jasperreports.svclayer;

import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JRExpressionChunk;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.ExpressionChunkVisitor;
import net.sf.jasperreports.engine.util.JRExpressionUtil;

/**
 * Helper to convert a subreport {@link JRExpression} to an actual text representation.
 *
 * Please DO NOT USE this as a general conversion from {@link JRExpression} to text, as it only supports parameter and text chunks
 *
 * @author mvrueden
 */
class SubreportExpressionVisitor implements ExpressionChunkVisitor {

    private static final Logger LOG = LoggerFactory.getLogger(SubreportExpressionVisitor.class);

    private final JasperReport report;

    private final StringBuffer stringBuffer = new StringBuffer();

    SubreportExpressionVisitor(JasperReport report) {
        this.report = report;
    }

    @Override
    public void visitTextChunk(JRExpressionChunk chunk) {
        stringBuffer.append(chunk.getText());
    }

    @Override
    public void visitParameterChunk(JRExpressionChunk chunk) {
        Optional<JRParameter> parameterOptional = Arrays.stream(report.getParameters())
                .filter(p -> p.getName().equals(chunk.getText()))
                .findAny();
        if (parameterOptional.isPresent()) {
            String string = new SubreportExpressionVisitor(report).visit(parameterOptional.get().getDefaultValueExpression());
            append(string);
        }
    }
    @Override
    public void visitVariableChunk(JRExpressionChunk chunk) {
        LOG.warn("Variable chunks are not supported. Skipping.");
    }

    @Override
    public void visitResourceChunk(JRExpressionChunk chunk) {
        LOG.warn("Resource chunks are not supported. Skipping.");
    }

    @Override
    public void visitFieldChunk(JRExpressionChunk chunk) {
        LOG.warn("Field chunks are not supported. Skipping.");
    }

    protected String visit(JRExpression expression) {
        JRExpressionUtil.visitChunks(expression, this);
        final String string = stringBuffer.toString();
        return string;
    }

    private void append(String string) {
        if (string != null) {
            stringBuffer.append(string);
        }
    }
}
