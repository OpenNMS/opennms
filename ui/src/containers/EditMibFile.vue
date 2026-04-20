<template>
  <div class="edit-mib-file">
    <div class="feather-row">
      <div class="feather-col-12">
        <BreadCrumbs :items="breadcrumbs" />
      </div>
    </div>

    <TableCard class="edit-mib-file">
      <div class="edit-mib-file__content">
        <div class="edit-mib-file__title">
          <p>Edit MIB File</p>
        </div>
        <div class="edit-mib-file__subtitle">
          <p>Make changes to the MIB file content and save.</p>
        </div>
        <div
          class="edit-mib-file__caption"
          v-if="store.selectedMibFile"
        >
          <p>File name: {{ store.selectedMibFile?.name }}</p>
        </div>
        <div
          class="edit-mib-file__input"
          v-if="store.selectedMibFile"
        >
          <FeatherTextarea
            label="MIB File Content"
            v-model="textContent"
            rows="20"
          />
        </div>
        <div
          class="edit-mib-file__footer"
          v-if="store.selectedMibFile"
        >
          <FeatherButton
            secondary
            @click="onCancel"
          >
            Cancel
          </FeatherButton>
          <FeatherButton
            primary
            :disabled="isDisabled"
            @click="onSave"
          >
            Save
          </FeatherButton>
        </div>
        <div
          class="edit-mib-file__no-content"
          v-if="!store.selectedMibFile"
        >
          <p>No MIB file selected for editing.</p>
          <FeatherButton
            primary
            @click="onCancel"
          >
            Back to MIB Compiler
          </FeatherButton>
        </div>
      </div>
    </TableCard>
    <ConfirmationDialog
      @ok="confirmCancel"
      @cancel="cancelConfirmationVisible = false"
      title="Unsaved Changes"
      :visible="cancelConfirmationVisible"
    >
      <template #content> You have unsaved changes. Are you sure you want to leave without saving? </template>
    </ConfirmationDialog>
    <ConfirmationDialog
      @ok="confirmSave"
      @cancel="saveConfirmationVisible = false"
      title="Confirm Save"
      :visible="saveConfirmationVisible"
    >
      <template #content> Are you sure you want to save the changes to the MIB file? </template>
    </ConfirmationDialog>
  </div>
</template>

<script lang="ts" setup>
import ConfirmationDialog from '@/components/Common/ConfirmationDialog.vue'
import TableCard from '@/components/Common/TableCard.vue'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useSnackbar from '@/composables/useSnackbar'
import { setFileText } from '@/services/mibCompilerService'
import { useMenuStore } from '@/stores/menuStore'
import { useMibCompilerStore } from '@/stores/mibCompilerStore'
import { BreadCrumb } from '@/types'
import { FeatherButton } from '@featherds/button'
import { FeatherTextarea } from '@featherds/textarea'

const menuStore = useMenuStore()
const store = useMibCompilerStore()
const { showSnackBar } = useSnackbar()
const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)
const cancelConfirmationVisible = ref(false)
const saveConfirmationVisible = ref(false)
const router = useRouter()
const textContent = ref('')
const isDisabled = computed(() => !textContent.value || textContent.value === store.selectedMibFile?.contents)

const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: false },
  { label: 'MIB Compiler', to: '/mib-compiler', isAbsoluteLink: false },
  { label: 'Edit MIB File', to: '#', position: 'last' }
]))

const confirmCancel = () => {
  cancelConfirmationVisible.value = false
  router.push('/mib-compiler')
}

const confirmSave = async () => {
  if (!store.selectedMibFile) {
    showSnackBar({ msg: 'No MIB file selected for saving.', error: true })
    return
  }

  if (store.selectedMibFile.contents === textContent.value) {
    showSnackBar({ msg: 'No changes made to save.', error: true })
    return
  }

  try {
    const updatedFile = new File([textContent.value], store.selectedMibFile.name, { type: 'text/plain' })
    const arrayBuffer = await updatedFile.arrayBuffer()
    await setFileText(store.selectedMibFile.name, arrayBuffer)
    await store.fetchMibFiles()
    showSnackBar({ msg: 'MIB file saved successfully.' })
    saveConfirmationVisible.value = false
    router.push('/mib-compiler')
  } catch (error) {
    showSnackBar({ msg: 'Failed to save MIB file changes.', error: true })
    return
  }
}

const onCancel = () => {
  if (isDisabled.value) {
    router.push('/mib-compiler')
    return
  }
  cancelConfirmationVisible.value = true
}

const onSave = () => {
  if (isDisabled.value) {
    showSnackBar({ msg: 'No changes to save.', error: true })
    return
  }
  saveConfirmationVisible.value = true
}

onMounted(() => {
  if (store.selectedMibFile) {
    textContent.value = store.selectedMibFile.contents
  }
})
</script>

<style lang="scss" scoped>
@use '@featherds/styles/themes/variables';
@use '@featherds/styles/mixins/typography';

.edit-mib-file {
  padding: 20px;

  .edit-mib-file__content {
    background: var(variables.$surface);
    width: 100%;
    padding: 25px 0px;
    border-radius: 5px;

    .edit-mib-file__title {
      p {
        @include typography.headline1;
        margin: 0;
      }
    }

    .edit-mib-file__subtitle {
      p {
        @include typography.subtitle1;
        margin: 5px 0px 0px 0px;
      }
    }

    .edit-mib-file__caption {
      p {
        @include typography.headline3;
        margin: 5px 0px 20px 0px;
      }
    }

    .edit-mib-file__footer {
      display: flex;
      justify-content: flex-end;
      margin-top: 20px;
    }

    .edit-mib-file__no-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 20px;

      p {
        @include typography.body-large;
        margin: 20px 0px 0px 0px;
      }
    }
  }
}
</style>

