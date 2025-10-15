# Menu Templates

## Overview

The side menu organization is configured by the use of JSON menu templates.

Currently these can be found in the `opennms/jetty-webapps/opennms/WEB-INF/menu` folder in your installation.

The `menu-template.json` file controls the menu configuration.

It is read by the Rest API v2 Menu service and displayed by the UI.

Configuration changes made in the file will take effect as soon as the browser page is refreshed (no need to restart OpenNMS).

Currently there are 3 menu templates:

- `menu-template-default.json`: This is the default template (the same as the `menu-template.json` that is installed by default) and represents our latest recommended design.

- `menu-template-alt.json` is an alternative configuration.

- `menu-template-legacy.json` organizes the menu similar to previous versions of OpenNMS.

To use any of these menus, simply copy one of them to `menu-template.json`.

If you wish to create your own configuration, we recommend creating a new file, then copying it to `menu-template.json`.


## Template structure

The menu template structure can be found in the Java code in: `org.opennms.web.rest.support.menu.model.MainMenu` and `org.opennms.web.rest.support.menu.model.MenuEntry`.

<table>
  <tr>
    <th>Item</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>templateName</td>
    <td>A name for the template.</td>
  </tr>
  <tr>
    <td>baseHref, homeUrl, formattedDateTime, formattedDate, formattedTime,
  noticeStatus, username, baseNodeUrl, zenithConnectEnabled, zenithConnectBaseUrl,
  zenithConnectRelativeUrl, copyrightDates, version</td>
    <td>These are filled in at runtime with values. Any values you enter will be ignored.</td>
  </tr>
  <tr>
    <td>displayAddNodeButton</td>
    <td>Whether to display the "Add A Node" button on the top menu.</td>
  </tr>
  <tr>
    <td>sideMenuInitialExpand</td>
    <td>Whether the side menu is initially expanded.</td>
  </tr>
  <tr>
    <td>helpMenu, selfServiceMenu, userNotificationMenu, provisionMenu, flowsMenu, configurationMenu</td>
    <td>Control some specific menus. Most of these are deprecated and may be removed in a future version, we suggest not editing them.</td>
  </tr>
  <tr>
    <td>menus</td>
    <td>An array of top level menus, containing menu items. This is the place to configure what is displayed in the side menu, see the table below.</td>
  </tr>
</table>


## Top level menu item configuration

Within the `menus` array, each item is a top-level menu item for the side menu.

<table>
  <tr>
    <th>Item</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>id</td>
    <td>A unique id for the top-level menu item.  These are used in the HTML markup so we recommend using only something
    suitable for an HTML element id.
    For top-level items, the convention is to have a suffix of <code>Menu</code>, e.g. <code>administrationMenu</code>, <code>operationsMenu</code>, etc.
    </td>
  </tr>
  <tr>
    <td>name</td>
    <td>The name of the top-level menu item which is displayed in the menu UI.</td>
  </tr>
  <tr>
    <td>url</td>
    <td>The URL that the top-level menu links to.
    This can be empty or left as <code>"#"</code> if there are subitems.
    If you want clicking on the top level menu item to directly link to a page or URL, set <code>url</code>
    and also add an <code>action</code> property: <code>"action": "link"</code>. Also leave <code>items</code> as <code>null</code>.
    If you want this to link to an external URL outside of OpenNMS, set this property: <code>"isExternalLink": true</code>.
    You cannot set this link and have submenu items at the same time.
    </td>
  </tr>
  <tr>
    <td>action</td>
    <td>See <code>url</code>, this treat the top-level menu item as a link.</td>
  </tr>
   <tr>
    <td>locationMatch</td>
    <td>Used by the Search feature.</td>
  </tr>
  <tr>
    <td>icon</td>
    <td>An icon path ID for the icon to display.
    These must be Feather icons, see https://feather.nanthealth.com/Components/Icon/ for a list. For example <code>"action/AccountCircle"</code>.
    Note that currently only some of these icons are actually available to be used
    in the menu templates.
    You can use any icons that are currently used in the provided menu template files, and more will be added in the future.
    Currently icons are only available for the top-level menu items.
    </td>
  </tr>
  <tr>
    <td>roles</td>
    <td>An array of roles that the user must have in order to see the menu item.
      If this is <code>null</code> or <code>[]</code>, then the menu item is always displayed.
      The roles match those found in user configuration.
      If roles are specified, the user needs to have at least one of the given roles.
      Example: to restrict the menu item to be seen only by administrators, specify <code>"roles": ["ROLE_ADMIN"]</code>.
      Note that <code>roles</code> is usually specified on the menu item rather than on the top-level menu item.
    </td>
  </tr>
  <tr>
    <td>items</td>
    <td>An array of individual menu items, see the table below.</td>
  </tr>
  <tr>
    <td>type</td>
    <td>The type of the top-level menu item. The default is <code>item</code>; you can just
    omit this <code>type</code> field if this is a normal menu item.
    The other option is <code>separator</code> which will draw a horizontal separator line,
    useful for grouping the menu items. We may add <code>header</code> in the future to display a text header which isn't a menu item.
    </td>
  </tr>
</table>


## Menu item configuration

The `items` under each top level menu contain the individual menu items.


<table>
  <tr>
    <th>Item</th>
    <th>Description</th>
  </tr>

  <tr>
    <td>id</td>
    <td>A unique ID for the menu item.</td>
  </tr>

  <tr>
    <td>name</td>
    <td>The display name for the menu item.</td>
  </tr>

  <tr>
    <td>url</td>
    <td>The url that the menu item links to.
    If it is non-external, should be a relative link under the main <code>/opennms</code> path.
    Example: <code>"url": "element/index.jsp"</code> to link to the Search Inventory page.
    If it is an external link, provide the entire absolute URL, e.g. <code>"url": "https://www.opennms.com"</code>.</td>
  </tr>

  <tr>
    <td>locationMatch</td>
    <td>See above.</td>
  </tr>

  <tr>
    <td>roles</td>
    <td>See above.
    Set to <code>null</code> if the item should always be displayed regardless of the user's role.</td>
  </tr>

  <tr>
    <td>isExternalLink</td>
    <td>If <code>true</code>, then the <code>url</code> is treated as an external, absolute link.
    This is optional and can be omitted.</td>
  </tr>

  <tr>
    <td>linkTarget</td>
    <td>If present, specifies a target for the link, like the HTML <code>target</code> attribute.
    Currently only <code>_blank</code> and <code>_self</code> are supported; <code>_self</code> is the default so does not need to be specified. This is optional and can be omitted.</td>
  </tr>

  <tr>
    <td>icon</td>
    <td>See above.
    However, currently this is only supported for the top-level menu items.</td>
  </tr>

  <tr>
    <td>requiredSystemProperties</td>
    <td>This is an array of name/value pairs, where the <code>name</code> is a property name in an OpenNMS properties file
    (e.g. <code>/etc/opennms.properties</code> or <code>etc/opennms.properties.d/*.properties</code>) and the <code>value</code>
    is the value it needs to be in order to display the menu item.  See below for an example.</td>
  </tr>

  <tr>
    <td>action</td>
    <td>Used for performing some special actions when the item is clicked,
    rather than navigating to a link.
    Currently the only action is <code>logout</code> which will log out the user.
    Generally this is omitted and <code>url</code> is used instead.</td>
  </tr>
</table>

Here the `requiredSystemProperties` is used to display this menu item only if
`opennms.zenithConnect.enabled=true` is found in a `.properties` file.

Example of `requiredSystemProperties`:

```
{
  "id": "zenithConnect",
  "name": "Zenith Connect",
  // other properties...
  "requiredSystemProperties": [
    {
      "name": "opennms.zenithConnect.enabled",
      "value": "true"
    }
  ]
}
```
