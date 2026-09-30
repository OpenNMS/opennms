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

// Fired on window when a menu link points at the page already showing.
// Assigning the current URL is a no-op (the hash doesn't change, so neither the
// browser nor vue-router reacts), so the page would ignore the click; a page
// that should refresh on it listens for this event instead.
export const SAME_PAGE_NAVIGATION_EVENT = 'onms:same-page-navigation'

/** Go to `link`, or fire SAME_PAGE_NAVIGATION_EVENT if it is the current page. */
export const navigateTo = (link: string) => {
  if (new URL(link, window.location.href).href === window.location.href) {
    window.dispatchEvent(new CustomEvent(SAME_PAGE_NAVIGATION_EVENT))
    return
  }
  window.location.assign(link)
}
