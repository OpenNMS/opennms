<template>
  <div class="usage-stats-table">
    <PDataTable
      :value="tableData"
      sortField="key"
      :sortOrder="1"
      stripedRows
      size="large"
      scrollable
      scrollHeight="calc(100vh - 310px)"
      tableStyle="table-layout: fixed; width: 100%"
      aria-label="Usage Statistics Sharing"
    >
      <PColumn
        field="name"
        header="Name"
        sortable
        style="width: 20%"
        :pt="columnHeaderPt"
      />
      <PColumn
        field="key"
        header="Key name"
        sortable
        style="width: 20%"
        :pt="columnHeaderPt"
      />
      <PColumn
        field="description"
        header="Description"
        sortable
        style="width: 40%"
        :pt="columnHeaderPt"
      />
      <PColumn
        field="latestValue"
        header="Latest value"
        sortable
        style="width: 20%"
        :pt="columnHeaderPt"
      >
        <template #body="{ data }">
          <a
            v-if="data.isLink"
            href="#"
            @click.prevent="() => showFullValue(data)"
          >See full value</a>
          <template v-else-if="shouldClipValue(data)">
            {{ getClippedValue(data) }}
            <a
              href="#"
              @click.prevent="() => showFullValue(data)"
            >See full value</a>
          </template>
          <template v-else>{{ data.latestValue }}</template>
        </template>
      </PColumn>
    </PDataTable>
  </div>
  <MessageDialog
    :visible="showValueModalVisible"
    :title="showValueModalSubtitle ? `Usage Statistics: ${showValueModalSubtitle}` : 'Usage Statistics'"
    max-height="500px"
    max-width="550px"
    @close="showValueModalVisible = false"
  >
    <template #content>
      <div v-if="showValueModalVisible">
        <div class="full-value-wrapper">
          <div class="full-value-contents">
            {{ showValueModalContent }}
          </div>
        </div>
      </div>
    </template>
  </MessageDialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

import { isNumber, isString } from '@/lib/utils'
import { useUsageStatisticsStore } from '@/stores/usageStatisticsStore'
import {
  UsageStatisticsData,
  UsageStatisticsMetadata,
  UsageStatisticsMetadataItem
} from '@/types/usageStatistics'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import MessageDialog from '@/components/Common/MessageDialog.vue'

const PDataTable = DataTable
const PColumn = Column

// PrimeVue Column doesn't emit scope="col" on the header <th>; restore it via
// the passthrough so the header cells stay associated with their columns for
// screen readers (the FeatherDS table set scope="col" per column).
const columnHeaderPt = { headerCell: { scope: 'col' }}

interface StatisticsItem {
  // this is just for sorting
  [b: string]: string | boolean
  key: string
  name: string
  description: string
  isLink: boolean
  latestValue: string
}

const STRING_CLIP_LENGTH = 100

const usageStatisticsStore = useUsageStatisticsStore()

const showValueModalContent = ref('')
const showValueModalSubtitle = ref('')
const showValueModalVisible = ref(false)

const statistics = computed<UsageStatisticsData>(() => usageStatisticsStore.statistics )
const metadata = computed<UsageStatisticsMetadata>(() => usageStatisticsStore.metadata )

const metadataMap = computed<Map<string, UsageStatisticsMetadataItem>>(() => {
  const map = new Map<string, UsageStatisticsMetadataItem>()

  for (const obj of metadata.value.metadata) {
    const item = {
      key: obj.key,
      name: obj.name || '',
      description: obj.description || '',
      datatype: obj.datatype || 'string'
    } as UsageStatisticsMetadataItem

    map.set(obj.key, item)
  }

  return map
})

// Row data. Sorting/striping/sticky-header are handled by the DataTable.
const tableData = computed<StatisticsItem[]>(() => {
  const items = [] as StatisticsItem[]

  if (statistics.value && metadata.value) {
    for (const key of Object.keys(statistics.value)) {
      const statsValue = statistics.value[key]
      const metaItem = metadataMap.value.get(key)

      const { isLink, latestValue } = getLatestValue(statsValue, metaItem)

      items.push({
        key,
        name: metaItem?.name || '',
        description: metaItem?.description || '',
        isLink,
        latestValue
      } as StatisticsItem)
    }
  }

  return items
})

const getLatestValue = (statsValue: any, metaItem: UsageStatisticsMetadataItem | undefined) => {
  let latestValue = ''
  let isLink = false
  const datatype = metaItem?.datatype || ''

  // use hints from metadata if possible
  if (datatype) {
    if (datatype === 'string') {
      latestValue = (statsValue as string) || '--'
    } else if (datatype === 'boolean') {
      latestValue = statsValue && statsValue === true ? 'Yes' : 'No'
    } else if (datatype === 'number') {
      latestValue = new Intl.NumberFormat().format(statsValue as number)
    } else if (datatype === 'object') {
      isLink = true
    }
  } else {
    // fallback if metadata entry not found
    if (isString(statsValue)) {
      latestValue = (statsValue as string) || '--'
    } else if (isNumber(statsValue)) {
      latestValue = new Intl.NumberFormat().format(statsValue as number)
    } else {
      isLink = true
    }
  }

  return {
    isLink,
    latestValue
  }
}

const shouldClipValue = (row: StatisticsItem) => {
  return !row.isLink && row.latestValue && row.latestValue.length > STRING_CLIP_LENGTH
}

const getClippedValue = (row: StatisticsItem) => {
  return `${row.latestValue.substring(0, STRING_CLIP_LENGTH)}...`
}

const showFullValue = (item: StatisticsItem) => {
  const obj = statistics.value[item.key] || {}

  const value = {
    [item.key]: obj
  }

  const json = JSON.stringify(value, null, 2)

  // show and populate modal
  showValueModalSubtitle.value = item.name || item.key || ''
  showValueModalContent.value = json
  showValueModalVisible.value = true
}
</script>

<style lang="scss" scoped>
.usage-stats-table {
  // Fixed column widths (set per-column) with wrapping cell contents, so a long
  // Description doesn't push the Latest value column off-screen.
  :deep(.p-datatable-tbody > tr > td) {
    white-space: normal;
    word-break: break-word;
  }
}

.full-value-wrapper {
  min-height: 300px;
  min-width: 550px;
  max-width: 660px;
}

.full-value-contents {
  display: block;
  font-family: monospace;
  white-space: pre;
  margin: 0;
}
</style>
