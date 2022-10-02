export interface MenuItem {
  name: string
  url: string
  icon: string
  isAbsoluteUrl: boolean
  isVueLink: boolean
}

export interface TopMenuItem extends MenuItem {
  items: MenuItem[]
}

export interface MainMenu {
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
  menuItems: TopMenuItem[]
}

export interface NoticeStatusDisplay {
  icon: string
  colorClass: string
  title: string
}