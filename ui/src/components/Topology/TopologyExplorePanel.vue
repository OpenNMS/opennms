<!--
  Bottom Explore panel: collapsible tables of what is in the current view,
  filtered by the canvas selection. Alarms and Nodes always (the legacy map's
  AlarmTable/NodeTable), plus Applications and Perspective Outages while the
  Application graph is the loaded source.

  Selection runs both ways: clicking a row selects that node on the canvas, and
  selecting on the canvas filters the tables. Selecting several nodes filters to
  all of them; selecting a vertex that is not a node itself (an application)
  filters to what hangs off it.
-->
<template>
  <section class="topology-explore" :class="{ collapsed }">
    <header class="te-header">
      <button class="te-toggle" type="button" @click="collapsed = !collapsed">
        <span class="te-caret">{{ collapsed ? '▸' : '▾' }}</span> Explore
      </button>
      <div v-if="!collapsed" class="te-tabs">
        <button
          v-for="t in tabs"
          :key="t.key"
          type="button"
          :class="{ active: tab === t.key }"
          @click="tab = t.key"
        >
          {{ t.label }} ({{ t.count }})
        </button>
      </div>
      <span v-if="!collapsed && (isFiltered || selectedApplicationIds.length > 0)" class="te-filter">
        filtered to selection
        <a href="#" @click.prevent="$emit('select', null)">show all</a>
      </span>
    </header>

    <div v-if="!collapsed" class="te-body">
      <p v-if="loading" class="te-empty">Loading…</p>
      <p v-else-if="nodeRows.length === 0 && tab !== 'applications'" class="te-empty">
        No nodes in this view.
      </p>

      <OnmsTable
        v-else-if="tab === 'nodes'"
        :value="filteredNodeRows"
        data-key="id"
        scrollable
        scroll-height="flex"
        size="small"
        selection-mode="single"
        @row-click="onRowSelect($event.data.id)"
      >
        <OnmsColumn header="" :style="{ width: '2rem' }">
          <template #body="{ data }">
            <span class="te-dot" :style="{ background: severityColor(data.severity) }" />
          </template>
        </OnmsColumn>
        <OnmsColumn field="label" header="Node" sortable />
        <OnmsColumn field="severity" header="Highest Alarm Severity" sortable />
        <OnmsColumn field="location" header="Location" sortable />
      </OnmsTable>

      <OnmsTable
        v-else-if="tab === 'applications'"
        :value="filteredApplicationRows"
        data-key="id"
        scrollable
        scroll-height="flex"
        size="small"
      >
        <OnmsColumn field="name" header="Application" sortable />
        <OnmsColumn header="Services">
          <template #body="{ data }">{{ serviceCountFor(data.id) }}</template>
        </OnmsColumn>
        <OnmsColumn header="Perspectives">
          <template #body="{ data }">
            {{ data.perspectiveLocations.length ? data.perspectiveLocations.join(', ') : '—' }}
          </template>
        </OnmsColumn>
      </OnmsTable>

      <OnmsTable
        v-else-if="tab === 'perspective'"
        :value="filteredPerspectiveRows"
        data-key="id"
        scrollable
        scroll-height="flex"
        size="small"
        selection-mode="single"
        @row-click="onRowSelect(`placed-${$event.data.nodeId}`)"
      >
        <OnmsColumn field="nodeLabel" header="Node" sortable />
        <OnmsColumn field="serviceName" header="Service" sortable />
        <OnmsColumn field="perspective" header="Perspective" sortable />
        <OnmsColumn field="lostAt" header="Down since" sortable>
          <template #body="{ data }">{{ formatTime(data.lostAt) }}</template>
        </OnmsColumn>
      </OnmsTable>

      <OnmsTable
        v-else-if="tab === 'alarms'"
        :value="filteredAlarmRows"
        data-key="id"
        scrollable
        scroll-height="flex"
        size="small"
        selection-mode="single"
        @row-click="onRowSelect(`placed-${$event.data.nodeId}`)"
      >
        <OnmsColumn header="" :style="{ width: '2rem' }">
          <template #body="{ data }">
            <span class="te-dot" :style="{ background: severityColor(data.severity) }" />
          </template>
        </OnmsColumn>
        <OnmsColumn field="nodeLabel" header="Node" sortable />
        <OnmsColumn field="logMessage" header="Message">
          <template #body="{ data }">
            <span class="te-msg" v-text="stripHtml(data.logMessage)" />
          </template>
        </OnmsColumn>
        <OnmsColumn field="lastEventTime" header="Last event" sortable>
          <template #body="{ data }">{{ formatTime(data.lastEventTime) }}</template>
        </OnmsColumn>
      </OnmsTable>
      <p v-if="!loading && tab === 'alarms' && filteredAlarmRows.length === 0" class="te-empty">
        No alarms for these nodes.
      </p>
      <p v-if="!loading && tab === 'applications' && filteredApplicationRows.length === 0" class="te-empty">
        {{ applicationRows.length === 0 ? 'No applications defined.' : 'No applications match the selection.' }}
      </p>
      <p v-if="!loading && tab === 'perspective' && filteredPerspectiveRows.length === 0" class="te-empty">
        No perspective currently reports an outage for these nodes.
      </p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { OnmsColumn, OnmsTable } from '@opennms/onms-ui'
import { useTopologyStore } from '@/stores/topologyStore'
import { severityColor } from '@/components/Topology/severity'
import { nodeIdFromPlacedId } from '@/components/Topology/nodeIds'
import { getNodes } from '@/services/nodeService'
import { getAlarms } from '@/services/alarmService'
import {
  chunkByQueryLength,
  getApplications,
  getPerspectiveOutages,
  type PerspectiveOutage,
  type TopologyApplication
} from '@/services/topologyService'


const emit = defineEmits<{ (e: 'select', placedId: string | null): void }>()

const store = useTopologyStore()

const collapsed = ref(true)
// Alarms first, and active by default: it is the tab an operator opens the
// panel for, and leaving Nodes selected would highlight the second tab.
type ExploreTab = 'alarms' | 'nodes' | 'applications' | 'perspective'
const tab = ref<ExploreTab>('alarms')
const loading = ref(false)

/**
 * Applications and perspective outages are only meaningful for the Application
 * graph, so those two tabs appear only while it is the loaded source.
 */
const isApplicationGraph = computed(() =>
  store.discoveredGraph?.source.container === 'application'
)

interface TabDef {
  key: ExploreTab
  label: string
  /** Rows the tab will actually render, so the count tracks the selection. */
  count: number
}

/**
 * Tabs in display order. The Application graph leads with what an operator is
 * chasing there -- alarms, then what a perspective reports down -- and keeps the
 * raw node list last.
 */
const tabs = computed<TabDef[]>(() => {
  const alarms: TabDef = { key: 'alarms', label: 'Alarms', count: filteredAlarmRows.value.length }
  const nodes: TabDef = { key: 'nodes', label: 'Nodes', count: filteredNodeRows.value.length }
  if (!isApplicationGraph.value) {
    return [alarms, nodes]
  }
  return [
    alarms,
    { key: 'perspective', label: 'Perspective Outages', count: filteredPerspectiveRows.value.length },
    { key: 'applications', label: 'Applications', count: filteredApplicationRows.value.length },
    nodes
  ]
})

interface NodeRow {
  id: string // placed canvas id
  nodeId: number
  label: string
  location: string
}
interface AlarmRow {
  id: number
  nodeId: number
  nodeLabel: string
  severity: string
  logMessage: string
  lastEventTime: number
}

const nodeRows = ref<NodeRow[]>([])
const alarmRows = ref<AlarmRow[]>([])
const applicationRows = ref<TopologyApplication[]>([])
const perspectiveRows = ref<PerspectiveOutage[]>([])

// What is actually on screen, not every node the graph contains: on a large
// discovered topology the tables would otherwise describe thousands of nodes the
// canvas is not drawing, and refetch all of them on every status poll.
const placedRealIds = computed<number[]>(() => store.visibleNodeIds)

/**
 * The nodes behind the current selection, so the tables filter to all of them
 * rather than only to a single pick. A canvas id encodes its node id for placed
 * and one-vertex-per-node discovered graphs; where it cannot (several vertices
 * on one node) the vertex carries it instead, so the discovered graph is
 * consulted as a fallback.
 */
const selectedNodeIds = computed<number[]>(() => {
  const graph = store.discoveredGraph
  const ids = new Set<number>()
  for (const selected of store.selectedIds) {
    const fromId = nodeIdFromPlacedId(selected)
    if (fromId != null) {
      ids.add(fromId)
      continue
    }
    const vertex = graph?.nodes.find(n => n.id === selected)
    if (vertex?.nodeId != null) {
      ids.add(vertex.nodeId)
      continue
    }
    // A vertex that is no node itself (an application) stands for what hangs
    // off it, so selecting one narrows the tables to its children's nodes.
    // One hop, which is the whole subtree for an application-to-service graph.
    if (vertex && graph) {
      for (const link of graph.links) {
        const neighbourId = link.sourceId === vertex.id
          ? link.targetId
          : link.targetId === vertex.id ? link.sourceId : undefined
        const neighbour = neighbourId ? graph.nodes.find(n => n.id === neighbourId) : undefined
        if (neighbour?.nodeId != null) {
          ids.add(neighbour.nodeId)
        }
      }
    }
  }
  return Array.from(ids)
})

/** Applications named by the selection, so the Applications tab narrows too. */
const selectedApplicationIds = computed<string[]>(() => {
  const graph = store.discoveredGraph
  if (!graph) {
    return []
  }
  return store.selectedIds
    .map(id => graph.nodes.find(n => n.id === id)?.properties?.applicationId)
    .filter((id): id is string => id != null)
})

const isFiltered = computed(() => selectedNodeIds.value.length > 0)

/**
 * How many services an application watches, counted from the loaded graph's
 * application-to-service edges. The applications resource does not return them,
 * and the graph is already on hand.
 */
const serviceCountFor = (applicationId: number): number => {
  const graph = store.discoveredGraph
  if (!graph) {
    return 0
  }
  const vertex = graph.nodes.find(n => n.properties?.applicationId === String(applicationId))
  return vertex ? graph.links.filter(l => l.sourceId === vertex.id || l.targetId === vertex.id).length : 0
}

const matchesSelection = (nodeId: number | undefined): boolean =>
  !isFiltered.value || (nodeId != null && selectedNodeIds.value.includes(nodeId))

const filteredNodeRows = computed(() => nodeRows.value
  .filter(r => matchesSelection(r.nodeId))
  // Severity is attached here rather than at fetch time: captured, it froze at
  // whatever the store held when the rows were loaded, so the column contradicted
  // the canvas after every status poll.
  .map(r => ({ ...r, severity: store.severities[r.nodeId] ?? 'NORMAL' })))
const filteredAlarmRows = computed(() => alarmRows.value.filter(r => matchesSelection(r.nodeId)))
const filteredPerspectiveRows = computed(() =>
  perspectiveRows.value.filter(r => matchesSelection(r.nodeId))
)
const filteredApplicationRows = computed(() =>
  selectedApplicationIds.value.length === 0
    ? applicationRows.value
    : applicationRows.value.filter(r => selectedApplicationIds.value.includes(String(r.id)))
)

const onRowSelect = (placedId: string) => emit('select', placedId)

const stripHtml = (s?: string): string => (s ? s.replace(/<[^>]*>/g, '').trim() : '')
const formatTime = (ms?: number): string => {
  if (!ms) {
    return '—'
  }
  const d = new Date(ms)
  return `${d.toLocaleDateString()} ${d.toLocaleTimeString()}`
}

/**
 * One clause per node id, so the query grows with the view. Chunked by encoded
 * length rather than by count: `node.id==` is five bytes longer per clause than
 * `id==`, so a single count cannot keep both inside the request budget, which is
 * how these stayed over the limit after the first attempt at chunking them.
 */
const fetchNodesFor = async (ids: number[]): Promise<NodeRow[]> => {
  const pages = await Promise.all(
    chunkByQueryLength(ids, id => `id==${id}`)
      .map(part => getNodes({ _s: part.map(id => `id==${id}`).join(','), limit: part.length }))
  )
  return pages.flatMap(resp => (resp && resp.node ? resp.node : []).map((n) => {
    const nid = Number(n.id)
    return {
      id: `placed-${nid}`,
      nodeId: nid,
      label: n.label ?? String(nid),
      location: n.location ?? ''
    }
  }))
}

const fetchAlarmsFor = async (ids: number[]): Promise<AlarmRow[]> => {
  const pages = await Promise.all(
    chunkByQueryLength(ids, id => `node.id==${id}`)
      .map(part => getAlarms({ _s: part.map(id => `node.id==${id}`).join(','), limit: 1000 }))
  )
  return pages.flatMap(resp => (resp && resp.alarm ? resp.alarm : []).map(a => ({
    id: Number(a.id),
    nodeId: a.nodeId,
    nodeLabel: a.nodeLabel ?? String(a.nodeId),
    severity: a.severity ?? 'NORMAL',
    logMessage: a.logMessage ?? '',
    lastEventTime: a.lastEventTime
  })))
}

const fetchData = async (): Promise<void> => {
  const ids = placedRealIds.value
  if (ids.length === 0) {
    nodeRows.value = []
    alarmRows.value = []
    applicationRows.value = []
    perspectiveRows.value = []
    return
  }
  loading.value = true
  try {
    const [nodes, alarms, applications, perspectives] = await Promise.all([
      fetchNodesFor(ids),
      fetchAlarmsFor(ids),
      isApplicationGraph.value ? getApplications() : Promise.resolve([]),
      isApplicationGraph.value ? getPerspectiveOutages(ids) : Promise.resolve([])
    ])
    applicationRows.value = applications
    perspectiveRows.value = perspectives
    nodeRows.value = nodes
    alarmRows.value = alarms
  } finally {
    loading.value = false
  }
}

// Fetch when first expanded, whenever the placed-node set or the loaded source
// changes, and on every status refresh. That last one is what keeps the tables
// honest: alarms were fetched once and then left, so the canvas could show a node
// as newly critical while the Alarms tab beside it still did not list the alarm.
watch(
  [collapsed, placedRealIds, isApplicationGraph, () => store.statusRevision],
  ([isCollapsed]) => {
    if (!isCollapsed) {
      void fetchData()
    }
  }
)

// Never leave a tab selected that is no longer rendered, whichever way the set
// of tabs changed.
watch(tabs, (available) => {
  if (!available.some(t => t.key === tab.value)) {
    tab.value = available[0].key
  }
})
</script>

<style scoped>
.topology-explore {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--onms-border-on-surface);
  border-radius: 6px;
  background: var(--onms-surface);
  overflow: hidden;
  max-height: 38vh;
}
.topology-explore.collapsed {
  max-height: none;
}
.te-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.35rem 0.6rem;
  background: rgba(31, 95, 176, 0.10);
  border-bottom: 1px solid var(--onms-border-on-surface);
}
.topology-explore.collapsed .te-header {
  border-bottom: none;
}
.te-toggle {
  border: none;
  background: none;
  font-weight: 600;
  font-size: 0.9rem;
  cursor: pointer;
  color: var(--onms-primary-text-on-surface);
}
.te-caret {
  display: inline-block;
  width: 1em;
}
.te-tabs {
  display: flex;
  gap: 0.25rem;
}
.te-tabs button {
  border: 1px solid transparent;
  background: none;
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.85rem;
  color: var(--onms-primary-text-on-surface);
}
.te-tabs button.active {
  background: rgba(31, 95, 176, 0.10);
  border-color: var(--onms-border-on-surface);
  color: #1f5fb0;
  font-weight: 600;
}
.te-filter {
  margin-left: auto;
  font-size: 0.8rem;
  color: var(--onms-secondary-text-on-surface);
}
.te-body {
  flex: 1 1 auto;
  min-height: 0;
  overflow: auto;
  padding: 0.25rem;
}
.te-empty {
  padding: 0.75rem;
  color: var(--onms-secondary-text-on-surface);
  font-size: 0.85rem;
}
.te-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}
.te-msg {
  display: inline-block;
  max-width: 48ch;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}
</style>
