<template>
  <div class="feather-row">
    <div class="feather-col-12">
      <div class="card">

        <TopBar v-if="isHelpOpen" />

        <div class="feather-row">
          <transition name="fade">
            <div class="feather-col-3" v-if="!isHelpOpen">
              <FileSidebar />
            </div>
          </transition>

          <div :class="`feather-col-${isHelpOpen ? 8 : 9}`">
            <Editor />
          </div>

          <transition name="fade">
            <div class="feather-col-4" v-if="isHelpOpen">
              <Help />
            </div>
          </transition>

          <FeatherButton
            v-if="!isHelpOpen && snippets"
            class="help-btn"
            text
            @click="triggerHelp">
            Help
          </FeatherButton>
        </div>
      </div>
    </div>
  </div>
  <ConfimDialog />
</template>

<script setup lang="ts">
import { useStore } from 'vuex'
import { FeatherButton } from '@featherds/button'
import Editor from '@/components/FileEditor/Editor.vue'
import FileSidebar from '@/components/FileEditor/FileSidebar.vue'
import Help from '@/components/FileEditor/Help.vue'
import TopBar from '@/components/FileEditor/TopBar.vue'
import ConfimDialog from '@/components/FileEditor/ConfimDialog.vue'
const store = useStore()
const isHelpOpen = computed(() => store.state.fileEditorModule.isHelpOpen)
const snippets = computed(() => store.state.fileEditorModule.snippets)
const triggerHelp = () => store.dispatch('fileEditorModule/setIsHelpOpen', true)
onMounted(() => {
  store.dispatch('fileEditorModule/getFileNames')
  store.dispatch('fileEditorModule/getFileExtensions')
})
</script>

<style scoped lang="scss">
@import "@featherds/styles/mixins/typography";
@import "@featherds/styles/mixins/elevation";
.card {
  @include elevation(2);
  background: var($surface);
  padding: 15px;
  position: relative;
}
.feather-row {
  flex-wrap: nowrap;
}
.help-btn {
  position: absolute;
  right: 30px;
  top: 0px;
}
.fade-enter-active {
  transition: opacity 0.7s ease;
}

.fade-enter-from {
  opacity: 0;
}
</style>
