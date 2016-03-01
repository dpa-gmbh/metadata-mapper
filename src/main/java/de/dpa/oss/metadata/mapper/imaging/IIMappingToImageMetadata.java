package de.dpa.oss.metadata.mapper.imaging;

import com.google.common.base.Strings;
import de.dpa.oss.common.StringCharacterMapping;
import de.dpa.oss.metadata.mapper.imaging.common.ImageMetadata;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.ConfigType;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.IIMMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author oliver langer
 */
public class IIMappingToImageMetadata
{
    private static Logger logger = LoggerFactory.getLogger(IIMappingToImageMetadata.class);
    final private StringCharacterMapping iimStringCharacterMapping;
    final Map<String, ConfigType.DateParser> idToDateParser;

    public IIMappingToImageMetadata(final StringCharacterMapping iimStringCharacterMapping,
            Map<String, ConfigType.DateParser> idToDateParser)
    {
        this.iimStringCharacterMapping = iimStringCharacterMapping;
        this.idToDateParser = idToDateParser;
    }

    public void map(final IIMMapping.MapsTo mappingInfo, final List<String> valueList,
            final ImageMetadata metadata)
    {
        logger.debug("Filling IIM field \"" + mappingInfo.getField() + "\" with value(s): " + valueList);

        if (valueList == null || valueList.isEmpty())
        {
            return;
        }

        switch (mappingInfo.getTargetType())
        {
        case LIST_OF_STRING:
            for (String value : valueList)
            {
                mapString(mappingInfo, value, metadata);
            }
            break;
        case STRING:
            mapString(mappingInfo, valueList.get(0), metadata);
            break;
        case DATE:
            mapDate(mappingInfo, valueList.get(0), metadata);
            break;
        }
    }

    private void mapString(final IIMMapping.MapsTo mappingInfo, final String value,
            final ImageMetadata metadata)
    {
        if (iimStringCharacterMapping != null)
        {
            metadata.addIPTCEntry(mappingInfo.getField(), iimStringCharacterMapping.map(value));
        }
        else
        {
            metadata.addIPTCEntry(mappingInfo.getField(), value);
        }
    }

    private void mapDate(final IIMMapping.MapsTo mappingInfo, final String value,
            final ImageMetadata metadata)
    {
        if (Strings.isNullOrEmpty(mappingInfo.getDateParserRef()))
        {
            logger.error("Target IIM field \"" + mappingInfo.getField() + "\" is of type DATE but does not refer a Date Parser");
            throw new IllegalStateException(
                    "Target IIM field \"" + mappingInfo.getField() + "\" is of type DATE but does not refer a Date Parser");
        }

        if (!idToDateParser.containsKey(mappingInfo.getDateParserRef()))
        {
            logger.error("Target IIM field \"" + mappingInfo.getField() + "\" is of type DATE but refers a non-existing Date Parser:"
                    + mappingInfo.getDateParserRef());
            throw new IllegalStateException(
                    "Target IIM field \"" + mappingInfo.getField() + "\" is of type DATE but refers a non-existing Date Parser:"
                            + mappingInfo.getDateParserRef()
            );
        }

        if (Strings.isNullOrEmpty(mappingInfo.getOutputDateFormat()))
        {
            logger.error("Target IIM field \"" + mappingInfo.getField() + "\" is of type DATE but does specifiy the output date format");
            throw new IllegalStateException(
                    "Target IIM field \"" + mappingInfo.getField() + "\" is of type DATE but does specifiy the output date format");
        }

        ConfigType.DateParser dateParser = idToDateParser.get(mappingInfo.getDateParserRef());
        SimpleDateFormat parser = new SimpleDateFormat(dateParser.getInputDateFormat());
        SimpleDateFormat outputFormat = new SimpleDateFormat(mappingInfo.getOutputDateFormat());

        try
        {
            Date parsedDate = parser.parse(value);
            metadata.addIPTCEntry(mappingInfo.getField(), outputFormat.format(parsedDate));
        }
        catch (ParseException e)
        {
            logger.error("Parsing date string \"" + value + "\" using format string \"" + dateParser.getInputDateFormat()
                    + "\" failed. Trying to store input date string as output date string directly", e);
            metadata.addIPTCEntry(mappingInfo.getField(), value);
        }

    }
}
