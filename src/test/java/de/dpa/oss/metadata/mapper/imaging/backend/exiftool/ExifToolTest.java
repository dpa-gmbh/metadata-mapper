package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo.TagGroup;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo.TagInfo;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

public class ExifToolTest
{
    @Test
    public void shouldReturnListOfGroups()
            throws IOException, ParserConfigurationException, SAXException, JAXBException, ExifToolIntegrationException
    {
        // given
        // when
        TagInfo supportedTagsOfGroups = ExifTool.anExifTool().build().getSupportedTagsOfGroups();
        // then
        assertThat( supportedTagsOfGroups, is(notNullValue()));
        assertThat(supportedTagsOfGroups.getGroupByName("XMP::dc"), is(notNullValue()));
        TagGroup xmpDC = supportedTagsOfGroups.getGroupByName("XMP::dc");
        assertThat(xmpDC.getTagInfoById("creator"), is(notNullValue()));
        assertThat(xmpDC.getTagInfoById("creator").getType(), is("string"));
        assertThat(xmpDC.getTagInfoById("title"), is(notNullValue()));
        assertThat(xmpDC.getTagInfoById("title").getType(), is("lang-alt"));
    }

}