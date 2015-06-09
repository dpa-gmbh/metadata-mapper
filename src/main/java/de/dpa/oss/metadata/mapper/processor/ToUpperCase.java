package de.dpa.oss.metadata.mapper.processor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author oliver langer
 */
public class ToUpperCase implements Processor
{
    public final List<String> process(final List<String> values)
    {
        return Lists.transform(values, new Function<String, String>()
        {
            @Override public String apply(final String s)
            {
                return s.toUpperCase() ;
            }
        });
    }
}
