package de.dpa.metadatamapper.common;

/**
 * @author oliver langer
 */
public class CharacterMapping
{
    private char[] characterMap;
    
    public CharacterMapping()
    {
        characterMap = new char[Character.MAX_VALUE];
    }
    
    /*
    public void addMapping( final String fromHex16BitValue, final String toHex16BitValue )
    {
        int from = Integer.parseInt(fromHex16BitValue, 16);
        int to = Integer.parseInt(toHex16BitValue, 16);
        addMapping(Character.toChars(from), Character.toChars(to));
    }

    private void addMapping(final char[] from, final char[] toChars)
    {
      String s = "bla";
        s.ch
    }
    */
}
