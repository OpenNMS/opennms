Apparently the jung api and impl packages have the same packages.
This does not play very nice with OSGi, because the first bundle exporting the package always wins.
It is not possible to have two modules export the same packages.
Therefore this module embeds them nicely \o/