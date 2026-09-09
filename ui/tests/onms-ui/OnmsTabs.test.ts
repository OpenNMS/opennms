import { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels, OnmsTabs } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, describe, expect, it } from 'vitest'

// PrimeVue's TabList schedules updateInkBar on a timer at mount and never clears
// it. A wrapper left mounted lets that timer outlive the test environment, and it
// then throws "HTMLElement is not defined" as an uncaught exception — which fails
// the whole run, intermittently, depending on how the suite happens to be
// scheduled. Unmounting after each test disposes the timer with the component.
const mounted: ReturnType<typeof mount>[] = []

const track = (wrapper: ReturnType<typeof mount>) => {
  mounted.push(wrapper)
  return wrapper
}

const mountTabs = (tabsProps: Record<string, unknown> = {}) => track(mount(OnmsTabs, {
  props: { value: 0, ...tabsProps },
  global: {
    plugins: [PrimeVue],
    // registered so the Onms* tags resolve inside the string slot
    components: { OnmsTab, OnmsTabList, OnmsTabPanel, OnmsTabPanels }
  },
  slots: {
    default: `
      <OnmsTabList>
        <OnmsTab :value="0">First</OnmsTab>
        <OnmsTab :value="1">Second</OnmsTab>
      </OnmsTabList>
      <OnmsTabPanels>
        <OnmsTabPanel :value="0">Panel one</OnmsTabPanel>
        <OnmsTabPanel :value="1">Panel two</OnmsTabPanel>
      </OnmsTabPanels>`
  }
}))

describe('OnmsTabs family contract', () => {
  afterEach(() => {
    while (mounted.length) {
      mounted.pop()?.unmount()
    }
  })

  it('maps value onto PrimeVue Tabs and renders tab labels', () => {
    const wrapper = mountTabs()
    expect(wrapper.findComponent({ name: 'Tabs' }).props('value')).toBe(0)
    expect(wrapper.text()).toContain('First')
    expect(wrapper.text()).toContain('Panel one')
  })

  it('forwards update:value for v-model:value', async () => {
    const wrapper = mountTabs()
    await wrapper.findComponent({ name: 'Tabs' }).vm.$emit('update:value', 1)
    expect(wrapper.emitted('update:value')).toEqual([[1]])
  })

  it('accepts string values', () => {
    const wrapper = track(mount(OnmsTabs, {
      props: { value: 'alarms' },
      global: { plugins: [PrimeVue] }
    }))
    expect(wrapper.findComponent({ name: 'Tabs' }).props('value')).toBe('alarms')
  })

  it('OnmsTab maps value', () => {
    const wrapper = mountTabs()
    const tab = wrapper.findAllComponents({ name: 'Tab' })[0]
    expect(tab.props('value')).toBe(0)
    expect(tab.classes()).toContain('p-tab')
  })
})
