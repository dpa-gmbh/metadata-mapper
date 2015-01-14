package de.dpa.oss.metadata.mapper.imaging.xmp.parser;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author oliver langer
 */
public class XMPMetadataItemPathParser
{
    private final Pattern namePattern = Pattern.compile("(([^:]*):)?([^\\Q[\\E]*)(\\Q[\\E([^\\Q]\\E]*)\\Q]\\E)?");


    public LinkedList<XMPMetadataItemPath> parsePaths(final String path) {
        LinkedList<XMPMetadataItemPath> toReturn = new LinkedList<>();

        final String [] tokens = path.split("/");

        for( String token : tokens) {
            toReturn.addAll(createPathItem(token));
        }

        return toReturn;
    }

    private List<XMPMetadataItemPath> createPathItem(final String nextToken)
    {
        final List<XMPMetadataItemPath> toReturn = new ArrayList<>();

        Matcher matcher = namePattern.matcher(nextToken);
        if( matcher.matches()) {
            final String prefix = matcher.group(2);
            final String name = matcher.group(3);
            final String indexStr = matcher.group(5);
            int index = -1;

            if(!Strings.isNullOrEmpty(indexStr)) {
                index = Integer.parseInt(indexStr);
            }

            if( index > -1 ) {
                toReturn.add( new XMPMetadataItemPath(prefix,name));
                toReturn.add( new XMPMetadataArrayItemPath(prefix, name, index ) );
            } else {
                toReturn.add(new XMPMetadataItemPath(prefix, name));
            }
        } else {
            toReturn.add( new XMPMetadataItemPath("", nextToken) );
        }

        return toReturn;
    }

}
