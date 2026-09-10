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
 * Whether this browser will give up a WebGL context.
 *
 * Probed rather than assumed because the map cannot degrade: sigma renders
 * through WebGL, and @sigma/node-image dereferences the context it asks for
 * without checking, so an unsupported browser throws
 * "can't access property getParameter, n is null" and nothing renders at all.
 *
 * Reasons a context is refused include hardware acceleration being off, a
 * blocklisted driver, a session with no GPU, fingerprinting protection, and
 * having reached the browser's limit on live contexts.
 */
export const hasWebGL = (): boolean => {
  try {
    const canvas = document.createElement('canvas')
    const gl = canvas.getContext('webgl2') ?? canvas.getContext('webgl')
    if (!gl) {
      return false
    }
    // Contexts are a limited resource, so the probe hands its own back rather
    // than waiting for the collector to notice.
    gl.getExtension('WEBGL_lose_context')?.loseContext()
    return true
  } catch {
    // Some builds throw rather than returning null.
    return false
  }
}
