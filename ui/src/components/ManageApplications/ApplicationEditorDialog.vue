<template>
  <OnmsDialog
    :visible="visible"
    modal
    :header="`Edit Application: ${application?.name ?? ''}`"
    class="application-editor-dialog"
    width="min(960px, 95vw)"
    data-test="application-editor-dialog"
    @update:visible="(value: boolean) => value ? emit('update:visible', true) : requestClose()"
  >
    <div class="editor">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>

      <OnmsMessage v-if="confirmingDiscard" severity="warn" data-test="discard-prompt">
        <div class="discard-prompt">
          <span>Discard the unsaved changes to this application?</span>
          <OnmsButton variant="text" size="small" label="Keep editing" data-test="keep-editing-button" @click="confirmingDiscard = false" />
          <OnmsButton severity="danger" size="small" label="Discard changes" data-test="discard-button" @click="emit('update:visible', false)" />
        </div>
      </OnmsMessage>

      <div v-if="loading" class="loading" data-test="loading">
        <OnmsSpinner size="1.5rem" strokeWidth="6" />
        <span>Loading the application…</span>
      </div>
      <OnmsMessage v-else-if="loadFailed" severity="error" data-test="load-error">
        The application could not be loaded. Close this dialog and try again.
      </OnmsMessage>

      <template v-else>
        <FormField label="Perspective locations" for="perspective-locations">
          <template #label-suffix>
            <HelpBadge
              content="The perspective poller checks this application's services from each selected monitoring location, so you see outages as users there would. Default is the core server itself. With none selected, the application only groups its services."
              ariaLabel="Perspective locations help"
            />
          </template>
          <OnmsMultiSelect
            v-model="selectedLocations"
            inputId="perspective-locations"
            :options="locationOptions"
            filter
            display="chip"
            placeholder="None"
            fluid
            data-test="perspective-locations"
          />
        </FormField>
        <p v-if="locationsUnavailable" class="field-note" data-test="locations-unavailable">
          The list of monitoring locations could not be loaded; only the current selection is shown.
        </p>

        <section class="section">
          <div class="section-header">
            <h3 class="section-title" data-test="members-title">Services ({{ services.length }})</h3>
          </div>
          <OnmsTable
            :value="services"
            dataKey="id"
            sortField="nodeLabel"
            :sortOrder="1"
            :paginator="services.length > MEMBERS_PAGE_SIZE"
            :rows="MEMBERS_PAGE_SIZE"
            size="small"
            class="members-table"
            data-test="members-table"
          >
            <template #empty>
              <span class="muted" data-test="no-members">No services yet. Search below to add the services this application depends on.</span>
            </template>
            <OnmsColumn field="nodeLabel" header="Node" sortable>
              <template #body="{ data }">
                <a
                  v-if="data.nodeId"
                  :href="nodeUrl(data.nodeId)"
                  target="_blank"
                  rel="noopener"
                  :title="`Open ${data.nodeLabel} in a new tab`"
                  data-test="member-node-link"
                >{{ data.nodeLabel }}</a>
                <span v-else>{{ data.nodeLabel }}</span>
              </template>
            </OnmsColumn>
            <OnmsColumn field="ipAddress" header="IP address" sortable />
            <OnmsColumn field="serviceName" header="Service" sortable />
            <OnmsColumn header="" class="action-column">
              <template #body="{ data }">
                <OnmsIconButton
                  :icon="Delete"
                  severity="danger"
                  :title="`Remove ${describe(data)}`"
                  :aria-label="`Remove ${describe(data)}`"
                  data-test="remove-service-button"
                  @click="removeService(data)"
                />
              </template>
            </OnmsColumn>
          </OnmsTable>
        </section>

        <section class="section">
          <div class="section-header">
            <h3 class="section-title">Add services</h3>
            <OnmsSearchInput
              v-model="candidateSearch"
              placeholder="Node, IP address or service"
              ariaLabel="Search monitored services"
              dataTest="candidate-search"
              class="candidate-search"
            />
          </div>
          <div v-if="addableCandidates.length > 1" class="bulk-add">
            <OnmsButton
              variant="text"
              size="small"
              :label="`Add all ${addableCandidates.length} shown`"
              data-test="add-all-button"
              @click="addAllShown"
            />
          </div>
          <p v-if="candidateError" class="field-note error" role="alert" data-test="candidate-error">
            The services could not be searched. Try again.
          </p>
          <p v-else-if="candidateTotal > candidates.length" class="field-note" data-test="candidate-truncated">
            Showing the first {{ candidates.length }} of {{ candidateTotal }} matching services. Refine the search to find others.
          </p>
          <OnmsTable
            :value="candidates"
            dataKey="id"
            :loading="searching"
            size="small"
            scrollable
            scrollHeight="18rem"
            class="candidates-table"
            data-test="candidates-table"
          >
            <template #empty>
              <span v-if="!searching" class="muted" data-test="no-candidates">No monitored services match.</span>
            </template>
            <OnmsColumn field="nodeLabel" header="Node" />
            <OnmsColumn field="ipAddress" header="IP address" />
            <OnmsColumn field="serviceName" header="Service" />
            <OnmsColumn header="" class="action-column">
              <template #body="{ data }">
                <span v-if="memberIds.has(data.id)" class="muted" data-test="already-added">Added</span>
                <OnmsButton
                  v-else
                  variant="text"
                  size="small"
                  label="Add"
                  :aria-label="`Add ${describe(data)}`"
                  data-test="add-service-button"
                  @click="addService(data)"
                />
              </template>
            </OnmsColumn>
          </OnmsTable>
        </section>
      </template>
    </div>

    <template #footer>
      <span v-if="dirty" class="unsaved" data-test="unsaved-note">Unsaved changes</span>
      <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="requestClose" />
      <OnmsButton
        label="Save Application"
        :disabled="!dirty || saving || loading || loadFailed"
        :loading="saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'

import {
  OnmsButton,
  OnmsColumn,
  OnmsDialog,
  OnmsIconButton,
  OnmsMessage,
  OnmsMultiSelect,
  OnmsSearchInput,
  OnmsSpinner,
  OnmsTable,
  useOnmsToast
} from '@opennms/onms-ui'
import Delete from '@opennms/onms-ui/icons/action/Delete.vue'

import FormField from '@/components/Common/FormField.vue'
import HelpBadge from '@/components/Common/HelpBadge.vue'
import { useApplicationAdminStore } from '@/stores/applicationAdminStore'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { ApplicationService, ApplicationSummary } from '@/types/applicationAdmin'

const props = defineProps<{
  visible: boolean
  application: ApplicationSummary | null
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
}>()

const MEMBERS_PAGE_SIZE = 10
const CANDIDATE_LIMIT = 50
const SEARCH_DEBOUNCE_MS = 300

const store = useApplicationAdminStore()
const locationStore = useMonitoringLocationAdminStore()
const { showToast } = useOnmsToast()

const loading = ref(false)
const loadFailed = ref(false)
const saving = ref(false)
const errorText = ref('')

const services = ref<ApplicationService[]>([])
const originalServiceIds = ref<number[]>([])
const selectedLocations = ref<string[]>([])
const originalLocations = ref<string[]>([])
const locationsUnavailable = ref(false)

const memberIds = computed(() => new Set(services.value.map(service => service.id)))

const sameMembers = <T>(a: T[], b: T[]) => a.length === b.length && new Set(a).size === new Set([...a, ...b]).size

const dirty = computed(() =>
  !sameMembers(services.value.map(service => service.id), originalServiceIds.value)
  || !sameMembers(selectedLocations.value, originalLocations.value))

// the current selection stays selectable even when the location list is unavailable
const locationOptions = computed(() => {
  const names = new Set(locationStore.locations.map(location => location['location-name']))
  selectedLocations.value.forEach(name => names.add(name))
  return [...names].sort((a, b) => a.localeCompare(b))
})

const nodeUrl = (nodeId: number) => `#/node/${nodeId}`

const describe = (service: ApplicationService) =>
  [service.nodeLabel, service.ipAddress, service.serviceName].filter(Boolean).join(' / ')

const removeService = (service: ApplicationService) => {
  services.value = services.value.filter(member => member.id !== service.id)
}

const addService = (service: ApplicationService) => {
  if (!memberIds.value.has(service.id)) {
    services.value = [...services.value, service]
  }
}

const addableCandidates = computed(() => candidates.value.filter(candidate => !memberIds.value.has(candidate.id)))

const addAllShown = () => {
  services.value = [...services.value, ...addableCandidates.value]
}

// Cancel, Escape and the close icon ask first when there are unsaved changes
const confirmingDiscard = ref(false)
const requestClose = () => {
  if (dirty.value && !saving.value) {
    confirmingDiscard.value = true
  } else {
    emit('update:visible', false)
  }
}

const candidateSearch = ref('')
const candidates = ref<ApplicationService[]>([])
const candidateTotal = ref(0)
const candidateError = ref(false)
const searching = ref(false)
let searchRequest = 0
let searchTimer: ReturnType<typeof setTimeout> | undefined

// only the newest search commits, so a slow older one cannot overwrite it
const runSearch = async () => {
  const request = ++searchRequest
  searching.value = true
  const page = await store.searchServices((candidateSearch.value ?? '').trim(), CANDIDATE_LIMIT)
  if (request !== searchRequest) {
    return
  }
  searching.value = false
  candidateError.value = page === null
  candidates.value = page?.services ?? []
  candidateTotal.value = page?.totalCount ?? 0
}

// set while opening resets the search text, which must not start a second search
let skipSearchWatch = false

watch(candidateSearch, () => {
  if (skipSearchWatch) {
    skipSearchWatch = false
    return
  }
  clearTimeout(searchTimer)
  searchTimer = setTimeout(runSearch, SEARCH_DEBOUNCE_MS)
})

onBeforeUnmount(() => clearTimeout(searchTimer))

let loadRequest = 0

// only the load of the latest opening commits; an earlier one may still answer
// after the dialog was closed and reopened, for this or another application
const load = async (application: ApplicationSummary) => {
  const request = ++loadRequest
  loading.value = true
  loadFailed.value = false
  const [members, locationsOk] = await Promise.all([
    store.getMembers(application.id),
    locationStore.locations.length ? Promise.resolve(true) : locationStore.getLocations()
  ])
  if (request !== loadRequest) {
    return
  }
  loading.value = false
  locationsUnavailable.value = !locationsOk
  if (members === null) {
    loadFailed.value = true
    return
  }
  services.value = members.services
  originalServiceIds.value = members.services.map(service => service.id)
  selectedLocations.value = [...members.perspectiveLocations]
  originalLocations.value = [...members.perspectiveLocations]
}

watch(() => props.visible, (isVisible) => {
  if (!isVisible || !props.application) {
    clearTimeout(searchTimer)
    // an answer to a load still in flight is for a closed dialog
    loadRequest++
    return
  }
  errorText.value = ''
  confirmingDiscard.value = false
  services.value = []
  originalServiceIds.value = []
  selectedLocations.value = []
  originalLocations.value = []
  if (candidateSearch.value) {
    skipSearchWatch = true
    candidateSearch.value = ''
  }
  candidates.value = []
  candidateTotal.value = 0
  candidateError.value = false
  load(props.application)
  runSearch()
})

// the server names a vanished service by id only; say which row it is
const explainSaveError = (message: string) => {
  const missing = message.match(/^Monitored service (\d+) was not found\.$/)
  const service = missing ? services.value.find(member => member.id === Number(missing[1])) : undefined
  return service ? `${describe(service)} no longer exists. Remove it from the services and save again.` : message
}

const save = async () => {
  confirmingDiscard.value = false
  if (!props.application) {
    return
  }
  const application = props.application
  saving.value = true
  errorText.value = ''
  try {
    const result = await store.updateMembers(application, {
      serviceIds: services.value.map(service => service.id),
      perspectiveLocations: selectedLocations.value
    })
    if (result.success) {
      showToast({ message: `Application '${application.name}' saved.`, severity: 'success' })
      emit('update:visible', false)
    } else {
      errorText.value = explainSaveError(result.message)
    }
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.editor {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding-top: 0.5rem;
}

.loading {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  margin: 0;
}

.candidate-search {
  width: 20rem;
  max-width: 100%;
}

.field-note {
  margin: 0;
  font-size: 0.85rem;
  color: var(--p-text-muted-color);

  &.error {
    color: var(--p-red-700, #b91c1c);
  }
}

.muted {
  color: var(--p-text-muted-color);
  font-style: italic;
}

:deep(.action-column) {
  width: 5rem;
  text-align: right;
}

.bulk-add {
  display: flex;
  justify-content: flex-end;
  margin-top: -0.25rem;
}

.discard-prompt {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.unsaved {
  margin-right: auto;
  font-size: 0.85rem;
  color: var(--p-text-muted-color);
}

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}
</style>
