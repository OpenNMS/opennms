import JdbcColumnsEditor from '@/components/JdbcDataCollection/JdbcColumnsEditor.vue'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import type { JdbcColumn } from '@/types/jdbcDataCollection'

const columns: JdbcColumn[] = [
  { name: 'a', dataSourceName: '', type: 'gauge', alias: 'aa' },
  { name: 'b', dataSourceName: 'B', type: 'counter', alias: 'bb' }
]

const mountEditor = (props: Record<string, unknown> = {}) =>
  mount(JdbcColumnsEditor, {
    props: { modelValue: columns, types: ['gauge', 'counter', 'string'], ...props },
    global: { plugins: [PrimeVue], directives: { onmsTooltip: {}}}
  })

describe('JdbcColumnsEditor.vue', () => {
  it('shows a row per column', () => {
    const wrapper = mountEditor()

    expect(wrapper.findAll('tbody tr')).toHaveLength(2)
    expect((wrapper.find('[data-test="jdbc-column-alias-1"]').element as HTMLInputElement).value).toBe('bb')
  })

  it('emits a changed copy on input', async () => {
    const wrapper = mountEditor()

    await wrapper.find('[data-test="jdbc-column-alias-0"]').setValue('changed')

    const emitted = wrapper.emitted('update:modelValue')?.[0]?.[0] as JdbcColumn[]
    expect(emitted[0].alias).toBe('changed')
    expect(emitted[1]).toEqual(columns[1])
    expect(columns[0].alias).toBe('aa')
  })

  it('adds and removes columns', async () => {
    const wrapper = mountEditor()

    await wrapper.find('[data-test="jdbc-column-add"]').trigger('click')
    await wrapper.find('[data-test="jdbc-column-remove-0"]').trigger('click')

    const events = wrapper.emitted('update:modelValue') as JdbcColumn[][][]
    expect(events[0][0]).toHaveLength(3)
    expect(events[0][0][2]).toEqual({ name: '', dataSourceName: '', type: 'gauge', alias: '' })
    expect(events[1][0]).toEqual([columns[1]])
  })

  it('shows the errors of a row', () => {
    const wrapper = mountEditor({ errors: [{}, { alias: 'At most 19 characters.' }] })

    expect(wrapper.find('[data-test="jdbc-column-1"]').text()).toContain('At most 19 characters.')
    expect(wrapper.find('[data-test="jdbc-column-0"]').text()).not.toContain('At most')
  })

  it('says when there are no columns', () => {
    expect(mountEditor({ modelValue: [] }).text()).toContain('No columns.')
  })
})
