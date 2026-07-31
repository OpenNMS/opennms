<template>
  <Dialog
    :visible="visible"
    modal
    :header="`Edit Minion: ${minion?.id ?? ''}`"
    class="minion-editor-dialog"
    :style="{ width: '620px', maxWidth: '95vw' }"
    data-test="minion-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <Message v-if="errorText" severity="error" :closable="false" data-test="dialog-error">{{ errorText }}</Message>

      <FormField>
        <IftaLabel>
          <InputText id="minion-label" v-model="label" data-test="label-input" />
          <label for="minion-label">Label</label>
        </IftaLabel>
      </FormField>

      <FormField>
        <IftaLabel>
          <InputText id="minion-location" v-model="location" :invalid="!!locationProblem" data-test="location-input" />
          <label for="minion-location">Location *</label>
        </IftaLabel>
        <small v-if="locationProblem" class="field-error" data-test="location-error">{{ locationProblem }}</small>
        <small v-else class="hint">The monitoring location this minion belongs to (required).</small>
      </FormField>

      <div class="props-section">
        <div class="props-header">
          <span class="props-title">Properties</span>
          <Button
            outlined
            size="small"
            icon="pi pi-plus"
            label="Add"
            aria-label="Add property"
            data-test="add-property-button"
            @click="addProperty"
          />
        </div>
        <ul v-if="properties.length" class="props-list" data-test="property-list">
          <li v-for="(prop, index) in properties" :key="index" class="prop-row">
            <InputText v-model="prop.key" placeholder="Key" :data-test="`prop-key-${index}`" class="prop-key" />
            <InputText v-model="prop.value" placeholder="Value" :data-test="`prop-value-${index}`" class="prop-value" />
            <Button
              text
              rounded
              icon="pi pi-times"
              severity="danger"
              :aria-label="`Remove property ${prop.key}`"
              :data-test="`remove-prop-${index}`"
              @click="properties.splice(index, 1)"
            />
          </li>
        </ul>
        <p v-else class="no-props">No properties.</p>
        <small v-if="dupKeyProblem" class="field-error" data-test="prop-error">{{ dupKeyProblem }}</small>
      </div>
    </div>

    <template #footer>
      <Button text label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <Button label="Save Minion" :disabled="!!dupKeyProblem || !!locationProblem || saving" data-test="save-button" @click="save" />
    </template>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import Button from 'primevue/button'
import Dialog from 'primevue/dialog'
import IftaLabel from 'primevue/iftalabel'
import InputText from 'primevue/inputtext'
import Message from 'primevue/message'

import FormField from '@/components/Common/FormField.vue'
import { useMinionAdminStore } from '@/stores/minionAdminStore'
import { Minion } from '@/types/minionAdmin'
import { MinionEdit } from '@/services/minionAdminService'

const props = defineProps<{
  visible: boolean
  minion: Minion | null
}>()

const emit = defineEmits(['update:visible'])

const store = useMinionAdminStore()

const label = ref('')
const location = ref('')
const properties = ref<{ key: string; value: string }[]>([])
const saving = ref(false)
const errorText = ref('')

const locationProblem = computed(() => (location.value.trim() ? null : 'A location is required.'))

const dupKeyProblem = computed(() => {
  const keys = properties.value.map((p) => p.key.trim()).filter(Boolean)
  if (new Set(keys).size !== keys.length) {
    return 'Property keys must be unique.'
  }
  if (properties.value.some((p) => !p.key.trim() && p.value.trim())) {
    return 'A property value needs a key.'
  }
  return null
})

watch(
  () => props.visible,
  (isVisible) => {
    if (!isVisible) {
      return
    }
    errorText.value = ''
    label.value = props.minion?.label ?? ''
    location.value = props.minion?.location ?? ''
    properties.value = Object.entries(props.minion?.properties ?? {}).map(([key, value]) => ({ key, value: String(value) }))
  }
)

const addProperty = () => {
  properties.value.push({ key: '', value: '' })
}

const save = async () => {
  if (!props.minion || dupKeyProblem.value || locationProblem.value) {
    return
  }
  saving.value = true
  try {
    const propertyMap: Record<string, string> = {}
    for (const { key, value } of properties.value) {
      // preserve keys verbatim (do not trim) so a key the admin never touched
      // is not silently renamed; drop only fully-empty rows
      if (key || value) {
        if (key) {
          propertyMap[key] = value
        }
      }
    }
    // the service reads the current server row and changes only these three
    // fields, so server-maintained status/version/date are never clobbered
    const edit: MinionEdit = {
      id: props.minion.id,
      label: label.value.trim() || null,
      location: location.value.trim(),
      properties: propertyMap
    }
    const error = await store.updateMinion(edit)
    if (error === null) {
      emit('update:visible', false)
    } else {
      errorText.value = error
    }
  } finally {
    saving.value = false
  }
}
</script>

<style lang="scss" scoped>
.form-column {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-top: 0.5rem;

  :deep(input) {
    width: 100%;
  }
}

.props-section {
  .props-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 0.5rem;

    .props-title {
      font-weight: 600;
    }
  }

  .props-list {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 0.5rem;

    .prop-row {
      display: flex;
      gap: 0.5rem;
      align-items: center;

      .prop-key {
        flex: 0 0 40%;
      }
      .prop-value {
        flex: 1;
      }
    }
  }

  .no-props {
    margin: 0;
    color: var(--p-text-muted-color);
  }
}

.field-error {
  display: block;
  margin-top: 0.5rem;
  color: var(--p-red-500, #e24c4c);
}

.hint {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-text-muted-color);
}
</style>
