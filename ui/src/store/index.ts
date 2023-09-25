import { createStore } from 'vuex'

// store modules
import deviceModule from './device'
import fileEditorModule from './fileEditor'
import graphModule from './graph'
import ifServicesModule from './ifServices'
import ipInterfacesModule from './ipInterfaces'
import mapModule from './map'
import resourceModule from './resource'
import scvModule from './scv'
import searchModule from './search'

export default createStore({
  modules: {
    deviceModule,
    fileEditorModule,
    graphModule,
    ifServicesModule,
    ipInterfacesModule,
    mapModule,
    resourceModule,
    scvModule,
    searchModule
  }
})
