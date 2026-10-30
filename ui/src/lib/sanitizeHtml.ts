///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///


import DOMPurify from 'dompurify'

// A module of its own, not lib/utils: that is imported by the menu bundle, which has no use for
// DOMPurify and should not carry it.

/**
 * An HTML fragment made safe for v-html: scripts, event handlers and javascript: URLs removed.
 * Use it for any HTML the server did not write itself -- an event's or alarm's log message can
 * carry text from a device (trap varbinds, syslog).
 */
export const sanitizeHtml = (html?: string | null): string => (html ? DOMPurify.sanitize(html) : '')
