# About

The metadata mapper writes image metadata by extracting content elements from an XML 
document and mapping these elements to corresponding fields of the Information Interchange 
Model (IIM) records and Extensible Metadata Platform (XMP) of an JPEG image.
 
A metadata mapping is based on three files: an input XML, a JPEG file and a mapping configuration
file. The configuration contains XPath expressions and relates them to primitive and complex 
fields of XMP and IIM image metadata. In this way the metadata mapper knows which content to extract
out of the input XML file and how to map it to image metadata of the input JPEG file. The mapping
configuration is documented in the [Metadata Mapper Documentation](https://dpa-gmbh.github.io/metadata-mapper/).

XML documents may contain encodings which are not suitable for the target image metadata field. 
Or in some cases you may have XML documents with mixed character sets and you do not want
take over this mixture in the metadata of image. In such cases you may want to map and encode 
characters accordingly. The metadata mapper also supports configurable encoding and mapping 
of characters. 

The implementation contains a mapping configuration which supports extraction of content 
out of NewsML G2 Profile 2 compliant documents and mapping it to to IIM and XMP fields
according to IPTC "Photo Metadata" standard revision 1.2, October 2014. The source code contains 
an API (ImageMetadataOperation) as well as a Tool supporting simple command-line based operations.


# Prerequisites

The metadata mapper uses the exiftool (<http://www.sno.phy.queensu.ca/~phil/exiftool/>) to modify the
images accordingly. This tool has to be either in the execution path oder be referred by an environment
variable named EXIFTOOL
 <pre>
 export EXIFTOOL=/myinstallhome/exiftool
 </pre>
Alternatively you may setup the java system property
 <pre>
 -Dexiftool.path=/myinstallhome/exiftool
 </pre>
or directly call the wrapper in order to specify the path:
 <pre>
 de.dpa.oss.metadata.mapper.imaging.backend.exiftool.ExifTool.setPathToExifTool("/myinstallhome/exiftool")
 </pre>

# Building the Command Line Tool

Run
```
mvn -P executable clean package 
```
creates a jar containg all dependencies needed by the commandline tool

# Building the jar file

Run 
```
mvn clean package
```
creates a the jar for embedding the mapper into own projects


# Using the Command Line Tool

Calling the tool
```
java -jar target/metadata-mapper-${project.version}.jar

** MetadataMapper - Copyright (c) 2015 dpa Deutsche Presse-Agentur GmbH
Usage: de.dpa.oss.metadata.mapper.MetadataMapperCmd
  -inputImage                 (-i) [String] Filename of input image
  -outputImage                (-o) [String] Filename of resulting image
  -g2doc                      (-d) [String] Filename of input G2 document
  -validateMapping            (-v) [String] Validate given mappingCustomization file
  -mappingCustomization       (-m) [String] Mapping customization file which is used to 
                                            override and/or enhance the default 
                                            mappingCustomization. By default it uses dpa 
                                            mappingCustomization
  -printCharacterMappingTable (-c) [flag]   Outputs configured character 
                                            mappingCustomization table. Does not perform 
                                            any mappingCustomization. Uses default 
                                            mappingCustomization file if argument -m 
                                            is omitted
  -exiftoolPath               (-t) [String] Path to exiftool. Alternatively you may set 
                                            environment variable EXIFTOOL
  -emptyTagGroupBeforeMapping (-e) [flag]   Removes all tags from those tag groups which 
                                            are  
                                            used by the mappingCustomization. By default 
                                            mapped tag values will be merged with 
                                            existing tags
  -removeTagGroups            (-r) [String] Comma separated list of metadata tag groups 
                                            to clear before mapping. The syntax needs 
                                            to match the exiftool syntax to specify 
                                            containers: TAG_GROUP:TAG. 
                                            For a list of available containers see 
                                            exiftool. Example: -r IPTC:ALL,XMP:XMP-dc
  -removeAllTagGroups         (-R) [flag]   Removes all metadata from given file before 
                                            processing
  -explainMapping             (-x) [flag]   Experimental feature: Dumps mapping 
                                            information based on a given document. 
                                            At present state the output for XMP is not 
                                            complete
  -help                       (-h) [flag] 
```

The distribution contains samples in *./example*. Call the tool using these samples as
follows:
```
java -jar target/metadata-mapper-${project.version}.jar -i example/150105-99-07656.jpeg -o result.jpg -d example/g2-news-example.xml
```

This call uses the default mapping which is based on the IPTC "Photo Metadata" standard revision 1.2, 
October 2014.

To examine the result.jpg you can use one of the command line tools exiv2 (http://www.exiv2.org), 
exiftool (http://www.sno.phy.queensu.ca/~phil/exiftool/) or the firefox plugin Exif Viewer
(https://addons.mozilla.org/de/firefox/addon/exif-viewer/). It is a good idea to use multiple
tools simultaneously to evaluate the result.

You may provide customizations or enhancements of the default mappings. In this case specify the mapping file using
the option "-m". It loads the customizations and adds all those default mappings which are not overriden by the
customization. See e. g.

 ```
 example/dpa-mapping.xml
 ```

 Use the parameter "-m" to specify the path to your own mapping configuration.
  
# License
The MIT License (MIT) (http://opensource.org/licenses/MIT)

Copyright (c) 2015 dpa Deutsche Presse-Agentur GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions: The above copyright notice and this permission notice shall
be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
   




