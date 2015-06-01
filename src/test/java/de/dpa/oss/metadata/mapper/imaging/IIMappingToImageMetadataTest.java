package de.dpa.oss.metadata.mapper.imaging;

import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.ConfigType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.IIMMapping;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.IIMMappingTargetType;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class IIMappingToImageMetadataTest
{
    @Test
    public void shouldParseAndFormatDate()
    {
        // given
        ConfigType.DateParser dateParser = new ConfigType.DateParser();
        dateParser.setId( "dateParser");
        dateParser.setInputDateFormat( "yyyy_MM_dd");

        Map<String, ConfigType.DateParser> idToDateParser = new HashMap<>();
        idToDateParser.put(dateParser.getId(), dateParser );

        ImageMetadata imageMetadata = new ImageMetadata();

        IIMMapping.MapsTo mapsTo = new IIMMapping.MapsTo();
        mapsTo.setTargetType(IIMMappingTargetType.DATE);
        mapsTo.setDateParserRef( dateParser.getId());
        mapsTo.setOutputDateFormat( "yyyy:MM:dd");

        // when
        new IIMappingToImageMetadata(null,idToDateParser).map(mapsTo, Arrays.asList("2015_07_20"),imageMetadata);

        // then
        assertThat( imageMetadata.getIptcEntries(), is(notNullValue()));
        assertThat( imageMetadata.getIptcEntries().values(), hasSize(1));
        String mapped = imageMetadata.getIptcEntries().values().iterator().next();
        assertThat(mapped, is("2015:07:20"));
    }
}