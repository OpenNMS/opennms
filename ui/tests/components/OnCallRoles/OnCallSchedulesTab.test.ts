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
///

import { enableAutoUnmount, mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it, vi } from 'vitest'

import OnCallSchedulesTab from '@/components/OnCallRoles/OnCallSchedulesTab.vue'
import { cardProblems, newCard } from '@/lib/onCallSchedule'

enableAutoUnmount(afterEach)

const noop = { mounted: () => undefined }

describe('OnCallSchedulesTab', () => {
  const mountTab = (cards = [newCard('bob', 'daily')]) =>
    mount(OnCallSchedulesTab, {
      props: { cards, members: ['bob'], highlightKey: null },
      global: { plugins: [PrimeVue], directives: { 'onms-tooltip': noop, tooltip: noop }}
    })

  it('appends an empty card with a problem on Add Schedule', async () => {
    const w = mountTab([])
    await w.find('[data-test="add-schedule"]').trigger('click')
    const cards = w.emitted('update:cards')!.at(-1)![0] as ReturnType<typeof newCard>[]
    expect(cards).toHaveLength(1)
    expect(cardProblems(cards[0]).length).toBeGreaterThan(0)
  })

  it('removes a card', async () => {
    const w = mountTab([newCard('bob', 'daily'), newCard('bob', 'weekly')])
    await w.findAll('[data-test="remove-schedule"]')[0].trigger('click')
    expect((w.emitted('update:cards')!.at(-1)![0] as unknown[])).toHaveLength(1)
  })

  it('highlights and scrolls to the card named by highlightKey', async () => {
    const cards = [newCard('bob', 'daily'), newCard('bob', 'weekly')]
    const w = mountTab(cards)
    await w.setProps({ highlightKey: cards[1].key })
    await nextTick()
    await nextTick()
    const els = w.findAll('[data-test="oncall-schedule-card"]')
    expect(els[1].classes()).toContain('highlight')
    expect(els[0].classes()).not.toContain('highlight')
  })

  it('highlights a card named by a highlightKey already set at mount, then clears after 2 s', async () => {
    vi.useFakeTimers()
    try {
      const scroll = vi.fn()
      Element.prototype.scrollIntoView = scroll
      const cards = [newCard('bob', 'daily'), newCard('bob', 'weekly')]
      const w = mount(OnCallSchedulesTab, {
        props: { cards, members: ['bob'], highlightKey: cards[1].key },
        global: { plugins: [PrimeVue], directives: { 'onms-tooltip': noop, tooltip: noop }}
      })
      await nextTick()
      await nextTick()
      expect(w.findAll('[data-test="oncall-schedule-card"]')[1].classes()).toContain('highlight')
      expect(scroll).toHaveBeenCalled()
      expect(w.emitted('highlighted')).toHaveLength(1)
      await vi.advanceTimersByTimeAsync(2000)
      expect(w.findAll('[data-test="oncall-schedule-card"]')[1].classes()).not.toContain('highlight')
    } finally {
      vi.useRealTimers()
      // @ts-expect-error restoring the missing DOM method
      delete Element.prototype.scrollIntoView
    }
  })
})
