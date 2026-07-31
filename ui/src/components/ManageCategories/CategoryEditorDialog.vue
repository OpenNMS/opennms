<template>
  <Dialog
    :visible="visible"
    modal
    :header="isEditing ? `Edit Category: ${originalName}` : 'Add New Surveillance Category'"
    class="category-editor-dialog"
    :style="{ width: '520px', maxWidth: '95vw' }"
    data-test="category-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <Message v-if="errorText" severity="error" :closable="false" data-test="dialog-error">{{ errorText }}</Message>

      <FormField v-if="!isEditing">
        <IftaLabel>
          <InputText id="category-name" v-model="name" :invalid="!!nameProblem" data-test="category-name-input" />
          <label for="category-name">Category Name *</label>
        </IftaLabel>
        <small v-if="nameProblem" class="field-error" data-test="name-error">{{ nameProblem }}</small>
      </FormField>

      <FormField>
        <IftaLabel>
          <InputText id="category-description" v-model="description" data-test="category-description-input" />
          <label for="category-description">Description</label>
        </IftaLabel>
        <small class="hint">The category name is the identifier and cannot be changed after creation.</small>
      </FormField>
    </div>

    <template #footer>
      <Button text label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <Button
        :label="isEditing ? 'Save Category' : 'Add Category'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
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
import { useCategoryAdminStore } from '@/stores/categoryAdminStore'
import { AdminCategory } from '@/services/categoryAdminService'

const props = defineProps<{
  visible: boolean
  category: AdminCategory | null
}>()

const emit = defineEmits(['update:visible'])

const store = useCategoryAdminStore()

const name = ref('')
const description = ref('')
const saving = ref(false)
const errorText = ref('')

const isEditing = computed(() => props.category !== null)
const originalName = computed(() => props.category?.name ?? '')

// the category name is a URL path segment on write and is matched by name in
// filters; block characters that break addressing or markup
const nameProblem = computed(() => {
  if (isEditing.value) {
    return null
  }
  const trimmed = name.value.trim()
  if (!trimmed) {
    return null
  }
  // / \ % ? # break URL/path addressing; markup chars are unsafe; , ; = ( ) ! +
  // break the FIQL category.name== search used to load this category's members
  if (/[/\\%?#\s&<>"'`,;=()!+]/.test(trimmed)) {
    return 'The category name must not contain whitespace, markup, or the characters / \\ % ? # , ; = ( ) ! +'
  }
  return null
})

const isValid = computed(() => (isEditing.value || !!name.value.trim()) && !nameProblem.value)

watch(
  () => props.visible,
  (isVisible) => {
    if (!isVisible) {
      return
    }
    errorText.value = ''
    name.value = props.category?.name ?? ''
    description.value = props.category?.description ?? ''
  }
)

const save = async () => {
  saving.value = true
  try {
    const error = isEditing.value
      ? await store.updateCategoryDescription(originalName.value, description.value.trim())
      : await store.createCategory({ name: name.value.trim(), description: description.value.trim() || undefined })
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

.field-error {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-red-500, #e24c4c);
}

.hint {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-text-muted-color);
}
</style>
