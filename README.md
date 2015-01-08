# About

The metadata mapper is capable of extracting metadata out of an XML document and write this data 
into corresponding Information Interchange Model (IIM) records and Extensible Metadata 
Platform (XMP) entries of a given JPEG image. 

The extraction of metadata and its corresponding transformation to IIM and XMP has to be specified 
by a mapping. The implementation contains a mapping transforming data out of NewsML G2 Profile 2 
compliant documents to IIM and XMP according to IPTC "Photo Metadata" standard revision 1.2, 
Oktober 2014. Custom mappings can be specified using a given XML schema.

The implementation contains an API (ImageMetadataOperation) as well as a Tool supporting simple
command-line based operations.

# Building the Command-Line Tool

Run 
```
mvn package
```
creates a jar containg all dependencies.

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
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
   




