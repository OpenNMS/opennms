var absolutePath = /^(?:\/|(?:[A-Za-z]:)?[\\|\/])/;
var relativePath = /^\.?\.\//;

function isAbsolute ( path ) {
        return absolutePath.test( path );
}

function isRelative ( path ) {
        return relativePath.test( path );
}

function normalize ( path ) {
        return path.replace( /\\/g, '/' );
}

function basename ( path ) {
        return path.split( /(\/|\\)/ ).pop();
}

function dirname ( path ) {
        var match = /(\/|\\)[^\/\\]*$/.exec( path );
        if ( !match ) return '.';

        var dir = path.slice( 0, -match[0].length );

        // If `dir` is the empty string, we're at root.
        return dir ? dir : '/';
}

function extname ( path ) {
        var match = /\.[^\.]+$/.exec( basename( path ) );
        if ( !match ) return '';
        return match[0];
}

function relative ( from, to ) {
        var fromParts = from.split( /[\/\\]/ ).filter( Boolean );
        var toParts = to.split( /[\/\\]/ ).filter( Boolean );

        while ( fromParts[0] && toParts[0] && fromParts[0] === toParts[0] ) {
                fromParts.shift();
                toParts.shift();
        }

        while ( toParts[0] === '.' || toParts[0] === '..' ) {
                var toPart = toParts.shift();
                if ( toPart === '..' ) {
                        fromParts.pop();
                }
        }

        while ( fromParts.pop() ) {
                toParts.unshift( '..' );
        }

        return toParts.join( '/' );
}

function resolve () {
        var paths = [], len = arguments.length;
        while ( len-- ) paths[ len ] = arguments[ len ];

        var resolvedParts = paths.shift().split( /[\/\\]/ );

        paths.forEach( function (path) {
                if ( isAbsolute( path ) ) {
                        resolvedParts = path.split( /[\/\\]/ );
                } else {
                        var parts = path.split( /[\/\\]/ );

                        while ( parts[0] === '.' || parts[0] === '..' ) {
                                var part = parts.shift();
                                if ( part === '..' ) {
                                        resolvedParts.pop();
                                }
                        }

                        resolvedParts.push.apply( resolvedParts, parts );
                }
        });

        return resolvedParts.join( '/' ); // TODO windows...
}

window.path = { absolutePath: absolutePath, relativePath: relativePath, isAbsolute: isAbsolute, isRelative: isRelative, normalize: normalize, basename: basename, dirname: dirname, extname: extname, relative: relative, resolve: resolve };
