<template>
  <div class="feather-row file-upload">
    <FileUploader @uploadHandler="onUpload" filename="file" buttonCopy="Upload file" />
  </div>

  <h4 class="feather-row">Paste in text</h4>
  <h4 class="feather-row">Download sample file</h4>
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

<style scoped lang="scss">
  .file-upload {
    margin-top: 30px;
  }
</style>

