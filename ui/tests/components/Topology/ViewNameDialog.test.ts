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

import { OnmsDialog } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'
import { nextTick } from 'vue'
import ViewNameDialog from '@/components/Topology/ViewNameDialog.vue'

// The dialog replaces window.prompt for New / Save As / Rename. What it owes the
// callers is a trimmed name, a refusal to submit one that is blank or already
// taken, and a field seeded fresh on every open.

const mounted: { unmount: () => void }[] = []

const open = async (props: Record<string, unknown> = {}) => {
  const wrapper = mount(ViewNameDialog, {
    props: { visible: false, title: 'Rename view', actionLabel: 'Rename', ...props },
    global: { plugins: [PrimeVue] },
    attachTo: document.body
  })
  mounted.push(wrapper)
  await wrapper.setProps({ visible: true })
  await nextTick()
  return wrapper
}

const field = () => document.querySelector('.p-dialog input') as HTMLInputElement

const button = (label: string) => {
  const found = Array.from(document.querySelectorAll('.p-dialog button'))
    .find(b => b.textContent?.trim() === label) as HTMLButtonElement | undefined
  expect(found, `no "${label}" button rendered`).toBeTruthy()
  return found!
}

const type = async (value: string) => {
  const input = field()
  input.value = value
  input.dispatchEvent(new Event('input'))
  await nextTick()
}

describe('ViewNameDialog', () => {
  afterEach(() => {
    while (mounted.length) {
      mounted.pop()!.unmount()
    }
    document.body.innerHTML = ''
  })

  it('seeds the field from initialName when it opens', async () => {
    await open({ initialName: 'Core switches' })
    expect(field().value).toBe('Core switches')
  })

  it('emits the name trimmed', async () => {
    const wrapper = await open()
    await type('  Edge routers  ')
    button('Rename').click()
    expect(wrapper.emitted('submit')![0]).toEqual(['Edge routers'])
  })

  it('closes itself on submit, so the caller does not have to', async () => {
    const wrapper = await open()
    await type('Edge routers')
    button('Rename').click()
    expect(wrapper.emitted('update:visible')!.at(-1)).toEqual([false])
  })

  it('refuses a name already in the catalog, and says which', async () => {
    const wrapper = await open({ takenNames: ['Core switches', 'Edge routers'] })
    await type('Edge routers')

    expect(button('Rename').disabled).toBe(true)
    expect(document.querySelector('.p-dialog')!.textContent)
      .toContain('A view named "Edge routers" already exists.')

    // Enter bypasses the button, so it needs the same guard.
    field().dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }))
    await nextTick()
    expect(wrapper.emitted('submit')).toBeUndefined()
  })

  it('accepts a name once the collision is edited away', async () => {
    const wrapper = await open({ takenNames: ['Edge routers'] })
    await type('Edge routers')
    await type('Edge routers 2')

    expect(button('Rename').disabled).toBe(false)
    field().dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }))
    await nextTick()
    expect(wrapper.emitted('submit')![0]).toEqual(['Edge routers 2'])
  })

  it('refuses a blank or whitespace-only name', async () => {
    const wrapper = await open({ initialName: '' })
    expect(button('Rename').disabled).toBe(true)

    await type('   ')
    expect(button('Rename').disabled).toBe(true)
    field().dispatchEvent(new KeyboardEvent('keyup', { key: 'Enter', bubbles: true }))
    await nextTick()
    expect(wrapper.emitted('submit')).toBeUndefined()
  })

  it('reseeds on reopen, discarding an abandoned edit', async () => {
    const wrapper = await open({ initialName: 'Core switches' })
    await type('half-typed name')
    button('Cancel').click()
    await wrapper.setProps({ visible: false })

    await wrapper.setProps({ visible: true })
    await nextTick()
    expect(field().value).toBe('Core switches')
  })

  it('cancelling asks to close and submits nothing', async () => {
    const wrapper = await open({ initialName: 'Core switches' })
    button('Cancel').click()
    expect(wrapper.emitted('update:visible')!.at(-1)).toEqual([false])
    expect(wrapper.emitted('submit')).toBeUndefined()
  })

  it('puts the caret in the field with the seeded name selected', async () => {
    const wrapper = await open({ initialName: 'Core switches' })
    // The dialog appends to body behind a transition, so focus waits for the
    // wrapper's show event rather than mount.
    wrapper.findComponent(OnmsDialog).vm.$emit('show')
    await nextTick()

    expect(document.activeElement).toBe(field())
    expect(field().selectionStart).toBe(0)
    expect(field().selectionEnd).toBe('Core switches'.length)
  })
})
