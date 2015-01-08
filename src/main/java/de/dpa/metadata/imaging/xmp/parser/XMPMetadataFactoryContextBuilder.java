package de.dpa.metadata.imaging.xmp.parser;

import de.dpa.metadata.imaging.xmp.metadata.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author oliver langer
 */
public class XMPMetadataFactoryContextBuilder
{
    private LinkedList<XMPMetadataFactoryContext> contextStack;
    private final XMPMetadataItemPathParser xmpPathParser;

    public XMPMetadataFactoryContextBuilder()
    {
        contextStack = new LinkedList<>();
        xmpPathParser = new XMPMetadataItemPathParser();

        XMPMetadataFactoryContext rootContext = new XMPMetadataFactoryContext(XMPMetadataItemPath.ROOT_PATH_ITEM, new XMPRootCollection());
        contextStack.add(rootContext);
    }

    public List<XMPMetadata> getMetadataList()
    {
        return contextStack.getLast().getMetadataCollection().getMetadata();
    }

    public void addSingleMetadata(final String path, final XMPBase metadata)
    {
        if( path == null ) {
            System.out.println("huch");
        }
        LinkedList<XMPMetadataItemPath> pathItems = xmpPathParser.parsePaths(path);

        XMPMetadataFactoryContext context = determineContext(pathItems);

        context.getMetadataCollection().add(metadata);
    }

    public void addCollectionMetadata(final String path, final XMPCollection metadata)
    {

        LinkedList<XMPMetadataItemPath> pathItems = xmpPathParser.parsePaths(path);
        XMPMetadataItemPath self = pathItems.getLast();

        XMPMetadataFactoryContext context = determineContext(pathItems);

        context.getMetadataCollection().add(metadata);
        XMPMetadataFactoryContext newContext = new XMPMetadataFactoryContext(self, metadata);
        contextStack.push(newContext);
    }

    public void addStructMetadata(final String path, final XMPStruct structMetadata)
    {
        addCollectionMetadata(path, structMetadata);
    }

    private XMPMetadataFactoryContext determineContext(final LinkedList<XMPMetadataItemPath> pathItems)
    {
        if (contextStack.size() == 1)
        {
            // only root context exists
            return contextStack.getFirst();
        }

        Iterator<XMPMetadataItemPath> pathItemIterator = pathItems.iterator();
        Iterator<XMPMetadataFactoryContext> contextIterator = contextStack.descendingIterator();

        // can skip root element
        contextIterator.next();
        int matchedVisits = 1;

        while (pathItemIterator.hasNext() && contextIterator.hasNext())
        {
            XMPMetadataItemPath elementOfPathItems = pathItemIterator.next();
            XMPMetadataItemPath contextPathItem = contextIterator.next().getPathItem();
            if (!contextPathItem.equals(elementOfPathItems))
            {
                break;
            } else {
                matchedVisits++;
            }
        }

        int lastMatchingContext = contextStack.size() - matchedVisits;
        final int deleteBeforeIndex = lastMatchingContext;
        // remove all remaining contexts (elements before selected one) since they are not valid any more
        for (int i = 0; i < deleteBeforeIndex ; i++)
        {
            contextStack.pop();
            // correct return index
            lastMatchingContext--;
        }

        return contextStack.get(lastMatchingContext);

    }
}





