<template>
  <OnmsDialog
    :visible="visible"
    :header="`Manage Nodes: ${categoryName}`"
    class="category-nodes-dialog"
    width="min(820px, 95vw)"
    data-test="category-nodes-dialog"
    @update:visible="onVisible"
  >
    <p class="hint">
      Search the node inventory and toggle each node in or out of this category.
      Changes are applied immediately against the category membership API.
    </p>

    <div class="controls">
      <OnmsSearchInput
        v-model="searchTerm"
        class="search"
        placeholder="Search nodes by label"
        aria-label="Search nodes by label"
        dataTest="node-search"
      />
      <label v-if="anyRequisitioned" class="allow-req" data-test="allow-requisitioned">
        <OnmsCheckbox v-model="allowRequisitioned" />
        <span>Allow editing requisitioned nodes</span>
      </label>
    </div>

    <p v-if="anyRequisitioned" class="warn" data-test="requisitioned-warning">
      Nodes marked <em>(requisitioned)</em> are managed by a requisition — category
      changes on them are overwritten on the next provisioning synchronization, so
      they are locked. Tick the box above only if you intend to override them.
    </p>

    <OnmsTable
      lazy
      :value="nodes"
      :totalRecords="totalRecords"
      :loading="loading"
      paginator
      :rows="rows"
      :first="first"
      :rowsPerPageOptions="[10, 20, 50]"
      dataKey="id"
      class="data-table"
      data-test="nodes-table"
      @page="onPage"
    >
      <template #empty>
        <div class="empty" data-test="nodes-empty">
          {{ loadError ? 'Could not load nodes. Try again.' : 'No nodes match your search.' }}
        </div>
      </template>
      <OnmsColumn header="Node">
        <template #body="{ data }">
          <span class="node-label">{{ data.label }}</span>
          <span v-if="data.requisitioned" class="req-tag">(requisitioned)</span>
        </template>
      </OnmsColumn>
      <OnmsColumn field="location" header="Location" />
      <OnmsColumn header="In this category" style="width: 10rem">
        <template #body="{ data }">
          <OnmsToggleSwitch
            :modelValue="data.isMember"
            :disabled="isLocked(data) || busyIds.has(data.id)"
            :aria-label="`${data.isMember ? 'Remove' : 'Add'} ${data.label}`"
            :data-test="`member-toggle-${data.id}`"
            @update:modelValue="(value: boolean) => toggle(data, value)"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <template #footer>
      <OnmsButton label="Close" data-test="close-button" @click="onVisible(false)" />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { debounce } from 'lodash'

import {
  OnmsButton,
  OnmsCheckbox,
  OnmsColumn,
  OnmsDialog,
  OnmsSearchInput,
  OnmsTable,
  OnmsToggleSwitch
} from '@opennms/onms-ui'

import API from '@/services'
import { SORT } from '@/types'

interface NodeRow {
  id: number
  label: string
  location: string
  requisitioned: boolean
  isMember: boolean
}

const props = defineProps<{
  visible: boolean
  categoryId: number | null
  categoryName: string
}>()

const emit = defineEmits(['update:visible'])

const nodes = ref<NodeRow[]>([])
const totalRecords = ref(0)
const loading = ref(false)
const loadError = ref(false)
const first = ref(0)
const rows = ref(10)
const searchTerm = ref('')
const allowRequisitioned = ref(false)
const busyIds = ref<Set<number>>(new Set())
let loadToken = 0

const anyRequisitioned = computed(() => nodes.value.some(n => n.requisitioned))
const isLocked = (row: NodeRow) => row.requisitioned && !allowRequisitioned.value

// only allow a-z0-9 and a few safe chars into the FIQL label search
const sanitize = (term: string) => term.replace(/[^\w.\-* ]/g, '').trim()

const load = async () => {
  if (props.categoryId === null) {
    return
  }
  const token = ++loadToken
  loading.value = true
  loadError.value = false
  const term = sanitize(searchTerm.value)
  const params: Record<string, unknown> = {
    limit: rows.value,
    offset: first.value,
    orderBy: 'label',
    order: SORT.ASCENDING
  }
  if (term) {
    params._s = `label==*${term}*`
  }
  const resp = await API.getNodes(params as any)
  if (token !== loadToken) {
    return
  }
  if (resp === false) {
    nodes.value = []
    totalRecords.value = 0
    loadError.value = true
    loading.value = false
    return
  }
  nodes.value = (resp.node ?? []).map((n: any) => ({
    id: Number(n.id),
    label: n.label ?? `Node ${n.id}`,
    location: n.location ?? '-',
    requisitioned: !!n.foreignSource,
    isMember: (n.categories ?? []).some((c: any) => Number(c.id) === props.categoryId)
  }))
  totalRecords.value = resp.totalCount ?? nodes.value.length
  loading.value = false
}

const debouncedSearch = debounce(() => {
  first.value = 0
  load()
}, 300)

watch(searchTerm, () => debouncedSearch())

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible && props.categoryId !== null) {
      // reset per-open so a reused dialog never shows the previous category
      first.value = 0
      searchTerm.value = ''
      allowRequisitioned.value = false
      nodes.value = []
      load()
    }
  }
)

const onPage = (event: { first: number; rows: number }) => {
  first.value = event.first
  rows.value = event.rows
  load()
}

const toggle = async (row: NodeRow, target: boolean) => {
  if (busyIds.value.has(row.id) || isLocked(row)) {
    return
  }
  row.isMember = target // optimistic
  busyIds.value = new Set(busyIds.value).add(row.id)
  const ok = target
    ? await API.addNodeToCategory(props.categoryName, row.id)
    : await API.removeNodeFromCategory(props.categoryName, row.id)
  if (!ok) {
    row.isMember = !target // revert; the service already surfaced the error
  }
  const next = new Set(busyIds.value)
  next.delete(row.id)
  busyIds.value = next
}

const onVisible = (value: boolean) => emit('update:visible', value)
</script>

<style lang="scss" scoped>
.hint {
  margin: 0 0 0.75rem 0;
  font-size: 0.875rem;
  color: var(--p-text-muted-color);
}

.controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.75rem;
  flex-wrap: wrap;

  .search {
    flex: 1;
    min-width: 220px;
  }

  .allow-req {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    font-size: 0.875rem;
    white-space: nowrap;
  }
}

.warn {
  margin: 0 0 0.75rem 0;
  padding: 0.5rem 0.75rem;
  font-size: 0.85rem;
  border-radius: 4px;
  background: var(--p-yellow-50, #fffbeb);
  border: 1px solid var(--p-yellow-200, #fde68a);
  color: var(--p-yellow-800, #854d0e);
}

.node-label {
  margin-right: 0.4rem;
}

.req-tag {
  font-size: 0.75rem;
  color: var(--p-text-muted-color);
  font-style: italic;
}

.empty {
  padding: 1.5rem;
  text-align: center;
  color: var(--p-text-muted-color);
}
</style>
