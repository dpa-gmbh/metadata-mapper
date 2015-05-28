package de.dpa.oss.metadata.mapper.imaging.backend.exiftool;

import com.google.common.collect.ListMultimap;
import de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo.TagGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This builder can only be used for operations modifying the image. For other operations refer to
 * {@link ExifToolWrapper} directly
 */
public class ExifTool
{
    private final File inputSource;
    private List<String> tagModifications = new ArrayList<>();
    private List<String> codedCharsetOptions = new ArrayList<>();
    private List<String> tagGroupsToClear = new ArrayList<>();

    public enum CodedCharset
    {
        /* ESC % G or UTF8*/
        UTF8("UTF8"),
        /* ESC . A */
        LATIN1("\u001B.A");

        CodedCharset(final String codepageId)
        {
            this.codepageId = codepageId;
        }

        private final String codepageId;

        public String getCodepageId()
        {
            return codepageId;
        }
    }

    public static ExifTool modifyImage(final File image)
    {
        if (image == null)
            throw new IllegalArgumentException(
                    "image cannot be null and must be a valid stream of image data.");
        if (!image.canWrite())
            throw new SecurityException(
                    "Unable to read the given image ["
                            + image.getAbsolutePath()
                            + "], ensure that the image exists at the given path and that the executing Java process has permissions to read it.");

        return new ExifTool(image);
    }

    private ExifTool(final File inputSource)
    {
        this.inputSource = inputSource;
    }

    public ExifTool setImageMetadata(ListMultimap<String, String> tags)
    {
        if (tags == null || tags.size() == 0)
            throw new IllegalArgumentException(
                    "tags cannot be null and must contain 1 or more Tag to query the image for.");
        for (Map.Entry<String, String> entry : tags.entries())
        {
            tagModifications.add("-" + entry.getKey() + "=" + entry.getValue());
        }
        return this;
    }

    public ExifTool useEncodingCharsetForIPTC(final CodedCharset codedCharset)
    {
        this.codedCharsetOptions.add("-IPTC:codedcharacterset=" + codedCharset.getCodepageId());
        return this;
    }

    /**
     * Call either
     * <pre>
     *     exiftool -listx -S
     * </pre>
     * or {@link ExifToolWrapper#getSupportedTagsOfGroups()} to get the list of TagGroups which contains the group name
     * (g0 / {@link TagGroup#getName()}) and the specific location ( g1 / {@link TagGroup#getSpecificLocation()})
     */
    public ExifTool clearTagGroup(final String groupName, final String specificLocation)
    {
        tagGroupsToClear.add("-" + groupName + ":" + specificLocation + "=");
        return this;
    }

    /**
     * Removes all tag groups within the given media.
     */
    public ExifTool clearAllTagGroups()
    {
        tagGroupsToClear.add("-all=");
        return this;
    }

    public void execute(final ExifToolWrapper exifTool) throws ExifToolIntegrationException
    {
        final List<String> cmdArgs = new ArrayList<>();
        cmdArgs.addAll(this.codedCharsetOptions);
        cmdArgs.addAll(tagGroupsToClear);
        cmdArgs.addAll(tagModifications);
        exifTool.runExiftool(inputSource, cmdArgs);
    }
}
