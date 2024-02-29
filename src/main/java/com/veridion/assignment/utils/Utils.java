package com.veridion.assignment.utils;

import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.function.Consumer;

public class Utils {
    public static String removeLeadingOrLastingComma(String var) {
        if (var == null || var.isEmpty()) {
            return var;
        }

        while (var.startsWith(",") || var.startsWith(" ")) {
            var = var.substring(1).trim();
        }

        while (var.endsWith(",") || var.endsWith(" ")) {
            var = var.substring(0, var.length() - 1).trim();
        }

        return var;
    }

    public static String getCurrentFormattedDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm_ddMMyyyy");
        return sdf.format(new Date());
    }

    public static String quoteField(String field) {
        if (field == null || field.isEmpty()) {
            return "";
        }
        if (field.contains("\"") || field.contains(",")) {
            if (field.contains("\"")) {
                field = field.replace("\"", "\"\"");
            }
            return "\"" + field + "\"";
        }
        return field;
    }

    public static String removeProtocol(String url) {
        url = url.replaceAll("https?://", "");

        url = url.replaceAll("^www\\.", "");

        String[] parts = url.split("/");
        return parts[0];
    }

    public static String mergeStrings(String existingValue, String newValue) {
        if (StringUtils.hasLength(newValue) && !newValue.equals(existingValue)) {
            return StringUtils.hasLength(existingValue) ? Utils.removeLeadingOrLastingComma(existingValue + ", " + newValue) : Utils.removeLeadingOrLastingComma(newValue);
        }
        return Utils.removeLeadingOrLastingComma(existingValue);
    }

    public static StringBuilder removeLastCharacterIfExists(StringBuilder input, char charToRemove) {
        if (!input.isEmpty() && input.charAt(input.length() - 1) == charToRemove) {
            input.deleteCharAt(input.length() - 1);
        }
        return input;
    }

    public static void mergeField(Consumer<String> setter, String mainField, String contactField) {
        if (mainField.isEmpty()) {
            setter.accept(contactField);
        } else if (!contactField.isEmpty()) {
            String[] mainEntries = mainField.split(",");
            String[] contactEntries = contactField.split(",");

            for (String contactEntry : contactEntries) {
                String trimmedContactEntry = contactEntry.trim();
                boolean exists = false;
                for (String mainEntry : mainEntries) {
                    if (mainEntry.trim().equalsIgnoreCase(trimmedContactEntry)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    setter.accept(mainField + ", " + trimmedContactEntry);
                }
            }
        }
    }

    public static void addUniqueElement(String element, Set<String> uniqueElements, StringBuilder stringBuilder) {
        if (StringUtils.hasLength(element) && !uniqueElements.contains(element)) {
            if (!stringBuilder.isEmpty()) {
                stringBuilder.append(", ");
            }
            uniqueElements.add(element);
            stringBuilder.append(element);
        }
    }
}
