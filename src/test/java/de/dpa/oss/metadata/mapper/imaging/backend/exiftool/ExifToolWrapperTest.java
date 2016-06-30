package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo.TagGroup;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo.TagInfo;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.fail;

public class ExifToolWrapperTest
{
    @Test
    public void shouldReturnListOfGroups()
            throws IOException, ParserConfigurationException, SAXException, JAXBException, ExifToolIntegrationException
    {
        // given
        // when
        TagInfo supportedTagsOfGroups = ExifToolWrapper.anExifTool().build().getSupportedTagsOfGroups();
        // then
        assertThat( supportedTagsOfGroups, is(notNullValue()));
        assertThat(supportedTagsOfGroups.getGroupByName("XMP::dc"), is(notNullValue()));
        TagGroup xmpDC = supportedTagsOfGroups.getGroupByName("XMP::dc");
        assertThat(xmpDC.getTagInfoById("creator"), is(notNullValue()));
        assertThat(xmpDC.getTagInfoById("creator").getType(), is("string"));
        assertThat(xmpDC.getTagInfoById("title"), is(notNullValue()));
        assertThat(xmpDC.getTagInfoById("title").getType(), is("lang-alt"));
    }

    @Test
    public void shouldReportErrorIfExiftoolNotFound()
    {
        // given
        final String currentSetting = ExifToolWrapper.getPathToExifTool();

        ExifToolWrapper.setPathToExifTool("notfound");
        boolean rightExceptionThrown = false;
        // when
        try
        {
            ExifToolWrapper.anExifTool().build().getSupportedTagsOfGroups();
        }
        catch (ExifToolIntegrationException e)
        {
            rightExceptionThrown = true;
        }
        finally
        {
            ExifToolWrapper.setPathToExifTool(currentSetting);
        }

        // then
        assertThat(rightExceptionThrown, is(true));
    }

    @Test
    public void shouldUseConfiguredCharacterEncoding() throws ExifToolIntegrationException
    {
        // given
        final ExifToolWrapper exifToolWrapper = ExifToolWrapper.anExifTool()
                .withEncodingCharSet(ExifToolWrapper.MetadataEncodingScope.ALL_FORMATS, "UTF8").build();

        // when
        final File file = new File(this.getClass().getResource("/content/mapping-dpa-example-image.jpeg").getFile());
        exifToolWrapper.runExiftool(file, "-IPTC:ALL");
        // then no exception occurs

    }
}