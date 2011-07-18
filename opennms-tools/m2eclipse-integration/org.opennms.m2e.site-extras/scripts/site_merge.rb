#!/usr/bin/ruby -w

require 'rexml/document'
include REXML

main_sitexml = ARGV[0]
release_sitexml = ARGV[1]
main = Document.new( File.new( main_sitexml ) )
release = Document.new( File.new( release_sitexml ) )
last_main_feature = main.root.elements['//feature[last()]']
release_features = release.root.elements.each('//feature'){}
release_features.reverse_each{ |e| last_main_feature.parent.insert_after( last_main_feature, e ) }
main.write( $stdout, 2 )
