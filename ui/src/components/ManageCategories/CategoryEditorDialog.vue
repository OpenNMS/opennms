<template>
  <OnmsDialog
    :visible="visible"
    :header="isEditing ? `Edit Category: ${originalName}` : 'Add New Surveillance Category'"
    class="category-editor-dialog"
    width="min(520px, 95vw)"
    data-test="category-editor-dialog"
    @update:visible="(value: boolean) => emit('update:visible', value)"
  >
    <div class="form-column">
      <div v-if="errorText" class="dialog-error" role="alert" data-test="dialog-error">{{ errorText }}</div>

      <FormField v-if="!isEditing" label="Category Name" for="category-name" required :error="nameProblem || undefined" hint="The name is the identifier and cannot be changed after creation.">
        <OnmsInputText id="category-name" v-model="name" :invalid="!!nameProblem" :maxlength="MAX_NAME_LENGTH" fluid data-test="category-name-input" />
      </FormField>

      <FormField label="Description" for="category-description" :error="descriptionProblem || undefined">
        <OnmsInputText id="category-description" v-model="description" :invalid="!!descriptionProblem" :maxlength="MAX_DESCRIPTION_LENGTH" fluid data-test="category-description-input" />
      </FormField>
    </div>

    <template #footer>
      <OnmsButton variant="ghost" label="Cancel" data-test="cancel-button" @click="emit('update:visible', false)" />
      <OnmsButton
        :label="isEditing ? 'Save Category' : 'Add Category'"
        :disabled="!isValid || saving"
        data-test="save-button"
        @click="save"
      />
    </template>
  </OnmsDialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

import { OnmsButton, OnmsDialog, OnmsInputText, useOnmsToast } from '@opennms/onms-ui'

import FormField from '@/components/Common/FormField.vue'
import { useCategoryAdminStore } from '@/stores/categoryAdminStore'
import { AdminCategory } from '@/types/categoryAdmin'

const props = defineProps<{
  visible: boolean
  category: AdminCategory | null
}>()

const emit = defineEmits(['update:visible'])

const store = useCategoryAdminStore()
const { showToast } = useOnmsToast()

const name = ref('')
const description = ref('')
const saving = ref(false)
const errorText = ref('')

const isEditing = computed(() => props.category !== null)
const originalName = computed(() => props.category?.name ?? '')

// the category name is a URL path segment on write; block what breaks addressing or markup
// the categories columns are varchar(64) and varchar(256)
const MAX_NAME_LENGTH = 64
const MAX_DESCRIPTION_LENGTH = 256

const nameProblem = computed(() => {
  if (isEditing.value) {
    return null
  }
  const trimmed = name.value.trim()
  if (!trimmed) {
    return null
  }
  if (trimmed.length > MAX_NAME_LENGTH) {
    return `The category name cannot be longer than ${MAX_NAME_LENGTH} characters.`
  }
  if (/[/\\%?#<>"'`]/.test(trimmed)) {
    return 'The category name must not contain the characters / \\ % ? # < > " \' `'
  }
  // "." and ".." fold into the collection URL, so their item calls would hit every category
  if (trimmed === '.' || trimmed === '..') {
    return 'The category name cannot be . or ..'
  }
  return null
})

const descriptionProblem = computed(() =>
  description.value.trim().length > MAX_DESCRIPTION_LENGTH ? `The description cannot be longer than ${MAX_DESCRIPTION_LENGTH} characters.` : null)

const isValid = computed(() => (isEditing.value || !!name.value.trim()) && !nameProblem.value && !descriptionProblem.value)

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
    const categoryName = isEditing.value ? originalName.value : name.value.trim()
    const result = isEditing.value
      ? await store.updateCategoryDescription(originalName.value, description.value.trim())
      : await store.createCategory({ name: categoryName, description: description.value.trim() || undefined })
    if (result.success) {
      showToast({ message: `Category '${categoryName}' ${isEditing.value ? 'updated' : 'created'}.`, severity: 'success' })
      emit('update:visible', false)
    } else {
      errorText.value = result.message
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

.dialog-error {
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  border: 1px solid var(--p-red-200, #fecaca);
  background: var(--p-red-50, #fef2f2);
  color: var(--p-red-700, #b91c1c);
  font-size: 0.9rem;
}

.field-error {
  display: block;
  margin-top: 0.25rem;
  color: var(--p-red-500, #e24c4c);
}
</style>
