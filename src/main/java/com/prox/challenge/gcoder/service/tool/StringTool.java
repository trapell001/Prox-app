package com.prox.challenge.gcoder.service.tool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;

public class StringTool {
    public static String mergeAndRemoveDuplicates(String string1, String string2) {
        // Convert arrays to sets to remove duplicates
        HashSet<String> set1 = new HashSet<>(Arrays.asList(string1.split(",")));
        HashSet<String> set2 = new HashSet<>(Arrays.asList(string2.split(",")));

        // Merge sets
        set1.addAll(set2);

        // Convert set back to array
        String[] mergedArray = set1.toArray(new String[0]);

        // Convert array to string with comma-separated values
        return String.join(",", mergedArray);
    }

    public static String convertListToString(List<String> stringList) {
        // Sử dụng StringJoiner để nối các phần tử của danh sách
        StringJoiner stringJoiner = new StringJoiner(",");

        // Thêm từng phần tử vào StringJoiner
        for (String element : stringList) {
            stringJoiner.add(element);
        }
        // Lấy chuỗi kết quả từ StringJoiner
        return stringJoiner.toString();

    }

}
