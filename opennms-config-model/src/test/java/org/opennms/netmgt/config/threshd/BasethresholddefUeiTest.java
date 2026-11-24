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
package org.opennms.netmgt.config.threshd;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;

/**
 * A threshold's custom UEIs never carry surrounding whitespace, however the threshold was built, and a UEI that is
 * blank after the trim means "no custom UEI", so the threshold type's default UEI applies.
 */
public class BasethresholddefUeiTest {

    private static final String NS = "http://xmlns.opennms.org/xsd/config/thresholding";
    private static final String TRIGGER_UEI = "uei.opennms.org/t";
    private static final String REARM_UEI = "uei.opennms.org/r";

    @Test
    public void settersTrimPaddedUeis() {
        final Threshold threshold = new Threshold();
        threshold.setTriggeredUEI(" \t" + TRIGGER_UEI + "\n ");
        threshold.setRearmedUEI("  " + REARM_UEI + "\r\n");

        assertEquals(Optional.of(TRIGGER_UEI), threshold.getTriggeredUEI());
        assertEquals(Optional.of(REARM_UEI), threshold.getRearmedUEI());
    }

    @Test
    public void settersTreatBlankUeisAsAbsent() {
        for (final String blank : new String[] {"   ", "\t\n", "\u0001", " ", " 　 "}) {
            final Threshold threshold = new Threshold();
            threshold.setTriggeredUEI(blank);
            threshold.setRearmedUEI(blank);

            final String which = "value " + blank.codePoints().mapToObj(c -> String.format("U+%04X", c)).toList();
            assertEquals(which, Optional.empty(), threshold.getTriggeredUEI());
            assertEquals(which, Optional.empty(), threshold.getRearmedUEI());
        }
    }

    @Test
    public void parsedPaddedUeisAreReadTrimmed() {
        final Threshold threshold = parse(" " + TRIGGER_UEI + " ", " " + REARM_UEI + " ");

        assertEquals(Optional.of(TRIGGER_UEI), threshold.getTriggeredUEI());
        assertEquals(Optional.of(REARM_UEI), threshold.getRearmedUEI());
    }

    @Test
    public void parsedWhitespaceOnlyUeisAreAbsent() {
        final Threshold threshold = parse("   ", "  ");

        assertEquals(Optional.empty(), threshold.getTriggeredUEI());
        assertEquals(Optional.empty(), threshold.getRearmedUEI());
    }

    @Test
    public void paddedAndTrimmedThresholdsAreEqual() {
        final Threshold padded = parse(" " + TRIGGER_UEI + " ", " " + REARM_UEI + " ");
        final Threshold trimmed = parse(TRIGGER_UEI, REARM_UEI);

        assertEquals(trimmed, padded);
        assertEquals(trimmed.hashCode(), padded.hashCode());
    }

    /** Parses through JAXB, which fills the fields directly and never calls the setters. */
    private static Threshold parse(final String triggeredUei, final String rearmedUei) {
        return JaxbUtils.unmarshal(Threshold.class, "<threshold xmlns=\"" + NS + "\" type=\"high\" ds-type=\"node\""
                + " value=\"1\" rearm=\"0\" trigger=\"1\" ds-name=\"x\""
                + " triggeredUEI=\"" + triggeredUei + "\" rearmedUEI=\"" + rearmedUei + "\"/>");
    }
}
