export interface MenuItemDefinition {
  name: string
  url: string
  icon: string
  isAbsoluteUrl: boolean
  isVueLink: boolean
  items: MenuItemDefinition[]
}

export interface MainMenuDefinition {
  displayAdminLink: boolean
  countNoticesAssignedToUser: number
  countNoticesAssignedToOtherThanUser: number
  noticesAssignedToUserLink: string
  noticesAssignedToOtherThanUserLink: string
  noticeStatus: string
  adminLink: string
  rolesLink: string
  quickAddNodeLink: string
  searchLink: string
  selfServiceLink: string
  username: string
  menuItems: MenuItemDefinition[]
}

export interface NoticeStatusDisplay {
  icon: string
  colorClass: string
  title: string
}