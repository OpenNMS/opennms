/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.grafana;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Ignore;
import org.junit.Test;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.design.JRDesignDataset;
import net.sf.jasperreports.engine.design.JRDesignField;

// TODO MVR implement me
@Ignore("Requires a configured instance")
public class GrafanaPanelDatasourceTest {

    @Test
    public void canUseIt() throws JRException {
        JasperReportsContext context = DefaultJasperReportsContext.getInstance();
        JRDataset dataset = new JRDesignDataset(false);

        GrafanaQueryExecutor grafanaQueryExecutor = new GrafanaQueryExecutor(context, dataset, Collections.emptyMap());
        GrafanaPanelDatasource ds = grafanaQueryExecutor.createDatasource();

        JRDesignField field = new JRDesignField();
        field.setName(GrafanaPanelDatasource.IMAGE_FIELD_NAME);

        int graphsRendered = 0;
        while(ds.next()) {
            byte[] pngBytes = (byte[])ds.getFieldValue(field);
            assertThat(pngBytes.length, greaterThan(1));
            graphsRendered++;
        }

        assertThat(graphsRendered, greaterThanOrEqualTo(1));
    }

}