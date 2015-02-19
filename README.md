# About

The metadata mapper writes image metadata by extracting content elements from an XML 
document and mapping these elements to corresponding fields of the Information Interchange 
Model (IIM) records and Extensible Metadata Platform (XMP) of an JPEG image.
 
A metadata mapping is based on three files: an input XML, a JPEG file and a mapping configuration
file. The configuration contains XPath expressions and relates them to primitive and complex 
fields of XMP and IIM image metadata. In this way the metadata mapper knows which content to extract
out of the input XML file and how to map it to image metadata of the input JPEG file. The mapping
configuration is documented in [TODO].

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


# Building the Command Line Tool

Run 
```
mvn package
```
creates a jar containg all dependencies.

# Using the Command Line Tool

Calling the tool
```
java -jar target/metadata-mapper-1.0-SNAPSHOT.jar

** MetadataMapper - Copyright (c) 2015 dpa Deutsche Presse-Agentur GmbH
Usage: de.dpa.oss.metadata.mapper.MetadataMapper
  -inputImage (-i) [String] filename of input image
  -outputImage (-o) [String] filename of resulting image
  -g2doc (-d) [String] filename of input G2 document
  -validateMapping (-v) [String] Validate given mapping file
  -mapping (-m) [String] filename of mapping file. By default it uses dpa mapping
  -printCharacterMappingTable (-c) [flag] Outputs configured character mapping table. Does not perform any mapping. Uses default mapping file if argument -m is omittet
```

The distribution contains samples in *./example*. Call the tool using these samples as
follows:
```
java -jar target/metadata-mapper-1.0-SNAPSHOT.jar -i example/150105-99-07656.jpeg -o result.jpg -d example/g2-news-example.xml
```

This call uses the default mapping which is based on the IPTC "Photo Metadata" standard revision 1.2, 
October 2014.

To examine the result.jpg you can use one of the command line tools exiv2 (http://www.exiv2.org), 
exiftool (http://www.sno.phy.queensu.ca/~phil/exiftool/) or the firefox plugin Exif Viewer
(https://addons.mozilla.org/de/firefox/addon/exif-viewer/). It is a good idea to use multiple
tools simultaneously to evaluate the result.

You may provide your own metadata mapping. In this case take a look into the files
 ```
 example/metadata-mapping-1-0.xsd (underlying XML schema)
 example/iptc-metadata-mapping-2014-1.2.xml (mapping based on IPTC "Photo Metadata" standard 
   revision 1.2)  
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
   




