Running the new UI:

1. Start OpenNMS
2. Install all necessary 3rd-party tools
    1. Install node.js: http://nodejs.org/
    2. Install compass/sass: `sudo gem update --system; sudo gem install compass`
    3. Install the node.js tools for the JS dev environment: `sudo npm install -g grunt grunt-cli bower`
    4. Install the node.js dependencies: `npm install`
    5. Install the web dependencies: `bower install`
3. Start the server: `grunt serve`
