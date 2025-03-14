export interface ZenithConnectRegisterResponse {
  success: boolean
  nmsSystemId: string
  nmsDisplayName: string
  accessToken: string
  refreshToken: string
}

export interface ZenithConnectRegistration extends ZenithConnectRegisterResponse{
  id: string
  registrationDate?: Date
  lastConnected?: Date
  connected: boolean
}
