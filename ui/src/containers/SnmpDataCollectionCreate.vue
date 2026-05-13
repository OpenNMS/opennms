<template>
  <div class="snmp-data-collection-create-container">
    <div class="header">
      <div class="title">
        <div>
          <FeatherBackButton
            data-test="back-button"
            @click="handleCancel"
          >
            Go Back
          </FeatherBackButton>
        </div>
        <div>
          <h3>Create SNMP Data Collection Source</h3>
        </div>
      </div>
      <div class="action">
        <FeatherButton
          primary
          @click="showSourceCreationDialog"
          data-test="save-button"
        >
          Create Data Collection Source
        </FeatherButton>
      </div>
    </div>
    <div class="content">
      <div class="source">
        <TableCard class="source-card">
          <FeatherAutocomplete
            class="my-autocomplete"
            :disabled="store.selectedCollectionSource?.name && store.selectedCollectionSource?.id ? true : false"
            :model-value="selectedCollectionSource"
            @update:model-value="(item: any) => setSelectedCollectionSource(item)"
            label="Source Name"
            data-test="source-name"
            :results="results"
            type="single"
            @search="search"
          >
          </FeatherAutocomplete>
        </TableCard>
      </div>
      <div class="system-defs">
        <SystemDefTable />
        <SystemDefForm />
      </div>
      <div class="mib-groups">
        <MibGroupTable />
        <MibGroupForm />
      </div>
      <div class="resource-types">
        <ResourceTypeTable />
        <ResourceTypeForm />
      </div>
    </div>
    <div class="footer">
      <FeatherButton
        @click="handleCancel"
        data-test="cancel-button"
      >
        Cancel
      </FeatherButton>
      <FeatherButton
        primary
        data-test="create-button"
      >
        Create
      </FeatherButton>
    </div>
    <FeatherDialog
      v-model="sourceCreationDialogState"
      :labels="labels"
      hide-close
      @hidden="handleSourceCreationCancel"
    >
      <div class="modal-body-form">
        <div>
          <FeatherInput
            label="Data Collection Source Name"
            v-model="configName"
            :error="sourceCreationErrors?.name"
            data-test="source-name"
          />
        </div>
        <div class="profile-picker">
          <FeatherAutocomplete
            label="Add to profiles"
            type="multi"
            v-model="selectedProfileItems"
            :results="profileResults"
            :allow-new="false"
            :error="sourceCreationErrors?.profiles"
            text-prop="_text"
            data-test="profile-picker"
            @search="searchProfiles"
          />
        </div>
      </div>
      <template v-slot:footer>
        <FeatherButton
          @click="handleSourceCreationCancel"
          data-test="cancel-source-button"
        >
          Cancel
        </FeatherButton>
        <FeatherButton
          primary
          @click="handleSourceCreationSave"
          :disabled="Object.keys(sourceCreationErrors || {}).length > 0"
          data-test="create-source-button"
        >
          Create Source
        </FeatherButton>
      </template>
    </FeatherDialog>
  </div>
</template>

<script lang="ts" setup>
import TableCard from '@/components/Common/TableCard.vue'
import MibGroupForm from '@/components/SnmpDataCollectionCreate/MibGroupForm.vue'
import MibGroupTable from '@/components/SnmpDataCollectionCreate/MibGroupTable.vue'
import ResourceTypeForm from '@/components/SnmpDataCollectionCreate/ResourceTypeForm.vue'
import ResourceTypeTable from '@/components/SnmpDataCollectionCreate/ResourceTypeTable.vue'
import SystemDefForm from '@/components/SnmpDataCollectionCreate/SystemDefForm.vue'
import SystemDefTable from '@/components/SnmpDataCollectionCreate/SystemDefTable.vue'
import { getAllSnmpCollectionProfiles } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionCreationStore } from '@/stores/snmpDataCollectionCreationStore'
import { SnmpCollectionProfile } from '@/types/snmpDataCollection'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import { FeatherInput } from '@featherds/input'

const router = useRouter()
const loading = ref(false)
const timeout = ref<number>(-1)
const results = ref<Array<IAutocompleteItemType>>([])
const store = useSnmpDataCollectionCreationStore()
const selectedCollectionSource = ref<IAutocompleteItemType>()
const configName = ref('')
const sourceCreationDialogState = ref(false)
const availableProfiles = ref<SnmpCollectionProfile[]>([])
const selectedProfileItems = ref<IAutocompleteItemType[]>([])
const profileResults = ref<IAutocompleteItemType[]>([])
const labels = {
  title: 'Create New Data Collection Source'
}

const handleCancel = () => {
  if (store.selectedCollectionSource) {
    router.push({ name: 'SNMP Data Collection Detail', params: { id: store.selectedCollectionSource.id } })
  } else {
    router.push({ name: 'SNMP Data Collection' })
  }
}

const search = (query: string) => {
  loading.value = true
  clearTimeout(timeout.value)
  timeout.value = window.setTimeout(() => {
    results.value = store.uploadedSourceNames
      .filter((s) => s.name.toLowerCase().includes(query.toLowerCase()))
      .map((x) => ({ _text: x.name, _value: x.id }))
    loading.value = false
  }, 500)
}

const setSelectedCollectionSource = (item: IAutocompleteItemType) => {
  selectedCollectionSource.value = item
}

const sourceCreationErrors = computed(() => {
  const error: Record<string, string> = {}
  if (configName.value.trim() === '') {
    error.name = 'Configuration name is required.'
  }
  if (selectedProfileItems.value.length === 0) {
    error.profiles = 'Pick at least one profile.'
  }
  return Object.keys(error).length > 0 ? error : null
})

const showSourceCreationDialog = () => {
  configName.value = ''
  selectedProfileItems.value = []
  profileResults.value = profilesToItems(availableProfiles.value)
  sourceCreationDialogState.value = true
}

const handleSourceCreationCancel = () => {
  sourceCreationDialogState.value = false
  configName.value = ''
  selectedProfileItems.value = []
}

const profilesToItems = (profiles: SnmpCollectionProfile[]): IAutocompleteItemType[] =>
  profiles.map((p) => ({ _text: p.name, _value: p.id }))

const searchProfiles = (query: string) => {
  const q = (query ?? '').toLowerCase()
  profileResults.value = profilesToItems(
    availableProfiles.value.filter((p) => p.name.toLowerCase().includes(q))
  )
}

const handleSourceCreationSave = () => {
  // TODO: implement source creation
}

onMounted(async () => {
  await store.initializeCreationForm()
  availableProfiles.value = await getAllSnmpCollectionProfiles()
  profileResults.value = profilesToItems(availableProfiles.value)
  if (store.selectedCollectionSource) {
    nextTick(() => {
      selectedCollectionSource.value = {
        _text: store.selectedCollectionSource?.name,
        _value: store.selectedCollectionSource?.id
      }
    })
  } else {
    selectedCollectionSource.value = undefined as unknown as IAutocompleteItemType
  }
})
</script>

<style lang="scss" scoped>
.snmp-data-collection-create-container {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .title {
      display: flex;
      align-items: center;
      gap: 10px;

      h3 {
        margin: 0;
      }
    }

    .action {
      button {
        margin: 0;
      }
    }
  }

  .content {
    .source {
      margin-bottom: 20px;

      .source-card {
        padding: 20px;
      }
    }
  }

  .footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;

    button {
      margin: 0;
    }
  }
}

.modal-body-form {
  width: 50rem;
}
</style>

