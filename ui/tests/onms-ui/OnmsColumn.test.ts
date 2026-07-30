import { OnmsColumn } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import DataTable from 'primevue/datatable'
import { defineComponent } from 'vue'
import { describe, expect, it } from 'vitest'

// OnmsColumn is a compile-time re-export of PrimeVue Column (see the
// component's header comment). These tests pin the runtime contract the
// re-export depends on: discovery inside a DataTable, field/header
// rendering, and the #body slot scope.
const globalPlugins = { plugins: [PrimeVue] }

const rows = [
  { id: 1, name: 'node-a' },
  { id: 2, name: 'node-b' }
]

const mountTable = (template: string) =>
  mount(defineComponent({
    components: { DataTable, OnmsColumn },
    setup: () => ({ rows }),
    template
  }), { global: globalPlugins })

describe('OnmsColumn', () => {
  it('is discovered by DataTable and renders field/header', () => {
    const wrapper = mountTable(`
      <DataTable :value="rows">
        <OnmsColumn field="name" header="Name" />
      </DataTable>`)
    expect(wrapper.find('th').text()).toBe('Name')
    const cells = wrapper.findAll('td')
    expect(cells[0].text()).toBe('node-a')
    expect(cells[1].text()).toBe('node-b')
  })

  it('renders the #body slot with row scope', () => {
    const wrapper = mountTable(`
      <DataTable :value="rows">
        <OnmsColumn field="name" header="Name">
          <template #body="{ data }"><em class="cell">custom-{{ data.name }}</em></template>
        </OnmsColumn>
      </DataTable>`)
    const custom = wrapper.findAll('em.cell')
    expect(custom).toHaveLength(2)
    expect(custom[0].text()).toBe('custom-node-a')
  })

  it('supports v-for generated columns', () => {
    const wrapper = mount(defineComponent({
      components: { DataTable, OnmsColumn },
      setup: () => ({ rows, cols: [{ id: 'id', label: 'ID' }, { id: 'name', label: 'Name' }] }),
      template: `
        <DataTable :value="rows">
          <OnmsColumn v-for="col in cols" :key="col.id" :field="col.id" :header="col.label" />
        </DataTable>`
    }), { global: globalPlugins })
    expect(wrapper.findAll('th')).toHaveLength(2)
    expect(wrapper.findAll('td')).toHaveLength(4)
  })
})
