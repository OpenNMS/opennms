import DestinationPathEditorPanel from '@/components/Notifications/DestinationPathEditorPanel.vue'
import TargetRowEditor from '@/components/Notifications/TargetRowEditor.vue'
import { useNotificationConfigStore } from '@/stores/notificationConfigStore'
import { DestinationPath } from '@/types/notificationConfig'
import { createTestingPinia } from '@pinia/testing'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import { nextTick } from 'vue'

const path: DestinationPath = {
  name: 'Email-Admin',
  'initial-delay': '0s',
  target: [
    { name: 'Admin', command: ['javaEmail'] },
    { name: 'Unix-Admins', command: ['javaEmail'] }
  ],
  escalate: [{ delay: '15m', target: [{ name: 'Oncall', command: ['javaEmail'] }] }]
}

// The tab fetches users/groups/roles alongside the paths, so the panel can open
// before they land; start with empty lookups to reproduce that.
const mountPanel = () => {
  const wrapper = mount(DestinationPathEditorPanel, {
    props: { path },
    global: {
      plugins: [PrimeVue, createTestingPinia({ stubActions: false })]
    }
  })
  return { wrapper, store: useNotificationConfigStore() }
}

const rowTypes = (wrapper: ReturnType<typeof mountPanel>['wrapper']) =>
  wrapper.findAllComponents(TargetRowEditor).map(row => row.props('row').type)

describe('DestinationPathEditorPanel', () => {
  it('re-infers target types once the group and role lists arrive', async () => {
    const { wrapper, store } = mountPanel()
    expect(rowTypes(wrapper)).toEqual(['user', 'user', 'user'])

    store.groups = ['Admin', 'Unix-Admins']
    await nextTick()
    expect(rowTypes(wrapper)).toEqual(['group', 'group', 'user'])

    store.roles = ['Oncall']
    await nextTick()
    expect(rowTypes(wrapper)).toEqual(['group', 'group', 'role'])
  })

  it('leaves a row alone once the user has changed it', async () => {
    const { wrapper, store } = mountPanel()
    const firstRow = wrapper.findAllComponents(TargetRowEditor)[0].props('row')

    // what TargetRowEditor does on a type change
    firstRow.type = 'email'
    firstRow.name = ''
    await nextTick()

    store.groups = ['Admin', 'Unix-Admins']
    await nextTick()
    expect(rowTypes(wrapper)).toEqual(['email', 'group', 'user'])
  })
})
