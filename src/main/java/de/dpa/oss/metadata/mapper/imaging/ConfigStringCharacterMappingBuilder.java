package de.dpa.oss.metadata.mapper.imaging;

import de.dpa.oss.common.StringCharacterMappingTable;
import de.dpa.oss.common.StringCharacterMapping;
import de.dpa.oss.metadata.mapper.imaging.configuration.generated.CharacterMappingType;

import java.nio.charset.Charset;

/**
 * Constructs a {@link StringCharacterMapping} instance based on the given configuration.
 * If no mapping is defined and if the target charset is utf-8 then a simplified mapper is returned
 * *
 * @author oliver langer
 */
public class ConfigStringCharacterMappingBuilder
{
    private StringCharacterMappingTable.CharacterMappingBuilder characterMappingBuilder;
    private String targetCharsetName = null;
    private String fallbackReplacementChar = null;
    private boolean hasMappingTable = false;

    public static ConfigStringCharacterMappingBuilder stringCharacterMappingBuilder()
    {
        return new ConfigStringCharacterMappingBuilder();
    }
    
    private ConfigStringCharacterMappingBuilder() {
        characterMappingBuilder = StringCharacterMappingTable.aCharacterMapping();
    }

    public ConfigStringCharacterMappingBuilder withTargetCharsetAndFallbackReplacementChar( final String targetCharsetName,
            final String fallbackReplacementChar )
    {
        this.targetCharsetName = targetCharsetName;
        this.fallbackReplacementChar = fallbackReplacementChar;
        
        characterMappingBuilder.restrictToCharsetUsingDefaultChar(targetCharsetName, fallbackReplacementChar);
        return this;
    }

    public ConfigStringCharacterMappingBuilder withMappingConfigurartion( final CharacterMappingType characterMappingConfig )
    {

        if (characterMappingConfig != null)
        {
            for (CharacterMappingType.Character character : characterMappingConfig.getCharacter())
            {
                characterMappingBuilder.addCodepointMapping(character.getFrom(), character.getTo());
            }

            hasMappingTable = true;
        }
        
        return this;
    }

    public StringCharacterMapping build()
    {
        /**
         * if there is no mapping defined and if charset utf-8 is used then a simplified string character mapper 
         * is returned.
         */
        if( !hasMappingTable && (targetCharsetName == null || Charset.forName("utf-8").aliases().contains(
                targetCharsetName.toLowerCase())))
        {
            return new StringCharacterMapping()
            {
                @Override public String map(final String inputString)
                {
                    return inputString;
                }
            };
        }
        else
        {
            return characterMappingBuilder.build();
        }
                
    }
}
