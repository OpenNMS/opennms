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

/**
 * The body of a failed request when it is the server's own explanation: plain text, or a string under the
 * JSON type (axios leaves a body it cannot parse as a string). An HTML error page from Jetty or from a
 * proxy in front of it (a 500, 502 or 504, say) is not one, whatever its status: the fallback is used,
 * so the page's markup never ends up in a message.
 */
export const serverMessage = (err: any, fallback: string): string => {
  const detail = err?.response?.data
  if (typeof detail !== 'string') {
    return fallback
  }
  const text = detail.trim()
  const type = String(err?.response?.headers?.['content-type'] ?? '').toLowerCase()
  if (!text || text.startsWith('<') || (type && !type.startsWith('text/plain') && !type.startsWith('application/json'))) {
    return fallback
  }
  return text
}
