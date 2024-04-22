package com.prox.challenge.gcoder.converter;

import com.prox.challenge.gcoder.service.ConfigService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

@Convert
public class UrlNameConverter implements AttributeConverter<String,String> {

    public static final UrlNameConverter instance = new UrlNameConverter();
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if(attribute == null || attribute.isEmpty() || attribute.equals(ConfigService.URL_FIRST_FILE)) return null;
        attribute = attribute.replace(ConfigService.URL_FIRST_FILE, "");
        int cut = attribute.lastIndexOf("/");
        if(cut != -1)attribute = attribute.substring(cut + 1);
        return attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if(dbData == null || dbData.isEmpty()) return "";
        int cut = dbData.lastIndexOf("/");
        if(cut != -1)dbData = dbData.substring(cut + 1);
        return ConfigService.URL_FIRST_FILE + dbData;
    }
}
