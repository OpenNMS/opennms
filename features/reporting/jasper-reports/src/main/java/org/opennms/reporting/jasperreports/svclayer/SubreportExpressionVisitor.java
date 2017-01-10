/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
