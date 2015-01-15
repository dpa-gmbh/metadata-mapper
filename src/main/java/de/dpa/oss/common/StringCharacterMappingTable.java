package de.dpa.oss.common;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Arrays;

/**
 * <p>This class maps all characters of a given input string to an output string. The mapping is based on code points
 * allowing up to 4-byte unicodes.</p>
 * <p/>
 * <p>The class supports the restriction of the mapping to a certain character set. If such a character set is specified
 * ({@link StringCharacterMappingTableBuilder#restrictToCharsetUsingDefaultChar(String, String)}) then the fallback replacement character
 * hs to be specified too. This character is used in case an input character cannot be mapped or is malformed </p>
 *
 * @author oliver langer
 */
public class StringCharacterMappingTable implements StringCharacterMapping
{
    /**
     * first simple approach:
     * <ul>
     * <li>use the code point of the character which should be mapped</li>
     * <li>if the element equals -1 at this index then the character will be returned unchanged.
     * Otherwise the code point will be used defined at this index</li>
     * </ul>
     */
    private int codePointMapping[];
    private static int NO_MAPPING_ENTRY = -1;
    private Charset targetCharset;

    /**
     * characters of this string will be used during mapping in case a character cannot be mapped
     * or is somehow malformed. 
     */
    private String targetCharsetMappingFallbackAppendString = "?";

    public static StringCharacterMappingTableBuilder aCharacterMapping( )
    {
        return new StringCharacterMappingTableBuilder();
    }

    private StringCharacterMappingTable(final int[] codePointMapping, final Charset targetCharset,
            final String targetCharsetMappingFallbackString)
    {
        this.codePointMapping = codePointMapping;
        this.targetCharset = targetCharset;
        if (targetCharset != null)
        {
            this.targetCharsetMappingFallbackAppendString = targetCharsetMappingFallbackString;
        }
    }

    @Override public String map(final String inputString)
    {
        if (inputString == null || inputString.length() == 0)
        {
            return inputString;
        }

        final CharsetEncoder charsetEncoder;
        if (targetCharset != null)
        {
            charsetEncoder = targetCharset.newEncoder();
        }
        else
        {
            charsetEncoder = Charset.defaultCharset().newEncoder();
        }

        final StringBuilder utf16StringRepresentation = new StringBuilder();

        CodepointIterator codepointIterator = CodepointIterator.iterate(inputString);
        while (codepointIterator.hasNext())
        {
            int currentCodePoint = codepointIterator.next();
            int mappedCodepointValue = codePointMapping[currentCodePoint];
            if (mappedCodepointValue == NO_MAPPING_ENTRY)
            {
                if (targetCharset != null)
                {
                    /**
                     * try to map it to target charset. If not possible then use fallback mapping character
                     */
                    CharBuffer utf16CharBuffer = CharBuffer.wrap(Character.toChars(currentCodePoint));
                    if (charsetEncoder.canEncode(utf16CharBuffer))
                    {
                        utf16StringRepresentation.append(utf16CharBuffer);
                    }
                    else
                    {
                        utf16StringRepresentation.append(targetCharsetMappingFallbackAppendString);
                    }
                }
                else
                {
                    CharBuffer utf16CharBuffer = CharBuffer.wrap(Character.toChars(currentCodePoint));
                    if (charsetEncoder.canEncode(utf16CharBuffer))
                    {
                        utf16StringRepresentation.append( utf16CharBuffer );
                    }
                    else
                    {
                        utf16StringRepresentation.append(targetCharsetMappingFallbackAppendString);
                    }
                }
            }
            else
            {
                CharBuffer utf16CharBuffer = CharBuffer.wrap(Character.toChars(mappedCodepointValue));
                if (charsetEncoder.canEncode(utf16CharBuffer))
                {
                    utf16StringRepresentation.append(utf16CharBuffer);
                }
                else
                {
                    utf16StringRepresentation.append(targetCharsetMappingFallbackAppendString);
                }
            }
        }

        return utf16StringRepresentation.toString();
    }

    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<characterMapping>");
        for (int i = 0; i < Character.MAX_CODE_POINT; i++)
        {
            int mapTo = codePointMapping[i];
            if (mapTo != NO_MAPPING_ENTRY)
            {
                String sourceString = new String(Character.toChars(i));
                if (sourceString.equals("\""))
                {
                    sourceString = "&quot;";
                }

                String targetString = new String(Character.toChars(mapTo));
                if (targetString.equals("\""))
                {
                    targetString = "&quot;";
                }

                sb.append("  <character from=\"0x").append(Integer.toHexString(i))
                        .append("\" to=\"0x")
                        .append(Integer.toHexString(mapTo))
                        .append("\"")
                        .append(" comment=\"from ")
                        .append(sourceString)
                        .append("; to=")
                        .append(targetString)
                        .append("\"/>");
                sb.append("\n");
            }
        }
        sb.append("</characterMapping>");
        return sb.toString();
    }

    public static class StringCharacterMappingTableBuilder
    {
        private int codePointMapping[];
        private Charset targetCharset = null;
        private String targetCharsetMappingFallbackCharacter = null;

        public StringCharacterMappingTableBuilder()
        {
            codePointMapping = new int[Character.MAX_CODE_POINT];
            Arrays.fill(codePointMapping, NO_MAPPING_ENTRY);
        }

        public StringCharacterMappingTableBuilder addCodepointMapping(final String fromHex16BitCodepoint, final String toHex16BitCodepoint)
        {
            int fromCodepoint = hexToInt(fromHex16BitCodepoint);
            int toCodepoint = hexToInt(toHex16BitCodepoint);
            addCodepointMapping(fromCodepoint, toCodepoint);
            return this;
        }

        public StringCharacterMappingTableBuilder addCodepointMapping(final int fromCodepoint, final int toCodePoint)
        {
            codePointMapping[fromCodepoint] = toCodePoint;
            return this;
        }

        /**
         * Iterates over the source and target string simultaneously. For each code point of the source
         * string it adds a mapping to the corresponding code point of the target string. It is a 1:1 mapping
         * and therefor expects equal numbers of code points for source and target string.
         */
        public StringCharacterMappingTableBuilder addMultiCharacterMapping(final String sourceCharacters, final String targetCharacters)
        {
            CodepointIterator cpSrc = CodepointIterator.iterate(sourceCharacters);
            CodepointIterator cpDest = CodepointIterator.iterate(targetCharacters);

            while (cpSrc.hasNext())
            {
                addCodepointMapping(cpSrc.next(), cpDest.next());
            }

            return this;
        }

        /**
         * * 
         * @param charsetName name of the target charset
         * @param targetCharsetMappingFallbackCharacter during mapping of a character this string will be used instead of
         *                                              the character in case it can not be encoded into the target charset encoding.
         *                                              reasons may be: not part of the target charset or char is malformed
         * @return
         */
        public StringCharacterMappingTableBuilder restrictToCharsetUsingDefaultChar(final String charsetName,
                String targetCharsetMappingFallbackCharacter)
        {
            if (targetCharsetMappingFallbackCharacter == null)
            {
                throw new IllegalArgumentException("fallback mapping character must not be null");
            }
            targetCharset = Charset.forName(charsetName);
            this.targetCharsetMappingFallbackCharacter = targetCharsetMappingFallbackCharacter;

            return this;
        }

        /**
         *  
         * @param targetCharsetMappingFallbackCharacter during mapping of a character this string will be used instead of
         *                                              the character in case it can not be encoded into the target charset encoding.
         */
        public StringCharacterMappingTableBuilder withFallbackReplacementCharacter(final String targetCharsetMappingFallbackCharacter)
        {
            this.targetCharsetMappingFallbackCharacter = targetCharsetMappingFallbackCharacter; 
            return this;
        }
        
        public StringCharacterMappingTable build()
        {
            return new StringCharacterMappingTable(codePointMapping, targetCharset, targetCharsetMappingFallbackCharacter);
        }
    }

    public static int hexToInt(final String hexStr)
    {
        if (hexStr == null || hexStr.length() == 0)
        {
            throw new IllegalArgumentException();
        }

        int posX = hexStr.indexOf('x');

        final String stringToParse;

        if (posX > -1)
        {
            stringToParse = hexStr.substring(posX + 1);
        }
        else
        {
            stringToParse = hexStr;
        }

        return Integer.parseInt(stringToParse, 16);
    }
}
