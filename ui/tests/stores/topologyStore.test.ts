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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTopologyStore } from '@/stores/topologyStore'
import { saveView, listViews } from '@/services/topologyService'
import type { TopologyView } from '@/types/topology'

vi.mock('@/services/topologyService', () => ({
  listViews: vi.fn(),
  getView: vi.fn(),
  saveView: vi.fn(),
  deleteView: vi.fn(),
  getNodeSeverities: vi.fn(),
  getNodeIconIds: vi.fn(),
  loadDiscoveredGraph: vi.fn()
}))

const snapshot = { nodes: [], links: [], viewport: { zoom: 1, panX: 0, panY: 0 }}

const existingView = (): TopologyView => ({
  id: '5',
  name: 'Existing',
  nodes: [],
  links: [],
  labels: [],
  viewport: { zoom: 1, panX: 0, panY: 0 }
})

describe('useTopologyStore - saveCurrentViewAs (Save As)', () => {
  let store: ReturnType<typeof useTopologyStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useTopologyStore()
    vi.clearAllMocks()
  })

  it('on success, adopts the saved view as current and POSTs a new entry (no id)', async () => {
    store.currentView = existingView()
    const saved: TopologyView = { ...existingView(), id: '9', name: 'Copy' }
    vi.mocked(saveView).mockResolvedValue(saved)
    vi.mocked(listViews).mockResolvedValue([{ id: '9', name: 'Copy' }])

    const ok = await store.saveCurrentViewAs('Copy', snapshot)

    expect(ok).toBe(true)
    expect(store.currentView).toEqual(saved)
    // Save As must create a new catalog entry: candidate carries no id.
    expect(saveView).toHaveBeenCalledWith(expect.objectContaining({ id: undefined, name: 'Copy' }))
  })

  it('on failure (e.g. duplicate name -> 409), leaves the open view UNCHANGED', async () => {
    store.currentView = existingView()
    vi.mocked(saveView).mockResolvedValue(false)

    const ok = await store.saveCurrentViewAs('Taken', snapshot)

    expect(ok).toBe(false)
    // The regression: the open view must keep its id and name -- not get
    // detached (id dropped) and renamed to the conflicting name.
    expect(store.currentView?.id).toBe('5')
    expect(store.currentView?.name).toBe('Existing')
    expect(listViews).not.toHaveBeenCalled()
    expect(store.isSaving).toBe(false)
  })

  it('returns false when there is no open view', async () => {
    store.currentView = null
    expect(await store.saveCurrentViewAs('Whatever', snapshot)).toBe(false)
    expect(saveView).not.toHaveBeenCalled()
  })
})

describe('useTopologyStore - node size (density default + clamp)', () => {
  let store: ReturnType<typeof useTopologyStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useTopologyStore()
  })

  it('defaults large for small graphs and small for dense graphs', () => {
    store.setNodeSizeForCount(5)
    expect(store.nodeSize).toBe(20)
    store.setNodeSizeForCount(10)
    expect(store.nodeSize).toBe(20)
    store.setNodeSizeForCount(100)
    expect(store.nodeSize).toBe(9)
    store.setNodeSizeForCount(55)
    expect(store.nodeSize).toBeGreaterThan(9)
    expect(store.nodeSize).toBeLessThan(20)
  })

  it('clamps the manual size to [MIN, MAX]', () => {
    store.setNodeSize(999)
    expect(store.nodeSize).toBe(store.NODE_SIZE_MAX)
    store.setNodeSize(-5)
    expect(store.nodeSize).toBe(store.NODE_SIZE_MIN)
    store.setNodeSize(14)
    expect(store.nodeSize).toBe(14)
  })
})

describe('useTopologyStore - view background', () => {
  let store: ReturnType<typeof useTopologyStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useTopologyStore()
    store.newView()
  })

  it('setBackground writes to the open view (so Save persists it) and clears', () => {
    expect(store.background).toBeUndefined()
    store.setBackground({ type: 'image', ref: 'asset:a1', x: -300, y: 200, width: 600, height: 400, opacity: 0.5 })
    expect(store.background?.ref).toBe('asset:a1')
    // saveCurrentView spreads currentView, so the view itself must carry it.
    expect(store.currentView?.background?.ref).toBe('asset:a1')
    store.setBackground(undefined)
    expect(store.background).toBeUndefined()
  })

  it('removing the background also leaves adjust mode', () => {
    store.setBackground({ type: 'image', ref: 'asset:a1', x: 0, y: 0, width: 100, height: 100 })
    store.setBackgroundAdjustMode(true)
    store.setBackground(undefined)
    expect(store.isBackgroundAdjustMode).toBe(false)
  })

  it('adjust mode never survives leaving Edit mode or switching views', () => {
    store.setBackgroundAdjustMode(true)
    store.setEditMode(false)
    expect(store.isBackgroundAdjustMode).toBe(false)

    store.setBackgroundAdjustMode(true)
    store.newView()
    expect(store.isBackgroundAdjustMode).toBe(false)
  })
})

describe('useTopologyStore - annotation shapes', () => {
  let store: ReturnType<typeof useTopologyStore>

  const shape = (id: string) => ({
    id,
    type: 'rect' as const,
    x: 0,
    y: 100,
    width: 200,
    height: 100,
    label: 'DC-1'
  })

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useTopologyStore()
    store.newView()
  })

  it('supports CRUD on shapes', () => {
    store.addShape(shape('shape-1'))
    expect(store.getShape('shape-1')?.label).toBe('DC-1')
    store.updateShape('shape-1', { label: 'DMZ', type: 'ellipse' })
    expect(store.getShape('shape-1')).toMatchObject({ label: 'DMZ', type: 'ellipse', width: 200 })
    store.removeShape('shape-1')
    expect(store.getShape('shape-1')).toBeUndefined()
  })

  it('saves shapes with the view and resets them on New', async () => {
    store.addShape(shape('shape-1'))
    vi.mocked(saveView).mockImplementation(async (v: TopologyView) => v)
    await store.saveCurrentView(snapshot)
    const savedArg = vi.mocked(saveView).mock.calls.at(-1)![0] as TopologyView
    expect(savedArg.shapes).toHaveLength(1)
    expect(savedArg.shapes![0].id).toBe('shape-1')

    store.newView()
    expect(store.shapes).toHaveLength(0)
  })

  it('shape draw mode is an Edit-mode gesture', () => {
    store.setShapeDrawMode(true)
    store.setEditMode(false)
    expect(store.isShapeDrawMode).toBe(false)
  })
})

describe('useTopologyStore - canvas prefs and view style', () => {
  let store: ReturnType<typeof useTopologyStore>

  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    store = useTopologyStore()
    store.newView()
  })

  it('stats overlay preference persists to localStorage, not the view', async () => {
    expect(store.showCanvasStats).toBe(true)
    store.setShowCanvasStats(false)
    expect(localStorage.getItem('opennms.topology.showCanvasStats')).toBe('false')

    vi.mocked(saveView).mockImplementation(async (v: TopologyView) => v)
    await store.saveCurrentView(snapshot)
    const savedArg = vi.mocked(saveView).mock.calls.at(-1)![0] as TopologyView & Record<string, unknown>
    expect('showCanvasStats' in savedArg).toBe(false)
  })

  it('label colors live on the view so Save persists them', async () => {
    store.setViewStyle({ nodeLabelColor: '#112233' })
    store.setViewStyle({ linkLabelColor: '#445566' })
    expect(store.viewStyle).toEqual({ nodeLabelColor: '#112233', linkLabelColor: '#445566' })

    vi.mocked(saveView).mockImplementation(async (v: TopologyView) => v)
    await store.saveCurrentView(snapshot)
    const savedArg = vi.mocked(saveView).mock.calls.at(-1)![0] as TopologyView
    expect(savedArg.style).toEqual({ nodeLabelColor: '#112233', linkLabelColor: '#445566' })
  })
})

describe('useTopologyStore - clearing view style back to automatic', () => {
  it('setViewStyle(undefined field) removes the override so theme defaults apply', () => {
    setActivePinia(createPinia())
    const store = useTopologyStore()
    store.newView()
    store.setViewStyle({ nodeLabelColor: '#112233', linkLabelColor: '#445566' })
    store.setViewStyle({ nodeLabelColor: undefined })
    expect(store.viewStyle?.nodeLabelColor).toBeUndefined()
    expect(store.viewStyle?.linkLabelColor).toBe('#445566')
  })
})
