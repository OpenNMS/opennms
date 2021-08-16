<template>
  <DataTable
    :value="events"
    showGridlines
    data-key="id"
    :loading="loading"
    responsiveLayout="scroll"
    @sort="sort"
    :lazy="true"
    :rowClass="getRowClass"
  >
    <template #header>Recent Events</template>

    <template #empty>No data found.</template>

    <template #loading>Loading data. Please wait.</template>

    <template #footer>
      <Pagination
        :parameters="queryParameters"
        @update-query-parameters="updateQueryParameters"
        moduleName="eventsModule"
        functionName="getEvents"
        totalCountStateName="totalCount"
      />
    </template>

    <Column field="id" header="Id" :sortable="true">
      <template #body="{ data }">
        <router-link :to="`/event/${data.id}`">{{ data.id }}</router-link>
      </template>
    </Column>

    <Column field="createTime" header="Created">
      <template #body="{ data }">{{ getFormattedCreatedTime(data.createTime) }}</template>
    </Column>

    <Column field="severity" header="Severity">
      <template #body="{ data }">{{ data.severity }}</template>
    </Column>

    <Column field="logMessage" header="Message">
      <template #body="{ data }">
        <span v-html="data.logMessage" class="log-message"></span>
      </template>
    </Column>
  </DataTable>
</template>
  
<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import DataTable from 'primevue/datatable'
import InputText from 'primevue/inputtext'
import Column from 'primevue/column'
import Pagination from './Pagination.vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'
import useQueryParameters from '../../hooks/useQueryParams'
import { Event } from '../../types'
import dayjs from 'dayjs'
export default defineComponent({
  name: 'EventsTable',
  components: {
    DataTable,
    InputText,
    Column,
    Pagination
  },
  setup() {
    const store = useStore()
    const route = useRoute()
    const loading = ref(false)
    const { queryParameters, sort, updateQueryParameters } = useQueryParameters({
      limit: 5,
      offset: 0,
      _s: `node.id==${route.params.id}`
    }, 'eventsModule/getEvents')
    const events = computed(() => store.state.eventsModule.events)
    const getRowClass = (data: Event) => data.severity.toLowerCase()
    const getFormattedCreatedTime = (time: number) => dayjs(time).format()
    return {
      sort,
      events,
      loading,
      getRowClass,
      queryParameters,
      updateQueryParameters,
      getFormattedCreatedTime
    }
  }
})
</script>
  
<style lang="scss">
.log-message {
  p {
    margin: 0px !important;
  }
}
.warning {
  background: rgba(255, 175, 34, 0.5) !important;
}
.normal {
  background: rgba(133, 217, 165, 0.5) !important;
}
</style>