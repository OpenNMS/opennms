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
package org.opennms.web.rest.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class UserMgmtValidationTest {

    private static void assertRejected(final String message, final List<String> requested, final List<String> stored) {
        try {
            UserMgmtValidation.validateDutySchedules("User \"u\"", requested, stored);
            fail("expected a rejection");
        } catch (final UserMgmtValidationException e) {
            assertEquals(message, e.getMessage());
        }
    }

    @Test
    public void testNameRejectsWhitespaceAndInvisibleCharacters() {
        assertNull(UserMgmtValidation.validateName("jane.doe@example.org", "userId"));
        assertNull(UserMgmtValidation.validateName("Müller", "userId"));
        for (final String bad : List.of("a b", "Ops\u00A0", "Ops\u2003", "a\u200Bb", "a\u0000b", "a\u2028b", "a<b", "a:b")) {
            assertNotNull("'" + bad + "' should be rejected", UserMgmtValidation.validateName(bad, "group name"));
        }
    }

    @Test
    public void testNullAndBlankDutySchedulesAreRejected() {
        assertRejected("Duty schedules must not be blank.", Arrays.asList((String) null), null);
        assertRejected("Duty schedules must not be blank.", Arrays.asList("MoWeFr800-1700", " "), null);
        assertRejected("Duty schedules must not be blank.", Arrays.asList((String) null), Arrays.asList("MoTu2000-800"));
    }

    @Test
    public void testStoredDutySchedulesAreKeptAndNewOnesValidated() {
        UserMgmtValidation.validateDutySchedules("u", Arrays.asList("MoTu2000-800", "MoWeFr800-1700"), Arrays.asList("MoTu2000-800"));
        try {
            UserMgmtValidation.validateDutySchedules("u", Arrays.asList("MoTu2000-800"), null);
            fail("an overnight schedule is new here");
        } catch (final UserMgmtValidationException expected) {
            // fine
        }
    }

    @Test(expected = UserMgmtValidationException.class)
    public void testTooLongDutyScheduleIsRejected() {
        UserMgmtValidation.checkLength("u", "duty schedule", "x".repeat(300), 255);
    }

    private static void assertFreeTextRejected(final String incoming, final String stored) {
        try {
            UserMgmtValidation.checkFreeText("Group \"g\"", "comment", incoming, stored);
            fail("expected a rejection");
        } catch (final UserMgmtValidationException e) {
            assertEquals("Group \"g\": the comment is longer than 4096 characters.", e.getMessage());
        }
    }

    @Test
    public void testFreeTextLimit() {
        assertEquals(4096, UserMgmtValidation.FREE_TEXT_LENGTH);
        UserMgmtValidation.checkFreeText("Group \"g\"", "comment", null, null);
        UserMgmtValidation.checkFreeText("Group \"g\"", "comment", "x".repeat(4096), null);
        // counted as stored: trimmed, and in code points
        UserMgmtValidation.checkFreeText("Group \"g\"", "comment", " " + "x".repeat(4096) + "\n", null);
        UserMgmtValidation.checkFreeText("Group \"g\"", "comment", "\uD83D\uDE00".repeat(4096), null);
        assertFreeTextRejected("x".repeat(4097), null);
        assertFreeTextRejected("x".repeat(4097), "x".repeat(4096));
        assertFreeTextRejected("\uD83D\uDE00".repeat(4097), null);
    }

    @Test
    public void testStoredLongFreeTextIsGrandfathered() {
        final String stored = "y".repeat(5000);
        UserMgmtValidation.checkFreeText("Group \"g\"", "comment", stored, stored);
        UserMgmtValidation.checkFreeText("Group \"g\"", "comment", " " + stored + "\n", stored);
        assertFreeTextRejected(stored + "z", stored);
        // on a create there is nothing stored to grandfather
        assertFreeTextRejected(stored, null);
    }

    private static void assertCommentsRejected(final String expectedMessage, final String incoming, final String stored) {
        try {
            UserMgmtValidation.checkComments("Group \"g\"", incoming, stored);
            fail("expected a rejection");
        } catch (final UserMgmtValidationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void testCommentsCheckMarkupAndLength() {
        final String markup = "The comments must not contain any HTML markup.";
        UserMgmtValidation.checkComments("Group \"g\"", null, null);
        UserMgmtValidation.checkComments("Group \"g\"", "plain text", null);
        assertCommentsRejected(markup, "<b>x</b>", null);
        assertCommentsRejected(markup, "<b>x</b>", "x");
        assertCommentsRejected("Group \"g\": the comment is longer than 4096 characters.", "x".repeat(4097), null);
        // stored markup and stored long comments are grandfathered
        UserMgmtValidation.checkComments("Group \"g\"", " <b>x</b>\n", "<b>x</b>");
        UserMgmtValidation.checkComments("Group \"g\"", "y".repeat(5000), "y".repeat(5000));
        assertCommentsRejected(markup, "<b>x</b> changed", "<b>x</b>");
    }

    @Test
    public void testTextUnchangedIgnoresSurroundingWhitespace() {
        assertTrue(UserMgmtValidation.textUnchanged(" a\n", "a"));
        assertTrue(UserMgmtValidation.textUnchanged("", null));
        assertFalse(UserMgmtValidation.textUnchanged("a b", "ab"));
    }

    @Test
    public void testReplaceIfChanged() {
        final List<String> current = new ArrayList<>(Arrays.asList("a", "b"));
        UserMgmtValidation.replaceIfChanged(current, null);
        assertEquals(Arrays.asList("a", "b"), current);
        final List<String> same = Arrays.asList("a", "b");
        UserMgmtValidation.replaceIfChanged(current, same);
        assertEquals(Arrays.asList("a", "b"), current);
        UserMgmtValidation.replaceIfChanged(current, Arrays.asList("b", "c"));
        assertEquals(Arrays.asList("b", "c"), current);
        // replacing with the list itself must not empty it
        UserMgmtValidation.replaceIfChanged(current, current);
        assertEquals(Arrays.asList("b", "c"), current);
    }
}
