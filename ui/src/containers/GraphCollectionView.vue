<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>

  <div class="onms-row">
    <div class="onms-col-12">
      <div class="view-header">
        <h2 class="view-title" data-test="ksc-view-title">{{ report?.label ?? 'Graph Collection' }}</h2>
        <OnmsButton
          v-if="report && adminRole"
          variant="outlined"
          label="Edit"
          data-test="ksc-view-edit"
          @click="editorVisible = true"
        />
      </div>
    </div>
  </div>

  <div v-if="loading" class="onms-row">
    <div class="onms-col-12"><OnmsSpinner /></div>
  </div>

  <div v-else-if="!report" class="onms-row">
    <div class="onms-col-12">
      <EmptyList :content="{ msg: 'That graph collection could not be found.' }" data-test="ksc-view-missing" />
    </div>
  </div>

  <div v-else-if="!graphs.length" class="onms-row">
    <div class="onms-col-12">
      <EmptyList :content="{ msg: 'This report has no graphs yet. Use Edit to add some.' }" data-test="ksc-view-empty" />
    </div>
  </div>

  <div v-else class="onms-row">
    <div class="onms-col-12">
      <div class="graph-grid" :style="gridStyle" data-test="ksc-graph-grid">
        <div v-for="graph in graphs" :key="graph.key" class="graph-cell">
          <div class="graph-cell-header">{{ graph.title }}</div>
          <Graph
            v-if="graph.resourceId"
            :definition="graph.graphtype"
            :resourceId="graph.resourceId"
            :time="graph.time"
            :label="graph.key"
            :isSingleGraph="true"
          />
          <div v-else class="graph-cell-note" :data-test="`ksc-graph-unresolved-${graph.index}`">
            This graph is addressed by node/domain rather than a resource id and cannot be rendered here yet. Edit the report to point it at a resource.
          </div>
        </div>
      </div>
    </div>
  </div>

  <GraphCollectionEditorDialog
    v-if="editorVisible && report"
    :report="report"
    @close="editorVisible = false"
    @saved="onSaved"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'

import { OnmsButton, OnmsSpinner } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import Graph from '@/components/Resources/Graph.vue'
import GraphCollectionEditorDialog from '@/components/Ksc/GraphCollectionEditorDialog.vue'
import { decodeResourceId } from '@/components/Ksc/utils/kscResource'
import { kscTimespanToStartEndTime } from '@/components/Ksc/utils/kscTimespan'
import useRole from '@/composables/useRole'
import { useKscStore } from '@/stores/kscStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb, StartEndTime } from '@/types'
import { KscReport } from '@/types/ksc'

const props = defineProps({
  id: {
    required: true,
    type: String
  }
})

const store = useKscStore()
const menuStore = useMenuStore()
const { adminRole } = useRole()

const report = ref<KscReport | null>(null)
const loading = ref(true)
const editorVisible = ref(false)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Graph Collections', to: '/graph-collections' },
  { label: report.value?.label ?? 'Report', to: '#', position: 'last' }
])

interface RenderableGraph {
  key: string
  index: number
  title: string
  graphtype: string
  resourceId: string
  time: StartEndTime
}

const graphs = computed<RenderableGraph[]>(() => {
  if (!report.value) {
    return []
  }
  return (report.value.kscGraph ?? []).map((g, index) => ({
    // Unique per graph so Chart.js canvas ids never collide even when two
    // graphs share a title and graph type.
    key: `ksc-${report.value?.id}-${index}`,
    index,
    title: g.title || g.graphtype,
    graphtype: g.graphtype,
    resourceId: decodeResourceId(g.resourceId),
    time: kscTimespanToStartEndTime(g.timespan)
  }))
})

const columns = computed<number>(() => {
  const perLine = report.value?.graphs_per_line
  return perLine && perLine > 0 ? perLine : 1
})

const gridStyle = computed(() => ({
  gridTemplateColumns: `repeat(${columns.value}, minmax(0, 1fr))`
}))

const loadReport = async () => {
  loading.value = true
  try {
    const numericId = Number(props.id)
    report.value = Number.isFinite(numericId) ? await store.getReport(numericId) : null
  } finally {
    loading.value = false
  }
}

const onSaved = async () => {
  editorVisible.value = false
  await loadReport()
}

watch(() => props.id, loadReport)
onMounted(loadReport)
</script>

<style scoped lang="scss">
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0.5rem 0 1rem;
}

.view-title {
  margin: 0;
  font-size: 1.4rem;
}

.graph-grid {
  display: grid;
  gap: 15px;
}

.graph-cell {
  border: 2px solid var(--p-content-border-color);
  min-width: 0;

  .graph-cell-header {
    text-align: center;
    padding: 6px;
    font-weight: 600;
    background: var(--p-datatable-header-cell-background);
  }

  .graph-cell-note {
    padding: 1rem;
    color: var(--p-text-muted-color);
  }
}
</style>
