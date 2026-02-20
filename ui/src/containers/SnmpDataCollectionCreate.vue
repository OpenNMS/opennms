<template>
  <div class="snmp-data-collection-create-container">
    <div class="header">
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
      <div class="mib-groups"></div>
      <div class="resource-types"></div>
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
  </div>
</template>

<script lang="ts" setup>
import TableCard from '@/components/Common/TableCard.vue'
import SystemDefForm from '@/components/SnmpDataCollectionCreate/SystemDefForm.vue'
import SystemDefTable from '@/components/SnmpDataCollectionCreate/SystemDefTable.vue'
import { useSnmpDataCollectionCreationStore } from '@/stores/snmpDataCollectionCreationStore'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'

const router = useRouter()
const loading = ref(false)
const timeout = ref<number>(-1)
const results = ref<Array<IAutocompleteItemType>>([])
const store = useSnmpDataCollectionCreationStore()
const selectedCollectionSource = ref<IAutocompleteItemType>()

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

onMounted(async () => {
  await store.initializeCreationForm()
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
    align-items: center;
    gap: 20px;
    margin-bottom: 20px;

    h3 {
      margin: 0;
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
</style>

