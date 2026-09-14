<template>
  <OnmsTable
    lazy
    :value="eventStore.events"
    paginator
    :rows="pageSize"
    :first="first"
    :totalRecords="eventStore.totalCount"
    :rowsPerPageOptions="[5, 10, 20, 50]"
    data-test="events-table"
    @page="onPage"
  >
    <OnmsColumn field="id" header="Id">
      <template #body="{ data }">
        <router-link :to="`/event/${data.id}`">{{ data.id }}</router-link>
      </template>
    </OnmsColumn>
    <OnmsColumn field="createTime" header="Created">
      <template #body="{ data }">
        <span v-date>{{ data.createTime }}</span>
      </template>
    </OnmsColumn>
    <OnmsColumn field="severity" header="Severity">
      <template #body="{ data }">
        <OnmsTag :value="data.severity" :severity="severityMap[data.severity?.toLowerCase()] ?? 'secondary'" />
      </template>
    </OnmsColumn>
    <OnmsColumn field="logMessage" header="Message">
      <template #body="{ data }">
        <span v-html="data.logMessage" class="log-message" />
      </template>
    </OnmsColumn>
    <template #empty>
      <EmptyList :content="emptyListContent" data-test="empty-list" />
    </template>
  </OnmsTable>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { OnmsColumn, OnmsTable, OnmsTag, type OnmsTablePageEvent, type OnmsTagSeverity } from '@opennms/onms-ui'
import EmptyList from '@/components/Common/EmptyList.vue'
import { useEventStore } from '@/stores/eventStore'

const eventStore = useEventStore()
const route = useRoute()

const pageSize = ref(5)
const first = ref(0)
const emptyListContent = { msg: 'No results found.' }

const severityMap: Record<string, OnmsTagSeverity> = {
  critical: 'danger',
  major: 'danger',
  minor: 'warn',
  warning: 'warn',
  normal: 'success',
  cleared: 'success',
  indeterminate: 'secondary'
}

const queryParameters = ref({
  limit: 5,
  offset: 0,
  _s: `node.id==${route.params.id}`
})

const onPage = (event: OnmsTablePageEvent) => {
  first.value = event.first
  pageSize.value = event.rows
  queryParameters.value = {
    ...queryParameters.value,
    offset: event.first,
    limit: event.rows,
    _s: `node.id==${route.params.id}`
  }
  eventStore.getEvents(queryParameters.value)
}

onMounted(() => {
  eventStore.getEvents(queryParameters.value)
})

defineExpose({ onPage })
</script>

<style lang="scss" scoped>
.log-message {
  p {
    margin: 0;
  }
}
</style>
