<template>
  <div class="p-d-flex p-flex-column p-flex-md-row file-upload">
    <FileUpload
    name="upload"
    @upload="onUpload"
    :multiple="false"
    :maxFileSize="1000000"
    >
    <template #empty>
      <p>Drag and drop file to upload.</p>
    </template>
  </FileUpload>
  </div>

  <h4 class="p-d-flex p-flex-column p-flex-md-row">Paste in text</h4>
  <h4 class="p-d-flex p-flex-column p-flex-md-row">Download sample file</h4>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import FileUpload from 'primevue/fileupload'
import { useStore } from 'vuex'

export default defineComponent({
  components: {
    FileUpload
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

