package de.dpa.oss.metadata.mapper.common;

import de.dpa.oss.common.StringCharacterMappingTable;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

public class StringCharacterMappingTableTest
{
    @Test
    public void shouldLeaveStringAsIs()
    {
        // given
        StringCharacterMappingTable stringCharacterMappingTable = StringCharacterMappingTable.aCharacterMapping().build();

        final String stringToMap = "abcdefg52512ЄЅІЇЈЌЎёєѕіїјћќў";

        // when
        String mappedString = stringCharacterMappingTable.map(stringToMap);

        // then
        assertThat(mappedString, is(notNullValue()));
        assertThat(mappedString, is(stringToMap));
    }

    @Test
    public void shouldLeaveStringAsIsUsingNonUTF8Encoding()
    {
        // given
        StringCharacterMappingTable stringCharacterMappingTable = StringCharacterMappingTable.aCharacterMapping()
                .restrictToCharsetUsingDefaultChar("iso8859-15", "").build();

        final String stringToMap = "äöüÄÖÜ";

        // when
        String mappedString = stringCharacterMappingTable.map(stringToMap);

        // then
        assertThat(mappedString, is(notNullValue()));
        assertThat(mappedString, is(stringToMap));
    }

    @Test
    public void shouldMapBasicMultilingualPlane()
    {
        // given
        final String stringToMap = "AABABC";
        StringCharacterMappingTable stringCharacterMappingTable = StringCharacterMappingTable.aCharacterMapping()
                .addCodepointMapping("0x41", "0x44")
                .build();

        // when
        String mappedString = stringCharacterMappingTable.map(stringToMap);

        // then
        assertThat(mappedString, is(notNullValue()));
        assertThat(mappedString, is("DDBDBC"));
    }

    @Test
    public void shouldMapSupplementary()
    {
        /**
         * Character to substitute: 135260
         has highSurrogate: 55364	=> D844
         has lowSurrogate: 56412 => DC5C
         */
        StringCharacterMappingTable stringCharacterMappingTable = StringCharacterMappingTable.aCharacterMapping()
                .addCodepointMapping(135260, 198)
                .build();
        final String stringToMap = "Supplement\uD844\uDC5Cry";

        // when
        String mappedString = stringCharacterMappingTable.map(stringToMap);

        // then
        assertThat(mappedString, is(notNullValue()));
        assertThat(mappedString, is("SupplementÆry"));
    }

    @Test
    public void shouldMapByMultiCharMapping()
    {
        // given
        final String src01 = "¦¨´”„Ŗ“ŗĄĮĀĆĘĒČŹĖĢĶĪĻŃŅŌŲŁŚŪŻąįāćęēčźėģķīļńņōųłśūżĽŞŤľş";
        final String dst01 = "|\"'\"\"R\"rAIACEECZEGKILNNOULSUZaiaceeczegkilnnoulsuzLSTls";
        final String src02 = "ť˝ŔĂĹĚĎĐŇŐŘŮŰŢŕăĺěďđňőřůűţĦĤİĞĴħĥığĵĊĈĠĜŬŜċĉġĝŭŝĸĨŦĩŧŊŋŨũЁЂ";
        final String dst02 = "t\"RALEDÐNÖRUÜTraleddnöruütHHIGJhhigjCCGGUSccgguskITitNnUuËT";
        final String src03 = "ЄЅІЇЈЌЎёєѕіїјћќў‘’ͺ―΄ΑΒΕΖΗΙΚΜΝΟΡΤΥΪΫίΰϊϋόύ…†‡";
        final String dst03 = "ESIÏJKYëesiijhky'',-'ABEZHIKMNOPTYIYiüïüóu.++";
        final String src04 = "•–—･ⅰ™";
        final String dst04 = "·--·i®";

        // dashes
        final String src05 = "" + '\u2010' + '\u2011' + '\u2012' + '\u2013' + '\u2014' + '\u2212';
        final String dst05 = "------";

        // quotion mark
        final String src06 = "" + '\u2018' + '\u2019' + '\u201a' + '\u201c' + '\u201d' + '\u201e' + '\u2039' + '\u203a';
        final String dst06 = "''\"\"\"\"\"\"";

        final String src = (src01 + src02 + src03 + src04 + src05 + src06);
        final String dst = (dst01 + dst02 + dst03 + dst04 + dst05 + dst06);

        StringCharacterMappingTable stringCharacterMappingTable = StringCharacterMappingTable.aCharacterMapping()
                .addMultiCharacterMapping(src, dst)
                .build();

        // when then
        assertThat(stringCharacterMappingTable.map(src01), is(dst01));
        assertThat(stringCharacterMappingTable.map(src02), is(dst02));
        assertThat(stringCharacterMappingTable.map(src03), is(dst03));
        System.out.println(stringCharacterMappingTable);
    }

    @Test
    public void shouldMapToTargetCharset()
    {
        // given
        StringCharacterMappingTable stringCharacterMappingTable = StringCharacterMappingTable.aCharacterMapping()
                .restrictToCharsetUsingDefaultChar("iso8859-15", "A").build();

        final String srcString = "abcdefg";

        // when
        String mappedStr = stringCharacterMappingTable.map(srcString);

        // then
        assertThat(mappedStr, is(srcString));
    }

    @Test
    public void shouldUseReplacementCharacterIfNotMapable()
    {
        // given
        StringCharacterMappingTable stringCharacterMappingTable = StringCharacterMappingTable.aCharacterMapping()
                .restrictToCharsetUsingDefaultChar("iso8859-15", "A").build();

        final String srcString = "String with unmapable char:" + new String(Character.toChars(5122 /* ᐂ */)) + "XXX";
        final String expectedString = "String with unmapable char:AXXX";

        // when
        String mappedStr = stringCharacterMappingTable.map(srcString);

        // then
        assertThat(mappedStr, is(expectedString));
    }

    @Test
    public void shouldUseMappingAndReplacement()
    {
        // given
        StringCharacterMappingTable stringCharacterMappingTable = StringCharacterMappingTable.aCharacterMapping()
                .restrictToCharsetUsingDefaultChar("iso8859-15", "·")
                .addCodepointMapping(5120, 65).build();

        final String srcString = "String with unmapable char:" + new String(Character.toChars(5122 /* ᐂ */))
                + " and mapable char: " + new String(Character.toChars(5120));
        final String expectedString = "String with unmapable char:· and mapable char: A";

        // when
        String mappedStr = stringCharacterMappingTable.map(srcString);

        // then
        assertThat(mappedStr, is(expectedString));
    }

    @Test
    public void shouldCreateFormattedMappingTable()
    {
        // given
        final String src = "abc<>d";
        final String dst = "ABC{}D";

        StringCharacterMappingTable mappingTable = StringCharacterMappingTable.aCharacterMapping()
                .addMultiCharacterMapping(src, dst)
                .build();

        // when
        Map<Integer, String> codepointAlternativeCharacters = new HashMap<>();
        codepointAlternativeCharacters.put(">".codePointAt(0), "&gt;");
        codepointAlternativeCharacters.put("<".codePointAt(0), "&lt;");
        String formattedTable = mappingTable.toString("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n",
                codepointAlternativeCharacters);
        
        // then
        assertThat( formattedTable, is("<tr><td>3c</td><td>&lt;</td><td>7b</td><td>{</td></tr>\n"
                + "<tr><td>3e</td><td>&gt;</td><td>7d</td><td>}</td></tr>\n"
                + "<tr><td>61</td><td>a</td><td>41</td><td>A</td></tr>\n"
                + "<tr><td>62</td><td>b</td><td>42</td><td>B</td></tr>\n"
                + "<tr><td>63</td><td>c</td><td>43</td><td>C</td></tr>\n"
                + "<tr><td>64</td><td>d</td><td>44</td><td>D</td></tr>\n"));
    }
}