import { OnmsPaginator } from '@opennms/onms-ui'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const mountPaginator = (props: object = {}) =>
  mount(OnmsPaginator, { props: { totalRecords: 25, rows: 10, ...props }, global: { plugins: [PrimeVue] }})

describe('OnmsPaginator contract', () => {
  it('maps its props to the PrimeVue Paginator', () => {
    const inner = mountPaginator({ first: 10, rowsPerPageOptions: [10, 20], pageLinkSize: 3 }).findComponent({ name: 'Paginator' })

    expect(inner.props('first')).toBe(10)
    expect(inner.props('rows')).toBe(10)
    expect(inner.props('totalRecords')).toBe(25)
    expect(inner.props('rowsPerPageOptions')).toEqual([10, 20])
    expect(inner.props('pageLinkSize')).toBe(3)
  })

  it('renders a page link per page', () => {
    expect(mountPaginator().findAll('.p-paginator-page').map(p => p.text())).toEqual(['1', '2', '3'])
  })

  it('emits page in the OnmsTablePageEvent shape when a page is chosen', async () => {
    const wrapper = mountPaginator()

    await wrapper.findAll('.p-paginator-page')[1].trigger('click')

    expect(wrapper.emitted('page')![0][0]).toEqual({ page: 1, first: 10, rows: 10, pageCount: 3 })
  })

  it('can hide itself when everything fits on one page', () => {
    expect(mountPaginator({ totalRecords: 5, alwaysShow: false }).find('.p-paginator').exists()).toBe(false)
    expect(mountPaginator({ totalRecords: 5 }).find('.p-paginator').exists()).toBe(true)
  })

  it('passes unsafePt through as pt', () => {
    const pt = { root: { class: 'escape-hatch' }}

    expect(mountPaginator({ unsafePt: pt }).findComponent({ name: 'Paginator' }).props('pt')).toEqual(pt)
  })
})
