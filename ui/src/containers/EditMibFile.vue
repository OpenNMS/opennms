<template>
  <div class="edit-mib-file-container">
    <div class="row">
      <BreadCrumbs :items="breadcrumbs" />
    </div>
    <div class="header">
      <div class="heading">
        <p data-test="edit-title">Edit {{ fileName }}</p>
      </div>
      <div class="actions">
        <OnmsButton
          label="Save"
          :disabled="isLoading || !isModified"
          data-test="save-button"
          @click="save"
        />
        <OnmsButton
          label="Cancel"
          variant="outlined"
          data-test="cancel-button"
          @click="goBack"
        />
      </div>
    </div>
    <div class="editor-container">
      <VAceEditor
        v-model:value="content"
        lang="text"
        :theme="theme"
        style="height: 100%"
        :printMargin="false"
        @init="onEditorInit"
      />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { OnmsButton } from '@opennms/onms-ui'
import { VAceEditor } from 'vue3-ace-editor'
import 'ace-builds/src-noconflict/mode-text'
import 'ace-builds/src-noconflict/theme-xcode'
import 'ace-builds/src-noconflict/theme-dracula'
import BreadCrumbs from '@/components/Layout/BreadCrumbs.vue'
import useSnackbar from '@/composables/useSnackbar'
import { getGeneralErrorMessage } from '@/components/MibCompiler/mibFilesValidator'
import { getMibFileContent, updatePendingMibFile } from '@/services/mibCompilerService'
import { useAppStore } from '@/stores/appStore'
import { useMenuStore } from '@/stores/menuStore'
import { BreadCrumb } from '@/types'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const menuStore = useMenuStore()
const snackbar = useSnackbar()

const fileName = computed(() => String(route.query.name ?? ''))
const content = ref('')
const originalContent = ref('')
const isLoading = ref(false)

const isModified = computed(() => content.value !== originalContent.value)
const theme = computed(() => (appStore.theme === 'open-dark' ? 'dracula' : 'xcode'))

const homeUrl = computed<string>(() => menuStore.mainMenu?.homeUrl)
const breadcrumbs = computed<BreadCrumb[]>(() => ([
  { label: 'Home', to: homeUrl.value, isAbsoluteLink: false },
  { label: 'MIB Compiler', to: '/mib-compiler', isAbsoluteLink: false },
  { label: fileName.value, to: '#', position: 'last' }
]))

const onEditorInit = (editor: { setFontSize: (size: number) => void }) => {
  editor.setFontSize(14)
}

const goBack = () => {
  router.push('/mib-compiler')
}

const save = async () => {
  isLoading.value = true
  try {
    await updatePendingMibFile(fileName.value, content.value)
    snackbar.showSnackBar({ msg: `'${fileName.value}' saved successfully.` })
    originalContent.value = content.value
    goBack()
  } catch (error: unknown) {
    snackbar.showSnackBar({ msg: getGeneralErrorMessage(error, `Failed to save '${fileName.value}'.`), error: true })
  } finally {
    isLoading.value = false
  }
}

onMounted(async () => {
  if (!fileName.value) {
    goBack()
    return
  }
  isLoading.value = true
  try {
    const text = await getMibFileContent('pending', fileName.value)
    content.value = text
    originalContent.value = text
  } catch (error: unknown) {
    snackbar.showSnackBar({ msg: getGeneralErrorMessage(error, `Failed to load '${fileName.value}'.`), error: true })
    goBack()
  } finally {
    isLoading.value = false
  }
})
</script>

<style lang="scss" scoped>
.edit-mib-file-container {
  padding: 20px;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 30px 40px 15px 40px;

    .heading {
      p {
        font-size: 24px;
        font-weight: 600;
        margin: 0;
      }
    }

    .actions {
      display: flex;
      gap: 10px;
    }
  }

  .editor-container {
    margin: 0 40px;
    height: calc(100vh - 260px);
    border: 1px solid var(--p-content-border-color);
  }
}
</style>
