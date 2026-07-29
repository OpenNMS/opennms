import { OnmsColumn, OnmsTable } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { defineComponent } from 'vue'
import { describe, expect, it } from 'vitest'

const globalPlugins = { plugins: [PrimeVue] }
const rows = [{ id: 1, name: 'node-a' }, { id: 2, name: 'node-b' }]

describe('OnmsTable', () => {
  it('forwards data/pagination/sort/display props to the inner DataTable', () => {
    const inner = mount(OnmsTable, {
      props: {
        value: rows,
        dataKey: 'id',
        lazy: true,
        paginator: true,
        rows: 20,
        rowsPerPageOptions: [10, 20, 50],
        first: 40,
        totalRecords: 123,
        sortField: 'name',
        sortOrder: -1,
        stripedRows: true,
        size: 'small',
        scrollable: true,
        scrollHeight: '400px',
        tableStyle: 'min-width: 50rem'
      },
      global: globalPlugins
    }).findComponent({ name: 'DataTable' })
    expect(inner.props('value')).toEqual(rows)
    expect(inner.props('dataKey')).toBe('id')
    expect(inner.props('lazy')).toBe(true)
    expect(inner.props('paginator')).toBe(true)
    expect(inner.props('rows')).toBe(20)
    expect(inner.props('rowsPerPageOptions')).toEqual([10, 20, 50])
    expect(inner.props('first')).toBe(40)
    expect(inner.props('totalRecords')).toBe(123)
    expect(inner.props('sortField')).toBe('name')
    expect(inner.props('sortOrder')).toBe(-1)
    expect(inner.props('stripedRows')).toBe(true)
    expect(inner.props('size')).toBe('small')
    expect(inner.props('scrollable')).toBe(true)
    expect(inner.props('scrollHeight')).toBe('400px')
    expect(inner.props('tableStyle')).toBe('min-width: 50rem')
  })

  it('maps virtualScrollItemSize to virtualScrollerOptions', () => {
    const inner = mount(OnmsTable, {
      props: { value: rows, virtualScrollItemSize: 44 },
      global: globalPlugins
    }).findComponent({ name: 'DataTable' })
    expect(inner.props('virtualScrollerOptions')).toEqual({ itemSize: 44 })
  })

  it('leaves virtualScrollerOptions unset by default', () => {
    const inner = mount(OnmsTable, { props: { value: rows }, global: globalPlugins })
      .findComponent({ name: 'DataTable' })
    expect(inner.props('virtualScrollerOptions')).toBeNull()
  })

  it('bakes th scope="col" via table-level pt', () => {
    const wrapper = mount(defineComponent({
      components: { OnmsTable, OnmsColumn },
      setup: () => ({ rows }),
      template: `
        <OnmsTable :value="rows">
          <OnmsColumn field="name" header="Name" />
        </OnmsTable>`
    }), { global: globalPlugins })
    expect(wrapper.find('th').attributes('scope')).toBe('col')
  })

  it('deep-merges unsafePt with the baked headerCell pt (consumer keys win)', () => {
    const inner = mount(OnmsTable, {
      props: {
        value: rows,
        unsafePt: { root: { 'data-x': 'y' }, column: { headerCell: { 'data-col': 'z' }}}
      },
      global: globalPlugins
    }).findComponent({ name: 'DataTable' })
    expect(inner.props('pt')).toEqual({
      root: { 'data-x': 'y' },
      column: { headerCell: { scope: 'col', 'data-col': 'z' }}
    })
  })

  it('re-emits page, sort and row-edit-save with the original payloads', () => {
    const wrapper = mount(OnmsTable, { props: { value: rows }, global: globalPlugins })
    const inner = wrapper.findComponent({ name: 'DataTable' })
    const pageEvent = { page: 2, first: 40, rows: 20, pageCount: 7 }
    const sortEvent = { sortField: 'name', sortOrder: -1 }
    const editEvent = { data: rows[0], newData: { ...rows[0], name: 'edited' }, index: 0 }
    inner.vm.$emit('page', pageEvent)
    inner.vm.$emit('sort', sortEvent)
    inner.vm.$emit('row-edit-save', editEvent)
    expect(wrapper.emitted('page')![0]).toEqual([pageEvent])
    expect(wrapper.emitted('sort')![0]).toEqual([sortEvent])
    expect(wrapper.emitted('row-edit-save')![0]).toEqual([editEvent])
  })

  it('supports the v-model channels (first/rows/expandedRows/editingRows)', () => {
    const wrapper = mount(OnmsTable, { props: { value: rows }, global: globalPlugins })
    const inner = wrapper.findComponent({ name: 'DataTable' })
    inner.vm.$emit('update:first', 20)
    inner.vm.$emit('update:rows', 50)
    inner.vm.$emit('update:expandedRows', [rows[0]])
    inner.vm.$emit('update:editingRows', [rows[1]])
    expect(wrapper.emitted('update:first')![0]).toEqual([20])
    expect(wrapper.emitted('update:rows')![0]).toEqual([50])
    expect(wrapper.emitted('update:expandedRows')![0]).toEqual([[rows[0]]])
    expect(wrapper.emitted('update:editingRows')![0]).toEqual([[rows[1]]])
  })

  it('renders the #empty slot when there are no rows', () => {
    const wrapper = mount(defineComponent({
      components: { OnmsTable, OnmsColumn },
      template: `
        <OnmsTable :value="[]">
          <OnmsColumn field="name" header="Name" />
          <template #empty>Nothing found</template>
        </OnmsTable>`
    }), { global: globalPlugins })
    expect(wrapper.text()).toContain('Nothing found')
  })

  it('falls through DOM attrs (aria-label, data-test) to the root', () => {
    const wrapper = mount(OnmsTable, {
      props: { value: rows },
      attrs: { 'aria-label': 'My table', 'data-test': 'my-table' },
      global: globalPlugins
    })
    expect(wrapper.find('[aria-label="My table"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="my-table"]').exists()).toBe(true)
  })
})
