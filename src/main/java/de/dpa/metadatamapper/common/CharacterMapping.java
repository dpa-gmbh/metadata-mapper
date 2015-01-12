package de.dpa.metadatamapper.common;

import java.util.Arrays;

/**
 * @author oliver langer
 */
public class CharacterMapping
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

    public static CharacterMappingBuilder aCharacterMapping()
    {
        return new CharacterMappingBuilder();
    }

    private CharacterMapping(final int codePointMapping[])
    {
        this.codePointMapping = codePointMapping;
    }

    public String map(final String inputString)
    {
        if (inputString == null || inputString.length() == 0)
        {
            return inputString;
        }

        final StringBuilder sb = new StringBuilder();

        CodepointIterator codepointIterator = CodepointIterator.iterate(inputString);
        while (codepointIterator.hasNext())
        {
            int currentCodePoint = codepointIterator.next();
            int mappedCodepointValue = codePointMapping[currentCodePoint];
            if (mappedCodepointValue == NO_MAPPING_ENTRY)
            {
                sb.appendCodePoint(currentCodePoint);
            }
            else
            {
                sb.appendCodePoint(mappedCodepointValue);
            }
        }

        return sb.toString();
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

    public static class CharacterMappingBuilder
    {
        private int codePointMapping[];

        public CharacterMappingBuilder()
        {
            codePointMapping = new int[Character.MAX_CODE_POINT];
            Arrays.fill(codePointMapping, NO_MAPPING_ENTRY);
        }

        public CharacterMappingBuilder addCodepointMapping(final String fromHex16BitCodepoint, final String toHex16BitCodepoint)
        {
            int fromCodepoint = hexToInt(fromHex16BitCodepoint);
            int toCodepoint = hexToInt(toHex16BitCodepoint);
            addCodepointMapping(fromCodepoint, toCodepoint);
            return this;
        }

        public CharacterMappingBuilder addCodepointMapping(final int fromCodepoint, final int toCodePoint)
        {
            codePointMapping[fromCodepoint] = toCodePoint;
            return this;
        }

        /**
         * Iterates over the source and target string simultaneously. For each code point of the source
         * string it adds a mapping to the corresponding code point of the target string. It is a 1:1 mapping
         * and therefor expects equal numbers of code points for source and target string.
         */
        public CharacterMappingBuilder addMultiCharacterMapping(final String sourceCharacters, final String targetCharacters)
        {
            CodepointIterator cpSrc = CodepointIterator.iterate(sourceCharacters);
            CodepointIterator cpDest = CodepointIterator.iterate(targetCharacters);

            while (cpSrc.hasNext())
            {
                addCodepointMapping(cpSrc.next(), cpDest.next());
            }

            return this;
        }

        public CharacterMapping build()
        {
            return new CharacterMapping(codePointMapping);

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
