<template>
  <div class="onms-row">
    <div class="onms-col-12">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
  </div>
  <div class="onms-row">
    <div class="onms-col-12">
      <TableCard class="ksc-card">
        <div class="header">
          <div class="card-title" data-test="ksc-title">Graph Collections</div>
          <div class="header-right">
            <FormField class="search-input">
              <OnmsInputText
                placeholder="Search"
                aria-label="Search reports"
                :modelValue="search"
                data-test="ksc-search"
                @update:modelValue="(val) => (search = String(val ?? ''))"
              />
            </FormField>
            <OnmsButton
              variant="outlined"
              label="Reload Config"
              title="Re-read ksc-performance-reports.xml from disk"
              data-test="ksc-reload"
              :disabled="store.loading"
              @click="store.reloadConfig()"
            />
            <OnmsButton
              v-if="adminRole"
              label="New Report"
              data-test="ksc-new"
              @click="openCreate"
            />
          </div>
        </div>

        <OnmsTable
          :value="filteredReports"
          dataKey="id"
          sortField="label"
          :sortOrder="1"
          class="data-table"
          data-test="ksc-table"
        >
          <OnmsColumn
            field="label"
            header="Title"
            sortable
          >
            <template #body="{ data }">
              <router-link :to="`/graph-collections/${data.id}`" :data-test="`ksc-view-link-${data.id}`">{{ data.label }}</router-link>
            </template>
          </OnmsColumn>
          <OnmsColumn header="Graphs">
            <template #body="{ data }">
              {{ data.kscGraph?.length ?? 0 }}
            </template>
          </OnmsColumn>
          <OnmsColumn header="Actions">
            <template #body="{ data }">
              <div class="row-actions">
                <OnmsButton
                  variant="text"
                  label="View"
                  :aria-label="`View ${data.label}`"
                  :data-test="`ksc-view-${data.id}`"
                  @click="view(data)"
                />
                <OnmsButton
                  v-if="adminRole"
                  variant="text"
                  label="Edit"
                  :aria-label="`Edit ${data.label}`"
                  :data-test="`ksc-edit-${data.id}`"
                  @click="openEdit(data)"
                />
                <OnmsButton
                  v-if="adminRole"
                  variant="text"
                  label="Duplicate"
                  :aria-label="`Create a new report from ${data.label}`"
                  :data-test="`ksc-duplicate-${data.id}`"
                  @click="duplicate(data)"
                />
                <OnmsButton
                  v-if="adminRole"
                  variant="text"
                  label="Delete"
                  :aria-label="`Delete ${data.label}`"
                  :data-test="`ksc-delete-${data.id}`"
                  @click="confirmDelete(data)"
                />
              </div>
            </template>
          </OnmsColumn>
          <template #empty>
            <EmptyList :content="emptyContent" data-test="ksc-empty" />
          </template>
        </OnmsTable>
      </TableCard>
    </div>
  </div>

  <GraphCollectionEditorDialog
    v-if="editorVisible"
    :report="editing"
    @close="editorVisible = false"
    @saved="onSaved"
  />

  <OnmsConfirmationDialog
    v-if="pendingDelete"
    :visible="true"
    title="Delete report"
    actionButtonText="Delete"
    cancelButtonText="Cancel"
    data-test="ksc-delete-confirm"
    @ok="doDelete"
    @cancel="pendingDelete = null"
  >
    <template #content>
      Delete the report "{{ pendingDelete.label }}"? This cannot be undone.
    </template>
  </OnmsConfirmationDialog>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { OnmsButton, OnmsColumn, OnmsConfirmationDialog, OnmsInputText, OnmsTable } from '@opennms/onms-ui'

import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import EmptyList from '@/components/Common/EmptyList.vue'
import FormField from '@/components/Common/FormField.vue'
import TableCard from '@/components/Common/TableCard.vue'
import GraphCollectionEditorDialog from '@/components/Ksc/GraphCollectionEditorDialog.vue'
import useRole from '@/composables/useRole'
import { useKscStore } from '@/stores/kscStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'
import { KscReport } from '@/types/ksc'

const store = useKscStore()
const menuStore = useMenuStore()
const router = useRouter()
const { adminRole } = useRole()

const search = ref('')
const editorVisible = ref(false)
const editing = ref<KscReport | null>(null)
const pendingDelete = ref<KscReport | null>(null)

const homeUrl = computed<string>(() => menuStore.mainMenu.homeUrl)

const breadcrumbs = computed<BreadCrumb[]>(() => [
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: true },
  { label: 'Graph Collections', to: '#', position: 'last' }
])

const filteredReports = computed<KscReport[]>(() => {
  const term = search.value.trim().toLowerCase()
  if (!term) {
    return store.reports
  }
  return store.reports.filter(r => (r.label ?? '').toLowerCase().includes(term))
})

const emptyContent = computed(() => ({
  msg: search.value ? 'No reports match your search.' : 'No graph collections are defined yet.'
}))

const view = (report: KscReport) => {
  router.push(`/graph-collections/${report.id}`)
}

const openCreate = () => {
  editing.value = null
  editorVisible.value = true
}

const openEdit = async (report: KscReport) => {
  // The list may be summarized; always edit against the full report so Save
  // can't overwrite graphs the editor never loaded.
  editing.value = (report.id !== null ? await store.getReport(report.id) : null) ?? report
  editorVisible.value = true
}

const duplicate = async (report: KscReport) => {
  if (report.id === null) {
    return
  }
  const full = await store.getReport(report.id)
  if (!full) {
    return
  }
  // Clone into a new (id-less) report so Save creates a fresh one, carrying the
  // graphs and layout over — the legacy "Create from Existing".
  editing.value = {
    id: null,
    label: `${full.label} (copy)`,
    show_timespan_button: full.show_timespan_button,
    show_graphtype_button: full.show_graphtype_button,
    graphs_per_line: full.graphs_per_line,
    kscGraph: (full.kscGraph ?? []).map(g => ({ ...g }))
  }
  editorVisible.value = true
}

const onSaved = () => {
  editorVisible.value = false
}

const confirmDelete = (report: KscReport) => {
  pendingDelete.value = report
}

const doDelete = async () => {
  const target = pendingDelete.value
  pendingDelete.value = null
  if (target) {
    await store.removeReport(target)
  }
}

onMounted(() => store.load())
</script>

<style scoped lang="scss">
.ksc-card {
  padding: 25px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  gap: 1rem;
  flex-wrap: wrap;

  .header-right {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }

  .search-input {
    width: 220px;
  }
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.row-actions {
  display: flex;
  gap: 0.25rem;
}
</style>
