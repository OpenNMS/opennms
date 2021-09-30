/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnPathAnalysis;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpAsnPathAnalysisDao;

public class AsnPathTest {


    @Test
    public void testAsnPath() {

        BmpMessagePersister bmpMessagePersister = new BmpMessagePersister();
        BmpAsnPathAnalysisDao bmpAsnPathAnalysisDao = Mockito.mock(BmpAsnPathAnalysisDao.class);
        // Mock that no data returned from Dao
        Mockito.when(bmpAsnPathAnalysisDao.findByAsnPath(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyBoolean())).thenReturn(null);
        bmpMessagePersister.setBmpAsnPathAnalysisDao(bmpAsnPathAnalysisDao);

        List<BmpAsnPathAnalysis> bmpAsnPathAnalysisList = bmpMessagePersister.buildBmpAsnPath("8319 33891 1299 2914 2907 2907 ");
        assertThat(bmpAsnPathAnalysisList, Matchers.hasSize(5));

        BmpAsnPathAnalysis bmpAsnPathAnalysis = bmpAsnPathAnalysisList.get(0);
        assertEquals(8319L, bmpAsnPathAnalysis.getAsn().longValue());
        assertEquals(0L, bmpAsnPathAnalysis.getAsnLeft().longValue());
        assertEquals(33891L, bmpAsnPathAnalysis.getAsnRight().longValue());
        assertEquals(true, bmpAsnPathAnalysis.isAsnLeftPeering());

        bmpAsnPathAnalysis = bmpAsnPathAnalysisList.get(2);
        assertEquals(1299L, bmpAsnPathAnalysis.getAsn().longValue());
        assertEquals(33891L, bmpAsnPathAnalysis.getAsnLeft().longValue());
        assertEquals(2914L, bmpAsnPathAnalysis.getAsnRight().longValue());
        assertEquals(false, bmpAsnPathAnalysis.isAsnLeftPeering());

        bmpAsnPathAnalysis = bmpAsnPathAnalysisList.get(4);
        assertEquals(2907L, bmpAsnPathAnalysis.getAsn().longValue());
        assertEquals(2914L, bmpAsnPathAnalysis.getAsnLeft().longValue());
        assertEquals(0L, bmpAsnPathAnalysis.getAsnRight().longValue());
        assertEquals(false, bmpAsnPathAnalysis.isAsnLeftPeering());
        // Check Invalid path
        bmpAsnPathAnalysisList = bmpMessagePersister.buildBmpAsnPath("8319 33891A 1299 2914 2907 2907 ");
        assertThat(bmpAsnPathAnalysisList, Matchers.empty());
    }


    @Test
    public void testStringArrayToLongArray() {
        String[] asnPaths = {"8319", "ABC", "1299"};
        long[] asnArray = BmpMessagePersister.getLongArrayFromStringArray(asnPaths);
        assertTrue(asnArray.length == 0);
        String[] asnPaths1 = {"8319", "33891", "1299", "2914"};
        long[] asnArray1 = BmpMessagePersister.getLongArrayFromStringArray(asnPaths1);
        assertEquals(4L, asnArray1.length);
        long[] expected = {8319, 33891, 1299, 2914};
        assertArrayEquals(expected, asnArray1);
    }
}
