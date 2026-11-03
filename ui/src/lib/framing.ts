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
 * True when the page runs inside a frame/iframe. Treats a cross-origin
 * SecurityError on `top` as framed.
 */
export const isFramed = (win: Window = window): boolean => {
  try {
    return win.self !== win.top
  } catch {
    return true
  }
}

/**
 * An embedder keeps the menu in a frame by putting this in the frame's name,
 * e.g. `<iframe name="noc-with-header" src="...">`. The name stays with the
 * frame as it navigates. Same marker as the legacy navbar's.
 */
export const MENU_IN_FRAME_MARKER = '-with-header'

/**
 * True when the page is framed and the frame did not ask for the menu
 * (see MENU_IN_FRAME_MARKER): the host page supplies the chrome, so OpenNMS
 * shows no menu. Mirrored by the pre-paint script in
 * opennms-webapp/src/main/webapp/includes/bootstrap.jsp.
 */
export const hidesMenuInFrame = (win: Window = window): boolean => {
  return isFramed(win) && !(win.name ?? '').includes(MENU_IN_FRAME_MARKER)
}
