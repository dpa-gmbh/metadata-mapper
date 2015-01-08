package de.dpa.metadatamapper.imaging.common;

import com.adobe.xmp.XMPException;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.junit.Test;

import java.io.IOException;

public class ImageMetadataWriterTest
{
    @Test
    public void shouldWriteSingleIptcEntry() throws ImageWriteException, IOException, XMPException, ImageReadException
    {
       /* FIXME TODO // given
        ArgumentCaptor<PhotoshopApp13Data> writtenMetadata = ArgumentCaptor.forClass(PhotoshopApp13Data.class);
        ArgumentCaptor<InputStream> isCaptor = ArgumentCaptor.forClass( InputStream.class );
        ArgumentCaptor<OutputStream> osCaptor = ArgumentCaptor.forClass( OutputStream.class );
        JpegIptcRewriter iptcRewriter = mock(JpegIptcRewriter.class);
        JpegXmpRewriter xmpRewriter = mock(JpegXmpRewriter.class);

        // when
        new ImageMetadataWriter(xmpRewriter,iptcRewriter).write( mock(InputStream.class), imageMetadata, mock(OutputStream.class));

        // then
        verify( iptcRewriter).writeIPTC(isCaptor.capture(), osCaptor.capture(), writtenMetadata.capture());
        final PhotoshopApp13Data writtenMetadataValue = writtenMetadata.getValue();
        assertThat(writtenMetadataValue.getRecords(), hasSize(1));
        final IptcRecord iptcRecord = writtenMetadataValue.getRecords().get(0);
        assertThat( iptcRecord.getValue(), is("document title")) ;
        assertThat( iptcRecord.getIptcTypeName(), is(IptcTypes.OBJECT_NAME.getName())) ;
        */
    }

    @Test
    public void shouldWriteMultivalueIptcEntry() throws ImageWriteException, IOException, XMPException, ImageReadException
    {
        /* TODO FIXME

        // given
        MetadataRecord metadataRecord = new MetadataRecord();
        metadataRecord.addEntry( MetadataEntry.KEYWORDS, "keyword1", "keyword2" );
        ImageMetadata imageMetadata = new MetadataRecordToImageMetadataMapper().map(metadataRecord);
        ArgumentCaptor<PhotoshopApp13Data> writtenMetadata = ArgumentCaptor.forClass(PhotoshopApp13Data.class);
        ArgumentCaptor<InputStream> isCaptor = ArgumentCaptor.forClass( InputStream.class );
        ArgumentCaptor<OutputStream> osCaptor = ArgumentCaptor.forClass( OutputStream.class );
        JpegIptcRewriter iptcRewriter = mock(JpegIptcRewriter.class);
        JpegXmpRewriter xmpRewriter = mock(JpegXmpRewriter.class);

        // when
        new ImageMetadataWriter(xmpRewriter,iptcRewriter).write( mock(InputStream.class), imageMetadata, mock(OutputStream.class));

        // then
        verify( iptcRewriter).writeIPTC(isCaptor.capture(), osCaptor.capture(), writtenMetadata.capture());
        final PhotoshopApp13Data writtenMetadataValue = writtenMetadata.getValue();
        assertThat(writtenMetadataValue.getRecords(), hasSize(2));
        IptcRecord iptcRecord = writtenMetadataValue.getRecords().get(0);
        assertThat( iptcRecord.getValue(), is("keyword1")) ;
        assertThat( iptcRecord.getIptcTypeName(), is(IptcTypes.KEYWORDS.getName())) ;
        iptcRecord = writtenMetadataValue.getRecords().get(1);
        assertThat( iptcRecord.getValue(), is("keyword2")) ;
        assertThat( iptcRecord.getIptcTypeName(), is(IptcTypes.KEYWORDS.getName())) ;
*/    }

    @Test
    public void shouldWriteSingleXMPEntry() throws Exception
    {
        /*
        TODO FIXME
        // given
        MetadataRecord metadataRecord = new MetadataRecord();
        metadataRecord.addEntry( MetadataEntry.TITLE, "document title" );
        ImageMetadata imageMetadata = new MetadataRecordToImageMetadataMapper().map(metadataRecord);
        ArgumentCaptor<String> writtenMetadata = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InputStream> isCaptor = ArgumentCaptor.forClass( InputStream.class );
        ArgumentCaptor<OutputStream> osCaptor = ArgumentCaptor.forClass( OutputStream.class );
        JpegIptcRewriter iptcRewriter = mock(JpegIptcRewriter.class);
        JpegXmpRewriter xmpRewriter = mock(JpegXmpRewriter.class);

        // when
        new ImageMetadataWriter(xmpRewriter, iptcRewriter).write( mock(InputStream.class), imageMetadata, mock(OutputStream.class));

        // then
        verify( xmpRewriter).updateXmpXml(isCaptor.capture(), osCaptor.capture(), writtenMetadata.capture());
        Document document = XmlUtils.toDocument(writtenMetadata.getValue());
        assertThat(document, hasXPath("/xmpmeta/RDF/Description/title"));
        assertThat(document, hasXPath("/xmpmeta/RDF/Description/title", is("document title")));*/
    }

    @Test
    public void shouldWriteMultiValueXMPEntry()
            throws Exception
    {
        /*
        TODO FIXME
        // given
        MetadataRecord metadataRecord = new MetadataRecord();
        metadataRecord.addEntry( MetadataEntry.KEYWORDS, "keyword1", "keyword2" );
        ImageMetadata imageMetadata = new MetadataRecordToImageMetadataMapper().map(metadataRecord);
        ArgumentCaptor<String> writtenMetadata = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InputStream> isCaptor = ArgumentCaptor.forClass( InputStream.class );
        ArgumentCaptor<OutputStream> osCaptor = ArgumentCaptor.forClass( OutputStream.class );
        JpegIptcRewriter iptcRewriter = mock(JpegIptcRewriter.class);
        JpegXmpRewriter xmpRewriter = mock(JpegXmpRewriter.class);

        // when
        new ImageMetadataWriter(xmpRewriter,iptcRewriter).write(mock(InputStream.class), imageMetadata, mock(OutputStream.class));

        // then
        verify( xmpRewriter).updateXmpXml(isCaptor.capture(), osCaptor.capture(), writtenMetadata.capture());
        Document document = XmlUtils.toDocument(writtenMetadata.getValue());

        assertThat(writtenMetadata.getValue(), containsString("<dc:Subject>"));
        assertThat(document, hasXPath("/xmpmeta/RDF/Description/Subject"));
        assertThat(document, hasXPath("/xmpmeta/RDF/Description/Subject/Bag/li"));
        assertThat(document, hasXPath("/xmpmeta/RDF/Description/Subject/Bag/li", is("keyword1")));
        assertThat(document, hasXPath("/xmpmeta/RDF/Description/Subject/Bag/li[2]", is("keyword2")));*/
    }
}