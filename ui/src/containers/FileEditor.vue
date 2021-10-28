<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">
        <div class="feather-row">
          <div class="feather-col-3">
            <Files />
          </div>
          <div class="feather-col-9">
            <Editor />
          </div>
          <Snippets />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from "vue"
import { useStore } from 'vuex'
import Editor from '@/components/FileEditor/Editor.vue'
import Files from '@/components/FileEditor/Files.vue'
import Snippets from '@/components/FileEditor/Snippets.vue'
const store = useStore()
onMounted(() => store.dispatch('fileEditorModule/getFileNames'))
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  background: var(--feather-surface);
  margin: 15px;
  padding: 15px;
}
.feather-row {
  flex-wrap: nowrap;
}
</style>
