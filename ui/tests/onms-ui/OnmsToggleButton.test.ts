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

import { OnmsIcon, OnmsToggleButton } from '@opennms/onms-ui'
import CheckCircle from '@opennms/onms-ui/icons/action/CheckCircle.vue'
import Remove from '@opennms/onms-ui/icons/action/Remove.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const mountIt = (props: Record<string, unknown> = {}) =>
  mount(OnmsToggleButton, {
    props,
    global: { plugins: [PrimeVue] }
  })

const inner = (wrapper: ReturnType<typeof mountIt>) => wrapper.findComponent({ name: 'ToggleButton' })

describe('OnmsToggleButton contract', () => {
  it('maps props onto PrimeVue ToggleButton', () => {
    const toggle = inner(mountIt({ modelValue: true, onLabel: 'On', offLabel: 'Off', disabled: true, size: 'small', fluid: true }))
    expect(toggle.props('modelValue')).toBe(true)
    expect(toggle.props('onLabel')).toBe('On')
    expect(toggle.props('offLabel')).toBe('Off')
    expect(toggle.props('disabled')).toBe(true)
    expect(toggle.props('size')).toBe('small')
    expect(toggle.props('fluid')).toBe(true)
  })

  it('uses label for both states', () => {
    const wrapper = mountIt({ label: 'Ingress' })
    expect(inner(wrapper).props('onLabel')).toBe('Ingress')
    expect(inner(wrapper).props('offLabel')).toBe('Ingress')
    expect(wrapper.text()).toBe('Ingress')
  })

  it('lets onLabel and offLabel override label', () => {
    const toggle = inner(mountIt({ label: 'Ingress', onLabel: 'Ingress on' }))
    expect(toggle.props('onLabel')).toBe('Ingress on')
    expect(toggle.props('offLabel')).toBe('Ingress')
  })

  it('shows no text without a label, not PrimeVue\'s Yes / No', () => {
    expect(mountIt().text()).not.toMatch(/Yes|No/)
  })

  it('shows the icon for the current state, with a class for each state', async () => {
    const wrapper = mountIt({ modelValue: false, onIcon: CheckCircle, offIcon: Remove })
    let icon = wrapper.findComponent(OnmsIcon)
    expect(icon.props('icon')).toStrictEqual(Remove)
    expect(icon.classes()).toEqual(expect.arrayContaining(['onms-toggle-button-icon', 'onms-toggle-button-icon--off']))

    await wrapper.setProps({ modelValue: true })
    icon = wrapper.findComponent(OnmsIcon)
    expect(icon.props('icon')).toStrictEqual(CheckCircle)
    expect(icon.classes()).toEqual(expect.arrayContaining(['onms-toggle-button-icon', 'onms-toggle-button-icon--on']))
    expect(icon.classes()).not.toContain('onms-toggle-button-icon--off')
  })

  it('shows no icon for a state without one', () => {
    expect(mountIt({ modelValue: false, onIcon: CheckCircle }).findComponent(OnmsIcon).exists()).toBe(false)
  })

  it('emits the new value on update:modelValue and change', async () => {
    const wrapper = mountIt({ modelValue: false, label: 'Ingress' })
    await wrapper.find('button').trigger('click')
    expect(wrapper.emitted('update:modelValue')).toEqual([[true]])
    expect(wrapper.emitted('change')).toEqual([[true]])
  })

  it('falls DOM attrs through to the button', () => {
    const button = mountIt({ 'aria-label': 'Ingress flows', 'data-test': 'flow-toggle' }).find('button')
    expect(button.attributes('aria-label')).toBe('Ingress flows')
    expect(button.attributes('data-test')).toBe('flow-toggle')
  })
})
