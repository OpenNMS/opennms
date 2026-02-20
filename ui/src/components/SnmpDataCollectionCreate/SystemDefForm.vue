<template>
  <TableCard class="system-def-form-card">
    <div class="header">
      <div class="title-container">
        <h3 class="title">{{ props.systemDef ? 'Update System Definition' : 'Create System Definition' }}</h3>
      </div>
    </div>
    <div class="form">
      <div class="spacer"></div>
      <div class="spacer"></div>
      <FeatherInput
        label="Name"
        v-model.trim="name"
        data-test="system-def-name-input"
        :error="errors.name"
      />
      <div class="spacer"></div>
      <div class="spacer"></div>
      <FeatherRadioGroup
        :label="'OID Type'"
        v-model.trim="oidType"
        data-test="system-def-oid-type-input"
        :error="errors.oidType"
      >
        <FeatherRadio
          v-for="item in OID_TYPE_OPTIONS"
          :value="item.value"
          :key="item.name"
        >
          {{ item.name }}
        </FeatherRadio>
      </FeatherRadioGroup>
      <div class="spacer"></div>
      <div class="spacer"></div>
      <FeatherInput
        label="OID Value"
        v-model.trim="oidValue"
        data-test="system-def-oid-value-input"
        :error="errors.oidValue"
      />
      <div class="spacer"></div>
      <div class="spacer"></div>
      <FeatherAutocomplete
        class="my-autocomplete"
        label="Mib Groups"
        type="multi"
        v-model="mibGroupNames"
        :loading="loading"
        :results="results"
        @search="search"
        data-test="system-def-mib-groups-input"
        :error="errors.mibGroupNames"
      ></FeatherAutocomplete>
      <div class="spacer"></div>
      <div class="spacer"></div>
      <FeatherRadioGroup
        :label="'Status'"
        v-model="status"
        data-test="system-def-status-input"
        :error="errors.status"
      >
        <FeatherRadio
          v-for="item in STATUS_OPTIONS"
          :value="item.value"
          :key="item.name"
        >
          {{ item.name }}
        </FeatherRadio>
      </FeatherRadioGroup>
    </div>
    <div class="footer">
      <FeatherButton
        secondary
        data-test="cancel-button"
        @click="handleCancel"
      >
        Cancel
      </FeatherButton>
      <FeatherButton
        primary
        data-test="save-button"
        @click="handleSave"
      >
        {{ props.systemDef ? 'Update' : 'Create' }}
      </FeatherButton>
    </div>
  </TableCard>
</template>

<script lang="ts" setup>
import { DEFAULT_OID_TYPE, DEFAULT_STATUS, OID_TYPE_OPTIONS, STATUS_OPTIONS } from '@/lib/constants'
import { mapSnmpDataCollectionSystemDefPayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SnmpCollectionSystemDefPayload, SystemDefErrors } from '@/types/snmpDataCollection'
import { FeatherAutocomplete, IAutocompleteItemType } from '@featherds/autocomplete'
import FeatherButton from '@featherds/button/src/components/FeatherButton.vue'
import { FeatherInput } from '@featherds/input'
import { FeatherRadio, FeatherRadioGroup } from '@featherds/radio'
import TableCard from '../Common/TableCard.vue'

const props = defineProps<{
  systemDef: SnmpCollectionSystemDefPayload | null
}>()

const emit = defineEmits<{
  (e: 'save', systemDef: SnmpCollectionSystemDefPayload): void
  (e: 'cancel'): void
}>()

const oidType = ref<string>(DEFAULT_OID_TYPE)
const status = ref<boolean>(DEFAULT_STATUS)
const oidValue = ref<string>('')
const name = ref<string>('')
const timeout = ref<any>(null)
const loading = ref<boolean>(false)
const results = ref<Array<IAutocompleteItemType>>([])
const mibGroupNames = ref<Array<IAutocompleteItemType>>([])
const errors = ref<SystemDefErrors>({})
const store = useSnmpDataCollectionDetailStore()

const loadSystemDef = (systemDef: SnmpCollectionSystemDefPayload | null) => {
  if (systemDef === null) {
    name.value = ''
    oidType.value = DEFAULT_OID_TYPE
    oidValue.value = ''
    status.value = DEFAULT_STATUS
    mibGroupNames.value = []
  }
  if (systemDef !== null) {
    const def = systemDef
    if (def) {
      name.value = def.name
      oidType.value = def.sysoidMask ? 'mask' : def.sysoid ? 'single' : ''
      oidValue.value = def.sysoid || def.sysoidMask || ''
      status.value = def.enabled
      mibGroupNames.value = JSON.parse(def.mibGroupNames).map((x: string) => ({ _text: x, _value: x }))
    }
  }
}

const handleSave = () => {
  const payload = mapSnmpDataCollectionSystemDefPayloadToServer(
    name.value,
    oidType.value === 'single' ? oidValue.value : '',
    oidType.value === 'mask' ? oidValue.value : '',
    '',
    '',
    mibGroupNames.value.map((x) => x._value as string),
    status.value,
    store.selectedSystemDef?.id || 0,
    CreateEditMode.Create
  )
  emit('save', payload)
}

const handleCancel = () => {
  emit('cancel')
}

const search = (q: string) => {
  loading.value = true
  if (timeout.value) {
    clearTimeout(timeout.value)
  }
  timeout.value = setTimeout(() => {
    results.value = store.mibGroupNames
      .filter((x) => x.toLowerCase().indexOf(q.toLowerCase()) > -1)
      .map((x) => ({
        _text: x,
        _value: x
      }))
    loading.value = false
  }, 500)
}

watch(
  () => props.systemDef,
  (newVal) => {
    loadSystemDef(newVal)
  },
  { immediate: true }
)
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';
@use '@featherds/table/scss/table';
@use '@/styles/_transitionDataTable';

.system-def-form-card {
  padding: 20px;

  .header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 10px;

    .title-container {
      .title {
        @include typography.headline3;
        margin: 0;
      }
    }

    .action-container {
      display: flex;
      align-items: center;
      gap: 10px;

      button {
        margin: 0;
      }
    }
  }

  .spacer {
    min-height: 0.5em;
  }

  .form {
    padding: 16px;
  }

  .footer {
    display: flex;
    justify-content: flex-end;
    padding: 16px;
  }
}
</style>

