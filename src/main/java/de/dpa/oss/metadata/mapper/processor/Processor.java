package de.dpa.oss.metadata.mapper.processor;

import java.util.List;

/**
 * @author oliver langer
 */
public interface Processor
{
    /**
     * @param values selected by xpath expression. Empty array if no values have been
     *               selected
     * @return post-processed values
     */
    List<String> process(final List<String> values);
}
