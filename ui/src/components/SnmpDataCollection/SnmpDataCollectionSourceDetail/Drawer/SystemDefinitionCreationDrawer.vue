<template>
  <Drawer
    id="drawer"
    data-test="system-definition-drawer"
    v-model:visible="store.systemDefDrawerState.visible"
    position="right"
    :header="drawerTitle"
    :style="{ width: '40rem' }"
    @hide="store.closeSystemDefDrawer"
    class="system-definition-drawer"
  >
    <div class="container">
      <div class="drawer-content">
        <div class="switch-row">
          <ToggleSwitch
            v-model="status"
            data-test="system-def-status-input"
          />
          <label class="switch-label">{{ status ? 'Enabled' : 'Disabled' }}</label>
        </div>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div class="label">General Details</div>
        <FormField
          label="Name"
          :for="nameId"
          :error="errors.name"
        >
          <InputText
            :id="nameId"
            v-model.trim="name"
            :invalid="!!errors.name"
            data-test="system-def-name-input"
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div class="radio-group">
          <span class="radio-group-label">OID Type</span>
          <div
            class="radio-options"
            data-test="system-def-oid-type-input"
          >
            <div
              v-for="item in OID_TYPE_OPTIONS"
              :key="item.name"
              class="radio-option"
            >
              <RadioButton
                v-model="oidType"
                :inputId="`oid-type-${item.value}`"
                :value="item.value"
              />
              <label :for="`oid-type-${item.value}`">{{ item.name }}</label>
            </div>
          </div>
          <small
            v-if="errors.oidType"
            class="field-error"
          >{{ errors.oidType }}</small>
        </div>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <FormField
          label="OID Value"
          :for="oidValueId"
          :error="errors.oidValue"
        >
          <InputText
            :id="oidValueId"
            v-model.trim="oidValue"
            :invalid="!!errors.oidValue"
            data-test="system-def-oid-value-input"
            fluid
          />
        </FormField>
        <div class="spacer"></div>
        <div class="spacer"></div>
        <div class="label">MIB Groups</div>
        <FormField
          label="MIB Groups"
          :for="mibGroupId"
          :error="errors.mibGroupNames"
        >
          <AutoComplete
            :inputId="mibGroupId"
            class="my-autocomplete"
            v-model="mibGroupNames"
            multiple
            :suggestions="results"
            optionLabel="_text"
            @complete="search"
            data-test="system-def-mib-groups-input"
            :invalid="!!errors.mibGroupNames"
            dropdown
            fluid
          />
        </FormField>
      </div>
      <div class="spacer"></div>
      <div class="drawer-footer">
        <Button
          outlined
          label="Cancel"
          data-test="cancel-button"
          @click="store.closeSystemDefDrawer"
        />
        <Button
          label="Save Definition"
          data-test="save-button"
          :disabled="isSaveDisabled"
          @click="saveSystemDef"
        />
      </div>
    </div>
  </Drawer>
</template>

<script setup lang="ts">
import { computed, ref, useId, watch, watchEffect } from 'vue'

import useSnackbar from '@/composables/useSnackbar'
import { mapSnmpDataCollectionSystemDefPayloadToServer } from '@/mappers/snmpDataCollection.mapper'
import { createSystemDefinition, updateSystemDefinition } from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { CreateEditMode } from '@/types'
import { SystemDefErrors } from '@/types/snmpDataCollection'
import { IAutocompleteItemType } from '@featherds/autocomplete'
import AutoComplete from 'primevue/autocomplete'
import Button from 'primevue/button'
import Drawer from 'primevue/drawer'
import InputText from 'primevue/inputtext'
import { DEFAULT_OID_TYPE, DEFAULT_STATUS, OID_PATTERN, OID_MASK_PATTERN, OID_TYPE_OPTIONS } from '@/lib/constants'
import RadioButton from 'primevue/radiobutton'
import ToggleSwitch from 'primevue/toggleswitch'
import FormField from '@/components/Common/FormField.vue'

const store = useSnmpDataCollectionDetailStore()
const nameId = useId()
const oidValueId = useId()
const mibGroupId = useId()
const oidType = ref<string>(DEFAULT_OID_TYPE)
const status = ref<boolean>(DEFAULT_STATUS)
const oidValue = ref<string>('')
const name = ref<string>('')
const results = ref<Array<IAutocompleteItemType>>([])
const mibGroupNames = ref<Array<IAutocompleteItemType>>([])
const snackbar = useSnackbar()
const errors = ref<SystemDefErrors>({})
const isSaveDisabled = ref<boolean>(true)
const drawerTitle = computed(() =>
  store.systemDefDrawerState.isEditMode === CreateEditMode.Create
    ? 'Create System Definition'
    : 'Edit System Definition'
)

const loadInitialData = () => {
  mibGroupNames.value = store.mibGroupNames.map(name => ({ _text: name, _value: name }))
  if (store.systemDefDrawerState.isEditMode === CreateEditMode.Create) {
    name.value = ''
    oidType.value = DEFAULT_OID_TYPE
    oidValue.value = ''
    status.value = DEFAULT_STATUS
    mibGroupNames.value = []
  }
  if (store.systemDefDrawerState.isEditMode === CreateEditMode.Edit) {
    const def = store.selectedSystemDef
    if (def) {
      name.value = def.name
      oidType.value = def.sysoidMask ? 'mask' : def.sysoid ? 'single' : ''
      oidValue.value = def.sysoid || def.sysoidMask || ''
      status.value = def.enabled
      mibGroupNames.value = def.mibGroupNames.map((x: string) => ({ _text: x, _value: x }))
    }
  }
}

const validateDefinition = (): SystemDefErrors => {
  const validationErrors: SystemDefErrors = {}
  if (!name.value.trim()) {
    validationErrors['name'] = 'Name is required.'
  }
  if (!oidType.value) {
    validationErrors['oidType'] = 'OID Type is required.'
  }
  if (!oidValue.value.trim()) {
    validationErrors['oidValue'] = 'OID Value is required.'
  }
  if (oidType.value === 'single' && oidValue.value && !OID_PATTERN.test(oidValue.value)) {
    validationErrors['oidValue'] = 'OID Value format is invalid.'
  }
  if (oidType.value === 'mask' && oidValue.value && !OID_MASK_PATTERN.test(oidValue.value)) {
    validationErrors['oidValue'] = 'OID Mask format is invalid.'
  }
  if (mibGroupNames.value.length === 0) {
    validationErrors['mibGroupNames'] = 'At least one MIB Group must be selected.'
  }
  return validationErrors
}

const search = (event: { query: string }) => {
  const q = event.query.toLowerCase()
  results.value = store.mibGroupNames
    .filter(x => x.toLowerCase().indexOf(q) > -1)
    .map(x => ({ _text: x, _value: x }))
}

const saveSystemDef = async () => {
  errors.value = validateDefinition()
  if (Object.keys(errors.value).length > 0) {
    return
  }

  if (!store.selectedCollectionSource?.id) {
    snackbar.showSnackBar({ msg: 'Please select a Collection Source first.', error: true })
    return
  }

  try {
    const payload = mapSnmpDataCollectionSystemDefPayloadToServer(
      name.value,
      oidType.value === 'single' ? oidValue.value : '',
      oidType.value === 'mask' ? oidValue.value : '',
      [],
      [],
      mibGroupNames.value.map(x => x._value as string),
      status.value,
      store.selectedSystemDef?.id || 0,
      store.systemDefDrawerState.isEditMode
    )

    let response
    if (store.systemDefDrawerState.isEditMode === CreateEditMode.Create) {
      response = await createSystemDefinition(payload, store.selectedCollectionSource.id)
      snackbar.showSnackBar({ msg: 'System Definition created successfully.' })
    }
    if (store.systemDefDrawerState.isEditMode === CreateEditMode.Edit) {
      response = await updateSystemDefinition(payload, store.selectedCollectionSource.id)
      snackbar.showSnackBar({ msg: 'System Definition updated successfully.' })
    }

    if (response) {
      await store.fetchSystemDefinitions()
      store.closeSystemDefDrawer()
    } else {
      snackbar.showSnackBar({ msg: 'An error occurred while saving the System Definition.', error: true })
    }
  } catch (_error) {
    snackbar.showSnackBar({ msg: 'An error occurred while saving the System Definition.', error: true })
  }
}

watchEffect(() => {
  errors.value = validateDefinition()
  isSaveDisabled.value = Object.keys(errors.value).length > 0
})

watch(
  () => store.systemDefDrawerState.visible,
  (visible) => {
    if (visible) {
      loadInitialData()
    } else {
      name.value = ''
      oidType.value = DEFAULT_OID_TYPE
      oidValue.value = ''
      status.value = DEFAULT_STATUS
      mibGroupNames.value = []
      errors.value = {}
      isSaveDisabled.value = true
    }
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";

.system-definition-drawer {
  .container {
    .spacer {
      min-height: 0.5em;
    }

    .drawer-content {
      .label {
        @include headline4;
        margin-bottom: 0.5em;
      }

      .switch-row {
        display: flex;
        align-items: center;
        gap: 0.75rem;

        .switch-label {
          font-size: 12px;
          font-weight: 600;
        }
      }

      .radio-group {
        .radio-group-label {
          display: block;
          @include headline4;
          margin-bottom: 0.5em;
        }

        .radio-options {
          display: flex;
          gap: 1.5rem;
        }

        .radio-option {
          display: flex;
          align-items: center;
          gap: 0.5rem;
        }
      }

      .my-autocomplete {
        width: 100%;
      }

      .field-error {
        display: block;
        color: var(--p-red-500);
        font-size: 0.8em;
        margin-top: 0.25em;
      }
    }

    .drawer-footer {
      padding: 20px 0;
      display: flex;
      justify-content: flex-end;
      gap: 10px;
    }
  }
}
</style>
