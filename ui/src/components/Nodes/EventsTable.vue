<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-12 headline3">Recent Events</div>
    </div>
    <div class="feather-row">
      <div class="feather-col-12">
        <table
          class="tl1 tl2 tl3 tl4"
          summary="Recent Events"
        >
          <thead>
            <tr>
              <th scope="col">Id</th>
              <th scope="col">Created</th>
              <th scope="col">Severity</th>
              <th scope="col">Message</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="event in events"
              :key="event.id"
              :class="getRowClass(event)"
            >
              <td>
                <router-link :to="`/event/${event.id}`">{{ event.id }}</router-link>
              </td>
              <td v-date>{{ event.createTime }}</td>
              <td>{{ event.severity }}</td>
              <td>
                <span
                  v-html="event.logMessage"
                  class="log-message"
                ></span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <Pagination
      :parameters="queryParameters"
      @update-query-parameters="updateQueryParameters"
      :query="getEvents"
      :getTotalCount="getEventsTotalCount"
    />
  </div>
</template>

<script
  setup
  lang="ts"
>
import Pagination from '../Common/Pagination.vue'
import { useEventStore } from '@/stores/eventStore'
import useQueryParameters from '@/composables/useQueryParams'
import { Event, QueryParameters } from '@/types'

const eventStore = useEventStore()
const route = useRoute()

const getEvents = async (payload: QueryParameters) => {
  eventStore.getEvents(payload)
}

const getEventsTotalCount = () => {
  return eventStore.totalCount
}

const { queryParameters, updateQueryParameters } = useQueryParameters({
  limit: 5,
  offset: 0,
  _s: `node.id==${route.params.id}`
}, getEvents)

const events = computed(() => eventStore.events)
const getRowClass = (data: Event) => data.severity.toLowerCase()
</script>

<style
  lang="scss"
  scoped
>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  padding: 15px;
  margin-bottom: 15px;
}
table {
  @include table;
}
.log-message {
  p {
    margin: 0px;
  }
}
.warning {
  background: rgba(255, 175, 34, 0.5);
  color: var($state-color-on-surface);
}
.normal {
  background: rgba(133, 217, 165, 0.5);
  color: var($state-color-on-surface);
}
</style>
