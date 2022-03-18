package me.aakrylov.chunkanalyzer.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class StringUtils {

    public static Set<Character> convertToSet(String value) {
        char[] charArray = value.toCharArray();
        Set<Character> resultSet = new HashSet<>();
        for (char c : charArray) {
            resultSet.add(c);
        }
        return resultSet;
    }
}
