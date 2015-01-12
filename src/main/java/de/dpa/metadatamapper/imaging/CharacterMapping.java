package de.dpa.metadatamapper.imaging;

import de.dpa.metadatamapper.common.CharacterMappingTable;
import de.dpa.metadatamapper.common.StringCharacterMapping;
import de.dpa.metadatamapper.imaging.configuration.generated.CharacterMappingType;

/**
 * @author oliver langer
 */
public class CharacterMapping
{
    private final StringCharacterMapping stringMapper;
    private String targetCharacterSet;

    public CharacterMapping( final String targetCharacterSet, final CharacterMappingType characterMappingConfig, 
            final char defaultReplacementChar)
    {
        this.targetCharacterSet = targetCharacterSet;
        this.stringMapper = buildStringMapping(characterMappingConfig);
    }
    
    private StringCharacterMapping buildStringMapping(final CharacterMappingType config)
    {
        if( config == null )
        {
            return new StringCharacterMapping() {
                @Override public String map(final String inputString)
                {
                    return inputString;
                }
            };
        }

        CharacterMappingTable.CharacterMappingBuilder characterMappingBuilder = CharacterMappingTable.aCharacterMapping();
        for (CharacterMappingType.Character character : config.getCharacter())
        {
            characterMappingBuilder.addCodepointMapping( character.getFrom(), character.getTo());
        }
        
        return characterMappingBuilder.build();
    }
}
