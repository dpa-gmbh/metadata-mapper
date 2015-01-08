# About

The metadata mapper is capable of extracting metadata out of an XML document and write this data 
into corresponding Information Interchange Model (IIM) records and Extensible Metadata 
Platform (XMP) entries of a given JPEG image. 

The extraction of metadata and its corresponding transformation to IIM and XMP has to be specified 
by a mapping. The implementation contains a mapping transforming data out of NewsML G2 Profile 2 
compliant documents to IIM and XMP according to IPTC "Photo Metadata" standard revision 1.2, 
October 2014. Custom mappings can be specified using a given XML schema.

The implementation contains an API (ImageMetadataOperation) as well as a Tool supporting simple
command-line based operations.


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
Usage: de.dpa.metadatamapper.MetadataMapper
  -inputImage (-i) [String] filename of input image
  -outputImage (-o) [String] filename of resulting image
  -g2doc (-d) [String] filename of input G2 document
  -mapping (-m) [String] 
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
   




