package de.dpa.oss.metadata.mapper.imaging.backend.exiftool.taginfo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oliver langer
 */
public class TagInfo
{
    private final Map<String,TagGroup> nameToTagGroup;

    public TagInfo()
    {
        nameToTagGroup = new HashMap<>();
    }

    public TagGroup getGroupByName( final String name )
    {
        if( nameToTagGroup.containsKey( name ) )
        {
            return nameToTagGroup.get(name);
        }
        else
        {
            return null;
        }
    }

    public boolean hasGroupContainingTagWithId( final String groupname, final String tagId )
    {
        TagGroup tagGroup = getGroupByName(groupname);
        if( tagGroup == null )
        {
            return false;
        }
        return tagGroup.getTagInfoById( tagId ) != null;
    }

    public void add(final TagGroup tagGroup)
    {
        nameToTagGroup.put( tagGroup.getName(), tagGroup);
    }
}
