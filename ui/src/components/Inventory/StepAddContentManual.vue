<template>
  <div class="feather-row">
    <div class="feather-col-3">
      <FileUploader @uploadHandler="onUpload" filename="file" buttonCopy="Upload file" />
    </div>
  </div>

  <div class="feather-row">
    <div class="feather-col-12 headline3">Paste in text</div>
  </div>
  <div class="feather-row">
    <div class="feather-col-12 headline3">Download sample file</div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import FileUploader from '@/components/Common/FileUploader.vue'
import { useStore } from 'vuex'

export default defineComponent({
  components: {
    FileUploader
  },
  setup() {
    const store = useStore()

    const onUpload = () => {
      store.dispatch('inventoryModule/showAddStepNextButton', true)
    }

    return {
      onUpload
    }
  }
})
</script>
