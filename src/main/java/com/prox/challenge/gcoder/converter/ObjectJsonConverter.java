package com.prox.challenge.gcoder.converter;

import com.google.gson.Gson;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class ObjectJsonConverter implements AttributeConverter<Object, String> {
    @Autowired
    private Gson gson;
    @Override
    public String convertToDatabaseColumn(Object attribute) {
        return gson.toJson(attribute);
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        return gson.fromJson(dbData, Object.class);
    }
}
