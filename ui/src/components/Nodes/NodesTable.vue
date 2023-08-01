<template>
  <div class="card">
    <div class="feather-row">
      <div class="feather-col-4">
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
              <FeatherSortHeader
                scope="col"
                property="sysContact"
                :sort="sortStates.sysContact"
                v-on:sort-changed="sortChanged"
                >Sys Contact</FeatherSortHeader
              >
               <FeatherSortHeader
                scope="col"
                property="sysLocation"
                :sort="sortStates.sysLocation"
                v-on:sort-changed="sortChanged"
                >Sys Location</FeatherSortHeader
              >
              <FeatherSortHeader
                scope="col"
                property="sysDescription"
                :sort="sortStates.sysDescription"
                v-on:sort-changed="sortChanged"
                >Sys Description</FeatherSortHeader
              >
              <FeatherSortHeader
                scope="col"
                property="flows"
                :sort="sortStates.flows"
                v-on:sort-changed="sortChanged"
                >Flows</FeatherSortHeader
              >
             </tr>
          </thead>
          <tbody>
            <tr
              v-for="node in nodes"
              :key="node.id"
            >
              <td>
                <a
                  :href="computeNodeLink(node.id)"
                  @click="onNodeLinkClick(node.id)"
                  target="_blank">
                  {{ node.label }}
                </a>
                <!-- <router-link :to="`/node/${node.id}`">{{ node.label }}</router-link> -->
              </td>
              <td>{{ node.location }}</td>
              <td>{{ node.foreignSource }}</td>
              <td>{{ node.foreignId }}</td>
              <td>{{ node.sysContact }}</td>
              <td>{{ node.sysLocation }}</td>
              <td>{{ node.sysDescription }}</td>
              <td>{{ displayFlows(node) }}</td>
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
import { Category, FeatherSortObject, MonitoringLocation, Node } from '@/types'
import { MainMenu } from '@/types/mainMenu'
import { isNumber } from '@/lib/utils'

const store = useStore()
const sortStates: any = reactive({
  label: SORT.ASCENDING,
  location: SORT.NONE,
  foreignSource: SORT.NONE,
  foreignId: SORT.NONE,
  sysContact: SORT.NONE,
  sysLocation: SORT.NONE,
  sysDescription: SORT.NONE,
  flows: SORT.NONE
})

const currentSearch = ref('')
const nodes = computed(() => store.state.nodesModule.nodes)
const mainMenu = computed<MainMenu>(() => store.state.menuModule.mainMenu)
const selectedCategories = computed<Category[]>(() => store.state.hierarchyModule.selectedCategories)
const selectedFlows = computed<string[]>(() => store.state.hierarchyModule.selectedFlows)
const selectedLocations = computed<MonitoringLocation[]>(() => store.state.hierarchyModule.selectedMonitoringLocations)

const { queryParameters, updateQueryParameters, sort } = useQueryParameters({
  limit: 10,
  offset: 0,
  orderBy: 'label'
}, 'nodesModule/getNodes')

const sortChanged = (sortObj: FeatherSortObject) => {
  for (const key in sortStates) {
    sortStates[key] = SORT.NONE
  }
  sortStates[`${sortObj.property}`] = sortObj.value
  sort(sortObj)
}

const buildQueryParams = (searchVal: string) => {
  // search query
  let searchQuery = ''

  if (searchVal.length > 0) {
    const star = searchVal.endsWith('*') ? '' : '*'
    searchQuery = `node.label==${searchVal}${star}`
  }

  // category query
  const categoryItems = selectedCategories.value.map(cat => `category.id==${cat.id}`)

  let categoryQuery = ''

  if (categoryItems.length === 1) {
    categoryQuery = `${categoryItems[0]}`
  } else if (categoryItems.length > 1) {
    categoryQuery = `(${categoryItems.join(',')})`
  }

  // flows query
  const hasIngress = selectedFlows.value.some(f => f === 'Ingress')
  const hasEgress = selectedFlows.value.some(f => f === 'Egress')

  const flowItems = [
    hasIngress ? 'lastIngressFlow=gt=0' : '',
    hasEgress ? 'lastEgressFlow=gt=0' : ''
  ].filter(x => x)

  let flowsQuery = ''

  if (flowItems.length === 1) {
    flowsQuery = `${flowItems[0]}`
  } else if (flowItems.length > 1) {
    flowsQuery = `(${flowItems.join(',')})`
  }

  // monitoring locations query
  const locationItems = selectedLocations.value.map(loc => `node.location.locationName==${loc.name}`)
  let locationQuery = ''

  if (locationItems.length === 1) {
    locationQuery = `${locationItems[0]}`
  } else if (locationItems.length > 1) {
    locationQuery = `(${locationItems.join(',')})`
  }

  // TODO: filter on regex to screen out bad FIQL characters like ',', ';', etc.
  // and/or restrict characters in the FeatherInput above
  let query = [searchQuery, categoryQuery, flowsQuery, locationQuery].filter(s => s.length > 0).join(';')

  return query
}

const displayFlows = (node: Node) => {
  const hasIngress = node.lastIngressFlow && isNumber(node.lastIngressFlow)
  const hasEgress = node.lastEgressFlow && isNumber(node.lastEgressFlow)

  if (hasIngress && hasEgress) {
    return 'I/E'
  } else if (hasEgress) {
    return 'E'
  } else if (hasIngress) {
    return 'I'
  }

  return ''
}

const searchFilterHandler: UpdateModelFunction = (val = '') => {
  currentSearch.value = val
  updateQuery(val)
}

const updateQuery = (val?: string) => {
  const searchQuery = buildQueryParams(val || currentSearch.value)
  const searchQueryParam: QueryParameters = { _s: searchQuery }
  const updatedParams = { ...queryParameters.value, ...searchQueryParam }

  store.dispatch('nodesModule/getNodes', updatedParams)
  queryParameters.value = updatedParams
}

const computeNodeLink = (nodeId: number) => {
  return `${mainMenu.value.baseHref}${mainMenu.value.baseNodeUrl}${nodeId}`
}

const onNodeLinkClick = (nodeId: number) => {
  window.location.assign(computeNodeLink(nodeId))
}

watch([selectedCategories, selectedFlows, selectedLocations], () => {
  updateQuery()
})
</script>

<style lang="scss" scoped>
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
