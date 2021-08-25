<template>
  <div id="nav">
    <router-link :to="{ name: 'Home' }">Home</router-link> |
    <router-link :to="{ name: 'MapNodes' }">Map</router-link>
  </div>
  <router-view />
</template>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

#nav {
  padding: 1.2%;
  background: #0a0c1b;
  text-align: right;
}

#nav a {
  font-family: Arial;
  font-size: 14px;
  /* font-weight: bold; */
  color: #b4b6c8;
}

#nav a.router-link-exact-active {
  font-size: 15px;
  font-weight: bold;
  color: #E9EBF9;
}
</style>
