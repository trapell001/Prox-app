package com.prox.challenge.gcoder.converter;

import com.prox.challenge.gcoder.service.tool.StringTool;
import jakarta.persistence.AttributeConverter;

import java.util.ArrayList;
import java.util.List;

public class ListUrlConvert implements AttributeConverter<List<String>, String> {
    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if(attribute== null) return null;
        else {
            attribute.replaceAll(UrlNameConverter.instance::convertToDatabaseColumn);
            return StringTool.convertListToString(attribute);
        }
    }
    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if(dbData== null) return new ArrayList<>();
        else {
            List<String> result = new ArrayList<>(List.of(dbData.split(",")));
            result.replaceAll(UrlNameConverter.instance::convertToEntityAttribute);
            return result;
        }
    }
}
