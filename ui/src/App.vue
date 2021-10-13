  
<template>
  <Menubar />
  <Sidebar />
  <div id="view">
    <router-view />
    <notifications position="bottom right" duration=200 closeOnClick=true ignoreDuplicates=true />
  </div>
</template>
  
<script setup lang="ts">
import Menubar from './components/Layout/Menubar.vue'
import Sidebar from './components/Layout/Sidebar.vue'
import { notify } from "@kyvg/vue3-notification"

</script>
  
<style lang="scss">
body {
  margin: 0px;
}
input.p-dropdown-label {
  cursor: auto !important;
}
#view {
  padding: 30px;
  height: calc(100vh - 65px);
  overflow-y: scroll;
  margin-left: 230px;
}
</style>

<style lang="scss">
// theme colours
$primary-blue: rgba(
  $color: #0081ad,
  $alpha: 1
);
$primary-green: rgba(
  $color: #85d9a5,
  $alpha: 1
);
$primary-dark-blue: rgba(
  $color: #131736,
  $alpha: 1
);

$primary-sky-blue: rgba(
  $color: #14d1df,
  $alpha: 1
);
$secondary-sky-blue: rgba(
  $color: $primary-sky-blue,
  $alpha: 0.5
);
$tertiary-sky-blue: rgba(
  $color: $primary-sky-blue,
  $alpha: 0.25
);

// utility classes
.bg-primary-green {
  background: $primary-green !important;
}
.bg-primary-blue {
  background: $primary-blue !important;
}
.bg-tertiaty-sky-blue {
  background: $tertiary-sky-blue !important;
}

h1,
h2,
h3,
h4,
h5,
h6 {
  font-family: "Segoe UI", Roboto, Helvetica;
}
a {
  text-decoration: none;
  color: $primary-blue;
}
.flex-container {
  padding: 0;
  margin: 0;
  list-style: none;
  display: flex;
}
.space-between {
  justify-content: space-between;
}
.space-evenly {
  justify-content: space-evenly;
}
.top-10 {
  margin-top: 10px;
}
.page {
  max-width: 2400px;
  margin: auto;
}
.pointer {
  cursor: pointer !important;
}

// header theme
.p-datatable-header,
.p-panel-header {
  background: $primary-blue !important;
  color: white !important;
}

// Button theme
.p-button-primary,
.p-fileupload-buttonbar > .p-button {
  background: $primary-blue !important;
  border: none !important;
  &:hover {
    background: rgba($color: $primary-blue, $alpha: 0.8) !important;
  }
}
.p-button-success {
  background: $primary-green !important;
  border: none !important;
  &:hover {
    background: rgba($color: $primary-green, $alpha: 0.8) !important;
  }
}
.p-button-raised,
.p-button-text {
  color: $primary-dark-blue !important;
}
</style>
