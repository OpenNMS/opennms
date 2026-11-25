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

import SeverityIcon from '@/components/Common/SeverityIcon.vue'
import { OnmsIcon } from '@opennms/onms-ui'
import Critical from '@opennms/onms-ui/icons/severity/Critical.vue'
import Major from '@opennms/onms-ui/icons/severity/Major.vue'
import Minor from '@opennms/onms-ui/icons/severity/Minor.vue'
import None from '@opennms/onms-ui/icons/severity/None.vue'
import Normal from '@opennms/onms-ui/icons/severity/Normal.vue'
import Warning from '@opennms/onms-ui/icons/severity/Warning.vue'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'
import { toRaw } from 'vue'

const mountIcon = (props: Record<string, unknown> = {}) => mount(SeverityIcon, { props })

const iconOf = (wrapper: ReturnType<typeof mountIcon>) => toRaw(wrapper.findComponent(OnmsIcon).props('icon'))

describe('SeverityIcon.vue', () => {
  it.each([
    ['CRITICAL', 'critical', Critical],
    ['MAJOR', 'major', Major],
    ['MINOR', 'minor', Minor],
    ['WARNING', 'warning', Warning],
    ['NORMAL', 'normal', Normal]
  ])('shows %s with its own icon and colors', (severity, key, icon) => {
    const wrapper = mountIcon({ severity })

    expect(iconOf(wrapper)).toBe(icon)
    expect(wrapper.classes()).toContain(`severity-icon--${key}`)
  })

  it('shows INDETERMINATE with the "?" icon in its own colors', () => {
    const wrapper = mountIcon({ severity: 'INDETERMINATE' })

    expect(iconOf(wrapper)).toBe(None)
    expect(wrapper.classes()).toContain('severity-icon--indeterminate')
  })

  it.each(['CLEARED', 'BOGUS', undefined])('shows %s with the grey "?" icon', (severity) => {
    const wrapper = mountIcon({ severity })

    expect(iconOf(wrapper)).toBe(None)
    expect(wrapper.classes()).toContain('severity-icon--none')
  })

  it('takes the severity in any case', () => {
    expect(mountIcon({ severity: 'major' }).classes()).toContain('severity-icon--major')
  })

  it('is an image named for its severity', () => {
    const wrapper = mountIcon({ severity: 'MAJOR' })

    expect(wrapper.attributes('role')).toBe('img')
    expect(wrapper.attributes('aria-label')).toBe('Major severity')
  })

  it('names a severity OpenNMS does not have as unknown', () => {
    expect(mountIcon({ severity: 'BOGUS' }).attributes('aria-label')).toBe('Unknown severity')
    expect(mountIcon().attributes('aria-label')).toBe('Unknown severity')
  })

  it('takes a name of its own', () => {
    expect(mountIcon({ severity: 'MAJOR', label: 'Worst alarm: Major' }).attributes('aria-label')).toBe('Worst alarm: Major')
  })

  it('hides the icon inside from assistive technology, so the name is said once', () => {
    expect(mountIcon({ severity: 'MAJOR' }).find('svg').attributes('aria-hidden')).toBe('true')
  })

  it.each(['small', 'medium', 'large'])('has the %s size', (size) => {
    expect(mountIcon({ severity: 'MAJOR', size }).classes()).toContain(`severity-icon--${size}`)
  })

  it('is medium by default', () => {
    expect(mountIcon({ severity: 'MAJOR' }).classes()).toContain('severity-icon--medium')
  })
})
