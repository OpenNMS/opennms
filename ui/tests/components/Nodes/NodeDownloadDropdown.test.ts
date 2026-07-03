// ui/tests/components/Nodes/NodeDownloadDropdown.test.ts
import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import PrimeVue from 'primevue/config'
import NodeDownloadDropdown from '@/components/Nodes/NodeDownloadDropdown.vue'

const mountIt = (props: any) =>
  mount(NodeDownloadDropdown, { props, global: { plugins: [PrimeVue] }})

describe('NodeDownloadDropdown.vue', () => {
  it('builds CSV and JSON menu items wired to the props', () => {
    const onCsvDownload = vi.fn()
    const onJsonDownload = vi.fn()
    const wrapper = mountIt({ onCsvDownload, onJsonDownload })
    const items = (wrapper.vm as any).items as Array<{ label: string, command: () => void }>
    expect(items.map(i => i.label)).toEqual(['Download CSV...', 'Download JSON...'])
    items[0].command()
    items[1].command()
    expect(onCsvDownload).toHaveBeenCalledOnce()
    expect(onJsonDownload).toHaveBeenCalledOnce()
  })
})
