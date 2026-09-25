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

import { defineComponent, h } from 'vue'

/**
 * Glyphs for the Edit tool strip that the shared icon set does not carry: a
 * pointer for the select tool and a rectangle for the box tool. Same shape as
 * the vendored icons, an inline 24-unit SVG filled with currentColor.
 */
const glyph = (name: string, paths: string[]) =>
  defineComponent({
    name,
    render() {
      return h(
        'svg',
        { xmlns: 'http://www.w3.org/2000/svg', viewBox: '0 0 24 24' },
        paths.map(d => h('path', { d }))
      )
    }
  })

export const SelectToolIcon = glyph('SelectToolIcon', [
  'M6 3.5v14.2l3.9-3.2 2.4 5.5 2.2-1-2.4-5.4h5.1L6 3.5z'
])

export const BoxToolIcon = glyph('BoxToolIcon', [
  'M4 5h16v14H4V5zm2 2v10h12V7H6z'
])
