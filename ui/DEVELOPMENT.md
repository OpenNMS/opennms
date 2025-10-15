# Development Notes for OpenNMS Vue UI

## .env file

The `.env` file (or `.env.development`) contains some compile-time environment-specific configuration parameters.

- `VITE_BASE_V2_URL`: URL to the OpenNMS Rest Service v2 endpoint. Should be relative to the base URL (`VITE_BASE_URL`). Default: `/opennms/api/v2`

- `VITE_BASE_REST_URL`: URL to the OpenNMS Rest Service endpoint. Should be relative to the base URL (`VITE_BASE_URL`). Default: `/opennms/rest`

- `VITE_BASE_URL`: URL to the OpenNMS Rest Service endpoint. Should be relative to the base URL (`VITE_BASE_URL`). Default: `/opennms/rest`

- `VITE_BASE_URL`: Absolute base URL of the web app. Default: `http://localhost:8980`

- `VITE_MENU_APP_MOUNT_ID`: Id of the `div` element hosting the Vue menu application. Default: `opennms-sidemenu-container`

- `VITE_APP_LOGO_NAME`: Base logo name for the main logo at the top of the application.
  Default: `LogoHorizon` for Horizon. May be different for different products.
  The file should be a Vue file in the `src/assets` directory which contains `svg` code for the logo.
