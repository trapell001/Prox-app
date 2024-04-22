package com.prox.challenge.model;

import com.prox.challenge.gcoder.service.ConfigService;
import jakarta.persistence.AttributeConverter;

public class StringArrayConverter implements AttributeConverter<String[], String> {
    @Override
    public synchronized String convertToDatabaseColumn(String[] attribute) {
        if (attribute == null)return null;
        return ConfigService.GSON.toJson(attribute);
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        if(dbData == null) return null;
        return ConfigService.GSON.fromJson(dbData, String[].class);
    }
}
