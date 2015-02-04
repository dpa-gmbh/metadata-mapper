package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo;

import java.util.Map;

/**
 * @author oliver langer
 */
public class TagGroup
{
    private final String name;
    private final String informationType;
    private final String specificLocation;
    private final Map<String,TagGroupItem> idToTagGroupItemMap;

    protected TagGroup(final String name, final String informationType, final String specificLocation,
            final Map<String, TagGroupItem> idToTagGroupItemMap)
    {
        this.name = name;
        this.informationType = informationType;
        this.specificLocation = specificLocation;
        this.idToTagGroupItemMap = idToTagGroupItemMap;
    }

    public String getName()
    {
        return name;
    }

    public String getInformationType()
    {
        return informationType;
    }

    public String getSpecificLocation()
    {
        return specificLocation;
    }

    public TagGroupItem getTagInfoById( final String tagId )
    {
        if( idToTagGroupItemMap.containsKey(tagId))
        {
            return idToTagGroupItemMap.get(tagId);
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
            return true;
        if (!(o instanceof TagGroup))
            return false;

        final TagGroup tagGroup = (TagGroup) o;

        if (idToTagGroupItemMap != null ? !idToTagGroupItemMap.equals(tagGroup.idToTagGroupItemMap) : tagGroup.idToTagGroupItemMap != null)
            return false;
        if (informationType != null ? !informationType.equals(tagGroup.informationType) : tagGroup.informationType != null)
            return false;
        if (name != null ? !name.equals(tagGroup.name) : tagGroup.name != null)
            return false;
        if (specificLocation != null ? !specificLocation.equals(tagGroup.specificLocation) : tagGroup.specificLocation != null)
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (informationType != null ? informationType.hashCode() : 0);
        result = 31 * result + (specificLocation != null ? specificLocation.hashCode() : 0);
        result = 31 * result + (idToTagGroupItemMap != null ? idToTagGroupItemMap.hashCode() : 0);
        return result;
    }

    @Override public String toString()
    {
        return "TagGroup{" +
                "idToTagGroupItemMap=" + idToTagGroupItemMap +
                ", name='" + name + '\'' +
                ", informationType='" + informationType + '\'' +
                ", specificLocation='" + specificLocation + '\'' +
                '}';
    }
}
