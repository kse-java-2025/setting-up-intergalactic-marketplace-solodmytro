package ua.org.kse.domain.product;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CosmicTagConverter implements AttributeConverter<CosmicTag, String> {
    @Override
    public String convertToDatabaseColumn(CosmicTag attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public CosmicTag convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new CosmicTag(dbData);
    }
}