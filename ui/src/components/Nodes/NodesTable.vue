<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-3">
        <FeatherInput
          @update:modelValue="searchFilterHandler"
          label="Search node label"
        />
      </div>
    </div>
    <div class="feather-row">
      <div class="feather-col-12">
        <table
          class="tl1 tl2 tl3 tl4"
          summary="Nodes"
        >
          <thead>
            <tr>
              <FeatherSortHeader
                scope="col"
                property="label"
                :sort="sortStates.label"
                v-on:sort-changed="sortChanged"
                >Label</FeatherSortHeader
              >
              <FeatherSortHeader
                scope="col"
                property="location"
                :sort="sortStates.location"
                v-on:sort-changed="sortChanged"
                >Location</FeatherSortHeader
              >
              <FeatherSortHeader
                scope="col"
                property="foreignSource"
                :sort="sortStates.foreignSource"
                v-on:sort-changed="sortChanged"
                >Foreign Source</FeatherSortHeader
              >
              <FeatherSortHeader
                scope="col"
                property="foreignId"
                :sort="sortStates.foreignId"
                v-on:sort-changed="sortChanged"
                >Foreign Id</FeatherSortHeader
              >
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="node in nodes"
              :key="node.id"
            >
              <td>
                <router-link :to="`/node/${node.id}`">{{ node.label }}</router-link>
              </td>
              <td>{{ node.location }}</td>
              <td>{{ node.foreignSource }}</td>
              <td>{{ node.foreignId }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    <Pagination
      :parameters="queryParameters"
      @update-query-parameters="updateQueryParameters"
      moduleName="nodesModule"
      functionName="getNodes"
      totalCountStateName="totalCount"
    />
  </div>
</template>

<script
  setup
  lang="ts"
>
import Pagination from '../Common/Pagination.vue'
import { useStore } from 'vuex'
import { QueryParameters, UpdateModelFunction } from '@/types'
import useQueryParameters from '@/composables/useQueryParams'
import { FeatherInput } from '@featherds/input'
import { FeatherSortHeader, SORT } from '@featherds/table'
import { FeatherSortObject } from '@/types'

const store = useStore()
const sortStates: any = reactive({
  label: SORT.ASCENDING,
  location: SORT.NONE,
  foreignSource: SORT.NONE,
  foreignId: SORT.NONE
})

const sortChanged = (sortObj: FeatherSortObject) => {
  for (const key in sortStates) {
    sortStates[key] = SORT.NONE
  }
  sortStates[`${sortObj.property}`] = sortObj.value
  sort(sortObj)
}

const { queryParameters, updateQueryParameters, sort } = useQueryParameters({
  limit: 10,
  offset: 0,
  orderBy: 'label'
}, 'nodesModule/getNodes')
const searchFilterHandler: UpdateModelFunction = (val = '') => {
  const searchQueryParam: QueryParameters = { _s: `node.label==${val}*` }
  const updatedParams = { ...queryParameters.value, ...searchQueryParam }
  store.dispatch('nodesModule/getNodes', updatedParams)
  queryParameters.value = updatedParams
}
const nodes = computed(() => store.state.nodesModule.nodes)
</script>

<style
  lang="scss"
  scoped
>
@import "@featherds/table/scss/table";
@import "@featherds/styles/mixins/elevation";
@import "@featherds/styles/mixins/typography";
.card {
  @include elevation(2);
  background: var($surface);
  padding: 15px;
}
table {
  @include table;
}
</style>

