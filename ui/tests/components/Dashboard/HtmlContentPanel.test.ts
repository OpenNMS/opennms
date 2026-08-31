import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import HtmlContentPanel from '@/components/Dashboard/panels/HtmlContentPanel.vue'
import { TimeframePreset } from '@/types/dashboard'

const mountWith = (url: unknown) =>
  mount(HtmlContentPanel, {
    props: {
      panelId: 'test-panel',
      options: { url },
      filter: { surveillanceCategories: [], ipMatch: null },
      timeframe: { preset: TimeframePreset.Last24h, from: null, to: null },
      refreshTick: 0
    }
  })

describe('HtmlContentPanel URL safety', () => {
  it('loads a same-origin relative URL', () => {
    const w = mountWith('/opennms/graph/index.jsp')
    const iframe = w.find('iframe')
    expect(iframe.exists()).toBe(true)
    expect(iframe.attributes('src')).toContain('/opennms/graph/index.jsp')
  })

  it('refuses to render a javascript: URL', () => {
    const w = mountWith('javascript:fetch("/steal")')
    expect(w.find('iframe').exists()).toBe(false)
    expect(w.text()).toContain('not a valid same-origin')
  })

  it('refuses a data: URL', () => {
    const w = mountWith('data:text/html,<script>alert(1)</script>')
    expect(w.find('iframe').exists()).toBe(false)
  })

  it('refuses an external origin (frame-src self)', () => {
    const w = mountWith('https://evil.example.com/x')
    expect(w.find('iframe').exists()).toBe(false)
  })

  it('shows the empty hint when no URL is set', () => {
    const w = mountWith('')
    expect(w.find('iframe').exists()).toBe(false)
    expect(w.text()).toContain('No URL set')
  })
})
