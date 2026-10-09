<template>
  <TableCard class="snmp-data-collection-definition-search">
    <p class="intro">
      Find the source that defines a system definition, MIB group or resource type.
      A name can exist in more than one source.
    </p>
    <div class="search-row">
      <FormField
        label="Type"
        :for="kindId"
        class="kind-field"
      >
        <OnmsSelect
          :inputId="kindId"
          :modelValue="kind"
          @update:modelValue="onChangeKind"
          :options="KIND_OPTIONS"
          optionLabel="label"
          optionValue="value"
          data-test="definition-kind-select"
          fluid
        />
      </FormField>
      <FormField
        label="Exact Name"
        :for="nameId"
        class="name-field"
      >
        <OnmsAutoComplete
          :inputId="nameId"
          v-model="name"
          :suggestions="suggestions"
          @complete="onComplete"
          @optionSelect="search"
          @keydown.enter="search"
          placeholder="For example, mib2-interfaces"
          data-test="definition-name-input"
          fluid
        />
      </FormField>
      <OnmsButton
        label="Search"
        class="search-button"
        :disabled="!name.trim()"
        data-test="definition-search-button"
        @click="search"
      />
    </div>

    <OnmsTable
      v-if="results.length"
      :value="results"
      dataKey="key"
      class="data-table"
      data-test="definition-results-table"
    >
      <OnmsColumn
        field="name"
        header="Name"
      />
      <OnmsColumn
        field="sourceName"
        header="Source"
      />
      <OnmsColumn
        field="details"
        header="Details"
      />
      <OnmsColumn header="Status">
        <template #body="{ data }">
          <OnmsTag
            :class="data.enabled ? 'enabled-tag' : 'disabled-tag'"
            :value="data.enabled ? 'Enabled' : 'Disabled'"
            data-test="status-tag"
          />
        </template>
      </OnmsColumn>
      <OnmsColumn header="Actions">
        <template #body="{ data }">
          <OnmsIconButton
            :title="`View ${data.name} in source ${data.sourceName}`"
            data-test="view-in-source-button"
            :icon="ViewDetails"
            @click="openInSource(data)"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <EmptyList
      v-else-if="searchedName"
      :content="{ msg: `No source defines a ${kindNoun} named '${searchedName}'.` }"
      data-test="empty-list"
    />
  </TableCard>
</template>

<script lang="ts" setup>
import { computed, ref, useId } from 'vue'
import { useRouter } from 'vue-router'

import { OnmsAutoComplete, OnmsButton, OnmsColumn, OnmsIconButton, OnmsSelect, OnmsTable, OnmsTag } from '@opennms/onms-ui'
import ViewDetails from '@opennms/onms-ui/icons/action/ViewDetails.vue'
import FormField from '@/components/Common/FormField.vue'
import useSnackbar from '@/composables/useSnackbar'
import {
  findMibGroupsByName,
  findResourceTypesByName,
  findSystemDefsByName,
  getAllMibGroupNames,
  getAllResourceTypeNames,
  getAllSystemDefNames
} from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import EmptyList from '../Common/EmptyList.vue'
import TableCard from '../Common/TableCard.vue'

type DefinitionKind = 'systemdefs' | 'mibgroups' | 'resourcetypes'

interface DefinitionRow {
  key: string
  kind: DefinitionKind
  name: string
  sourceId: number
  sourceName: string
  enabled: boolean
  details: string
}

const KIND_OPTIONS: { label: string, noun: string, value: DefinitionKind }[] = [
  { label: 'System Definition', noun: 'system definition', value: 'systemdefs' },
  { label: 'MIB Group', noun: 'MIB group', value: 'mibgroups' },
  { label: 'Resource Type', noun: 'resource type', value: 'resourcetypes' }
]

// The resource type names endpoint also returns the instance values "0" and
// "ifIndex". No source defines them as resource types.
const INSTANCE_ONLY_NAMES = new Set(['0', 'ifIndex'])

// Tab index of each kind on the source detail page.
const DETAIL_TAB: Record<DefinitionKind, number> = { systemdefs: 0, mibgroups: 1, resourcetypes: 2 }

const router = useRouter()
const snackbar = useSnackbar()
const detailStore = useSnmpDataCollectionDetailStore()
const kindId = useId()
const nameId = useId()

const kind = ref<DefinitionKind>('mibgroups')
const name = ref('')
const searchedName = ref('')
const results = ref<DefinitionRow[]>([])
const suggestions = ref<string[]>([])
const allNames = ref<Partial<Record<DefinitionKind, string[]>>>({})
// Incremented for each search and at each type change. A response is shown
// only if no newer search or type change happened while it was pending.
let latestRequest = 0
// Incremented for each name completion. Only the newest completion sets the suggestions.
let latestCompletion = 0

const kindNoun = computed(() => KIND_OPTIONS.find(o => o.value === kind.value)?.noun ?? '')

const onChangeKind = (value: unknown) => {
  latestRequest++
  kind.value = value as DefinitionKind
  results.value = []
  searchedName.value = ''
}

const loadNames = async (k: DefinitionKind): Promise<string[]> => {
  if (!allNames.value[k]) {
    const names = k === 'systemdefs'
      ? await getAllSystemDefNames()
      : k === 'mibgroups' ? await getAllMibGroupNames() : await getAllResourceTypeNames()
    const definitionNames = k === 'resourcetypes' ? names.filter(n => !INSTANCE_ONLY_NAMES.has(n)) : names
    allNames.value[k] = [...new Set(definitionNames)].sort()
  }
  return allNames.value[k] ?? []
}

const onComplete = async (rawQuery: string) => {
  const completion = ++latestCompletion
  const requestedKind = kind.value
  try {
    const query = (rawQuery ?? '').trim().toLowerCase()
    const names = await loadNames(requestedKind)
    if (completion !== latestCompletion || requestedKind !== kind.value) {
      return
    }
    suggestions.value = names.filter(n => n.toLowerCase().includes(query)).slice(0, 50)
  } catch (_e) {
    if (completion === latestCompletion) {
      suggestions.value = []
    }
  }
}

const findRows = async (k: DefinitionKind, term: string): Promise<DefinitionRow[]> => {
  if (k === 'systemdefs') {
    return (await findSystemDefsByName(term)).map(d => ({
      key: `sd-${d.id}`,
      kind: k,
      name: d.name,
      sourceId: d.collectionSourceId,
      sourceName: d.collectionSourceName,
      enabled: d.enabled,
      details: [d.sysoid ? `sysoid ${d.sysoid}` : `sysoidMask ${d.sysoidMask}`,
        `${(d.mibGroupNames ?? []).length} MIB group(s)`].join(', ')
    }))
  }
  if (k === 'mibgroups') {
    return (await findMibGroupsByName(term)).map(g => ({
      key: `mg-${g.id}`,
      kind: k,
      name: g.name,
      sourceId: g.collectionSourceId,
      sourceName: g.collectionSourceName,
      enabled: g.enabled,
      details: `ifType ${g.ifType ?? '(none)'}`
    }))
  }
  return (await findResourceTypesByName(term)).map(r => ({
    key: `rt-${r.id}`,
    kind: k,
    name: r.name,
    sourceId: r.collectionSourceId,
    sourceName: r.collectionSourceName,
    enabled: r.enabled,
    details: `label ${r.label}`
  }))
}

const search = async () => {
  const term = name.value.trim()
  if (!term) {
    return
  }
  const request = ++latestRequest
  try {
    const rows = await findRows(kind.value, term)
    if (request !== latestRequest) {
      return
    }
    results.value = rows
    searchedName.value = term
  } catch (_e) {
    if (request !== latestRequest) {
      return
    }
    results.value = []
    searchedName.value = ''
    snackbar.showSnackBar({ msg: `Failed to search for '${term}'.`, error: true })
  }
}

// Open the source on the matching tab, filtered to this name.
const openInSource = (row: DefinitionRow) => {
  detailStore.activeTab = DETAIL_TAB[row.kind]
  if (row.kind === 'systemdefs') {
    detailStore.systemDefsSearchTerm = row.name
    detailStore.systemDefsPagination.page = 1
  } else if (row.kind === 'mibgroups') {
    detailStore.mibGroupsSearchTerm = row.name
    detailStore.mibGroupsPagination.page = 1
  } else {
    detailStore.resourceTypesSearchTerm = row.name
    detailStore.resourceTypesPagination.page = 1
  }
  router.push({ name: 'SNMP Data Collection Source Detail', params: { id: row.sourceId }})
}
</script>

<style lang="scss" scoped>
.snmp-data-collection-definition-search {
  margin-top: 10px;
  padding: 25px;
  border: 1px solid var(--p-content-border-color);

  .intro {
    margin-bottom: 16px;
  }

  .search-row {
    display: flex;
    align-items: flex-end;
    gap: 12px;
    margin-bottom: 20px;

    .kind-field {
      width: 220px;
    }

    .name-field {
      width: 400px;
    }
  }

  .enabled-tag {
    border-radius: 4px;
    background-color: #0B720C1F;

    :deep(.p-tag-label) {
      color: #0B720C !important;
    }
  }

  .disabled-tag {
    border-radius: 4px;
    background-color: #7575751F;

    :deep(.p-tag-label) {
      color: #757575 !important;
    }
  }
}
</style>
