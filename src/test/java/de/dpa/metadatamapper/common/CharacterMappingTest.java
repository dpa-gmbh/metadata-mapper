package de.dpa.metadatamapper.common;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

public class CharacterMappingTest
{
    @Test
    public void testShouldLeaveStringAsIs()
    {
        // given
        CharacterMapping characterMapping = CharacterMapping.aCharacterMapping().build();

        final String stringToMap = "abcdefg52512ЄЅІЇЈЌЎёєѕіїјћќў";

        // when
        String mappedString = characterMapping.map(stringToMap);

        // then
        assertThat(mappedString, is(notNullValue()));
        assertThat(mappedString, is(stringToMap));
    }

    @Test
    public void testShouldMapBasicMultilingualPlane()
    {
        // given
        final String stringToMap = "AABABC";
        CharacterMapping characterMapping = CharacterMapping.aCharacterMapping()
                .addCodepointMapping("0x41", "0x44")
                .build();

        // when
        String mappedString = characterMapping.map(stringToMap);

        // then
        assertThat(mappedString, is(notNullValue()));
        assertThat(mappedString, is("DDBDBC"));
    }

    @Test
    public void testShouldMapSupplementary()
    {
        /**
         * Character to substitute: 135260
         has highSurrogate: 55364	=> D844
         has lowSurrogate: 56412 => DC5C
         */
        CharacterMapping characterMapping = CharacterMapping.aCharacterMapping()
                .addCodepointMapping(135260, 198)
                .build();
        final String stringToMap = "Supplement\uD844\uDC5Cry";

        // when
        String mappedString = characterMapping.map(stringToMap);

        // then
        assertThat(mappedString, is(notNullValue()));
        assertThat(mappedString, is("SupplementÆry"));
    }

    @Test
    public void should()
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

        CharacterMapping characterMapping = CharacterMapping.aCharacterMapping()
                .addMultiCharacterMapping(src, dst)
                .build();
        
        // when then
        assertThat( characterMapping.map( src01 ), is(dst01 ));
        assertThat( characterMapping.map( src02 ), is(dst02 ));
        assertThat(characterMapping.map(src03), is(dst03));
        System.out.println(characterMapping);

    }
}