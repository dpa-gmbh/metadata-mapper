package de.dpa.oss.metadata.mapper.processor;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;

/**
 * Created by Schmidt-Nia.Robert on 10.06.2015.
 */
public class JoinTest
{

    @Test public void shouldJoinWithEmptyDelimiter()
    {
        // given
        ArrayList<String> data = Lists.newArrayList("wusel", "lesuw");
        // when
        List<String> result = new Join().process(data);
        // then
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is("wusellesuw"));
    }

    @Test public void shouldJoinWithDelimiter()
    {
        // given
        ArrayList<String> data = Lists.newArrayList("wusel", "lesuw");
        // when
        Join join = new Join();
        join.setDelimiter("X");

        List<String> result = join.process(data);
        // then
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is("wuselXlesuw"));
    }

    @Test public void shouldJoinWithLongDelimiter()
    {
        // given
        ArrayList<String> data = Lists.newArrayList("wusel", "lesuw");
        // when
        Join join = new Join();
        join.setDelimiter("/X/");

        List<String> result = join.process(data);
        // then
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is("wusel/X/lesuw"));
    }
}