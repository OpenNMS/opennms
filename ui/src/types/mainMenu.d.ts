import { StringifyOptions } from "querystring"

export interface MenuItemDefinition {
  name: string
  url: string
  icon: string
  isVueLink: boolean
  isAbsoluteUrl: boolean
  items: MenuItemDefinition[]
}

export interface MainMenuDefinition {
  noticeStatus: string
  displayAdminLink: boolean
  username: string
  countNoticesAssignedToUser: number
  countNoticesAssignedToOtherThanUser: number
  selfServiceLink: string,
  noticesAssignedToUserLink: string,
  noticesAssignedToOtherThanUserLink: string,
  rolesLink: string,
  quickAddNodeLink: string,
  adminLink: string,
  menuItems: MenuItemDefinition[]
}

export interface NoticeStatusDisplay {
  icon: string,
  colorClass: string,
  title: string
}