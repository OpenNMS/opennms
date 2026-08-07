<template>
  <TableCard class="notices-table">
    <div class="header">
      <div class="card-title" data-test="notices-title">{{ store.title }}</div>
      <div class="header-right">
        <div class="count" data-test="notices-count">{{ store.totalCount }} notice{{ store.totalCount === 1 ? '' : 's' }}</div>
        <OnmsIconButton
          variant="text"
          :icon="DownloadFileIcon"
          iconSize="1.2rem"
          title="Download as CSV"
          aria-label="Download notices as CSV"
          data-test="csv-download-button"
          :disabled="!store.totalCount"
          @click="downloadCsv"
        />
        <OnmsIconButton
          variant="text"
          :icon="PrintIcon"
          iconSize="1.2rem"
          title="Print / save as PDF"
          aria-label="Print notices"
          data-test="print-button"
          :disabled="!store.totalCount"
          @click="printNotices"
        />
      </div>
    </div>

    <OnmsTable
      v-if="store.notices.length"
      lazy
      :value="store.notices"
      :totalRecords="store.totalCount"
      paginator
      dataKey="id"
      :first="store.first"
      :rows="store.rows"
      :rowsPerPageOptions="[10, 20, 50, 100]"
      class="data-table"
      data-test="notices-table"
      @page="onPage"
    >
      <OnmsColumn
        field="id"
        header="ID"
      >
        <template #body="{ data }">
          <a
            :href="`${baseHref}notification/detail.jsp?notice=${data.id}`"
            :data-test="`notice-link-${data.id}`"
          >{{ data.id }}</a>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Severity">
        <template #body="{ data }">
          <OnmsTag
            v-if="data.severity"
            :value="data.severity"
            :severity="severityMap[String(data.severity).toLowerCase()] ?? 'secondary'"
          />
          <span v-else>-</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Subject">
        <template #body="{ data }">
          <span :title="data.textMessage ?? ''">{{ truncate(data.subject || data.textMessage || '-') }}</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Sent Time">
        <template #body="{ data }">
          {{ formatTime(data.pageTime) }}
        </template>
      </OnmsColumn>
      <OnmsColumn header="Node">
        <template #body="{ data }">
          <a
            v-if="data.nodeId"
            :href="`${baseHref}element/node.jsp?node=${data.nodeId}`"
          >{{ data.nodeLabel ?? data.nodeId }}</a>
          <span v-else>-</span>
        </template>
      </OnmsColumn>
      <OnmsColumn header="Interface">
        <template #body="{ data }">
          {{ data.ipAddress ?? '-' }}
        </template>
      </OnmsColumn>
      <OnmsColumn header="Service">
        <template #body="{ data }">
          {{ data.serviceType?.name ?? '-' }}
        </template>
      </OnmsColumn>
      <OnmsColumn
        v-if="showAckColumns"
        header="Responder"
      >
        <template #body="{ data }">
          {{ data.ackUser ?? '-' }}
        </template>
      </OnmsColumn>
      <OnmsColumn
        v-if="showAckColumns"
        header="Respond Time"
      >
        <template #body="{ data }">
          {{ formatTime(data.ackTime) }}
        </template>
      </OnmsColumn>
      <OnmsColumn
        v-if="!showAckColumns && canAcknowledgeNotifications"
        header="Actions"
      >
        <template #body="{ data }">
          <OnmsButton
            variant="text"
            label="Acknowledge"
            :aria-label="`Acknowledge notice ${data.id}`"
            :data-test="`ack-button-${data.id}`"
            :disabled="store.loading"
            @click="store.acknowledge(data)"
          />
        </template>
      </OnmsColumn>
    </OnmsTable>

    <div v-if="!store.notices.length">
      <EmptyList
        :content="emptyListContent"
        data-test="empty-list"
      />
    </div>
  </TableCard>
</template>

<script setup lang="ts">
import { computed } from 'vue'

import { OnmsIconButton, OnmsButton, OnmsColumn, OnmsTable, OnmsTag, type OnmsTablePageEvent, type OnmsTagSeverity } from '@opennms/onms-ui'

import EmptyList from '@/components/Common/EmptyList.vue'
import TableCard from '@/components/Common/TableCard.vue'
import DownloadFileIcon from '@/components/icons/action/DownloadFile.vue'
import PrintIcon from '@/components/icons/action/Print.vue'
import useSnackbar from '@/composables/useSnackbar'
import useRole from '@/composables/useRole'
import { useMenuStore } from '@/stores/menuStore'
import { MessageSeverity } from '@/types'
import { useNoticesStore } from '@/stores/noticesStore'
import { OnmsNotice } from '@/types/notices'

const { showSnackBar } = useSnackbar()
const { canAcknowledgeNotifications } = useRole()
const menuStore = useMenuStore()
const store = useNoticesStore()

// Cap for export/print fetches; the table itself stays server-paginated.
const EXPORT_LIMIT = 1000

const baseHref = computed<string>(() => menuStore.mainMenu.baseHref)
const showAckColumns = computed<boolean>(() => store.preset === 'allAcknowledged')

const emptyListContent = computed(() => ({
  msg: store.preset === 'allAcknowledged' ? 'No acknowledged notices found.' : 'No outstanding notices found.'
}))

const severityMap: Record<string, OnmsTagSeverity> = {
  critical: 'danger',
  major: 'danger',
  minor: 'warn',
  warning: 'warn',
  normal: 'success',
  cleared: 'success',
  indeterminate: 'secondary'
}

const truncate = (text: string, max = 90) => {
  return text.length > max ? `${text.slice(0, max)}…` : text
}

const formatTime = (value?: number | string | null) => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  const date = new Date(value)
  return isNaN(date.getTime()) ? '-' : date.toLocaleString()
}

const onPage = (event: OnmsTablePageEvent) => {
  store.onPage(event.first, event.rows)
}

const fetchAllForExport = async (): Promise<{ notices: OnmsNotice[], totalCount: number }> => {
  // delegate to the store so the same whoami guard as load() applies
  const result = await store.fetchForExport(EXPORT_LIMIT)
  if (result.totalCount > result.notices.length) {
    showSnackBar({
      msg: `Only the first ${result.notices.length} of ${result.totalCount} notices were exported.`,
      severity: MessageSeverity.Warn
    })
  }
  return result
}

const exportColumns = (notice: OnmsNotice): Record<string, string> => ({
  'ID': String(notice.id),
  'Severity': notice.severity ?? '',
  'Subject': notice.subject ?? '',
  'Text Message': notice.textMessage ?? '',
  'Sent Time': formatTime(notice.pageTime),
  'Node': notice.nodeLabel ?? '',
  'Interface': notice.ipAddress ?? '',
  'Service': notice.serviceType?.name ?? '',
  'Responder': notice.ackUser ?? '',
  'Respond Time': formatTime(notice.ackTime)
})

const downloadCsv = async () => {
  const { notices } = await fetchAllForExport()
  if (!notices.length) {
    return
  }
  const rows = notices.map(exportColumns)
  const headers = Object.keys(rows[0])
  // a leading = + - @ or tab would execute as a formula when the CSV is
  // opened in a spreadsheet; notice text derives from event data, which can
  // be externally influenced (traps), so neutralise it
  const escapeCell = (value: string) => {
    const guarded = /^[=+\-@\t\r]/.test(value) ? `'${value}` : value
    return `"${guarded.replaceAll('"', '""')}"`
  }
  const csv = [
    headers.map(escapeCell).join(','),
    ...rows.map(row => headers.map(h => escapeCell(row[h] === '-' ? '' : row[h])).join(','))
  ].join('\r\n')
  const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `notices-${store.preset}-${new Date().toISOString().slice(0, 19).replaceAll(':', '-')}.csv`
  link.click()
  URL.revokeObjectURL(link.href)
}

const printNotices = async () => {
  const { notices, totalCount } = await fetchAllForExport()
  if (!notices.length) {
    return
  }
  const rows = notices.map(exportColumns)
  const headers = Object.keys(rows[0])
  const escapeHtml = (value: string) =>
    value.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
  const html = `<!DOCTYPE html>
<html>
<head>
<title>${escapeHtml(store.title)}</title>
<style>
  body { font-family: -apple-system, 'Segoe UI', Roboto, sans-serif; margin: 24px; color: #222; }
  h1 { font-size: 18px; margin: 0 0 4px 0; }
  .meta { color: #666; font-size: 12px; margin-bottom: 16px; }
  table { border-collapse: collapse; width: 100%; font-size: 12px; }
  th, td { border: 1px solid #ccc; padding: 6px 8px; text-align: left; vertical-align: top; }
  th { background: #f0f0f5; }
  @media print { body { margin: 8px; } }
</style>
</head>
<body>
<h1>${escapeHtml(store.title)}</h1>
<div class="meta">OpenNMS Horizon — generated ${escapeHtml(new Date().toLocaleString())} — ${rows.length < totalCount ? `first ${rows.length} of ${totalCount}` : rows.length} notice${totalCount === 1 ? '' : 's'}</div>
<table>
<thead><tr>${headers.map(h => `<th>${escapeHtml(h)}</th>`).join('')}</tr></thead>
<tbody>${rows.map(row => `<tr>${headers.map(h => `<td>${escapeHtml(row[h])}</td>`).join('')}</tr>`).join('')}</tbody>
</table>
</body>
</html>`
  const printWindow = window.open('', '_blank')
  if (!printWindow) {
    return
  }
  printWindow.document.write(html)
  printWindow.document.close()
  printWindow.focus()
  printWindow.print()
}
</script>

<style lang="scss" scoped>
.notices-table {
  padding: 25px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;

  .header-right {
    display: flex;
    align-items: center;
    gap: 0.25rem;
  }
}

.card-title {
  font-size: 1.1rem;
  font-weight: 600;
}

.count {
  color: var(--p-text-muted-color);
  font-size: 0.9rem;
}
</style>
