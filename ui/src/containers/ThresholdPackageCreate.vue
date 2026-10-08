<template>
  <ThresholdEditPage
    v-if="isLoaded"
    title="Create New Threshd Package"
    sectionTitle="Basic Information"
    saveLabel="Create Threshd Package"
    :saveDisabled="hasErrors(errors) || isSaving"
    @back="goBack"
    @cancel="goBack"
    @save="onSave"
  >
    <FormField label="Name" for="threshd-package-name" required :error="errors.name">
      <OnmsInputText
        inputId="threshd-package-name"
        data-test="threshd-package-create-name"
        :modelValue="draft.name"
        :invalid="!!errors.name"
        fluid
        @update:modelValue="draft.name = String($event ?? '')"
      />
    </FormField>
    <div class="spacer"></div>
    <FormField
      label="Filter"
      for="threshd-package-filter"
      required
      :error="errors.filter"
      :hint="THRESHOLD_FIELD_HINTS.packageFilter"
    >
      <OnmsInputText
        inputId="threshd-package-filter"
        data-test="threshd-package-create-filter"
        :modelValue="draft.filter"
        :invalid="!!errors.filter"
        fluid
        @update:modelValue="draft.filter = String($event ?? '')"
      />
    </FormField>
    <div class="spacer"></div>
    <p class="hint">Services, address ranges and outage calendars are configured after the package exists.</p>
  </ThresholdEditPage>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import FormField from '@/components/Common/FormField.vue'
import ThresholdEditPage from '@/components/ThresholdConfiguration/Common/ThresholdEditPage.vue'
import useSnackbar from '@/composables/useSnackbar'
import { THRESHOLD_FIELD_HINTS } from '@/lib/thresholdHelpText'
import { PACKAGES_TAB_PATH, packagePath } from '@/lib/thresholdRoutes'
import { hasErrors, validateThreshdPackage } from '@/lib/thresholdValidator'
import { getDefaultThreshdPackage, useThreshdConfigurationStore } from '@/stores/threshdConfigurationStore'
import type { ThreshdPackage } from '@/types/thresholdConfig'
import { OnmsInputText } from '@opennms/onms-ui'

const router = useRouter()
const store = useThreshdConfigurationStore()
const { showSnackBar } = useSnackbar()

const isLoaded = ref(false)
const isSaving = ref(false)
const draft = ref<ThreshdPackage>(getDefaultThreshdPackage())

const errors = computed(() => validateThreshdPackage(draft.value, store.packageNames, true))

onMounted(async () => {
  // The package names drive the duplicate check.
  await store.fetchPackages()
  isLoaded.value = true
})

const goBack = () => {
  router.push(PACKAGES_TAB_PATH)
}

const onSave = async () => {
  const pkg: ThreshdPackage = { ...draft.value, name: draft.value.name.trim() }

  isSaving.value = true
  const result = await store.createPackage(pkg)
  isSaving.value = false

  if (result.success) {
    showSnackBar({ msg: 'Threshd package created.' })
    router.push(packagePath(pkg.name))
  } else {
    showSnackBar({ msg: result.message, error: true })
  }
}
</script>

<style lang="scss" scoped>
.hint {
  color: var(--p-text-muted-color);
  margin: 0;
}
</style>
