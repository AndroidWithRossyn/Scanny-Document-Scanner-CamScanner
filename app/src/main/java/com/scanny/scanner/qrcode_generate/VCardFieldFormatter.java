package com.scanny.scanner.qrcode_generate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

final class VCardFieldFormatter implements Formatter {

    private static final Pattern RESERVED_VCARD_CHARS = Pattern.compile("([\\\\,;])");
    private static final Pattern NEWLINE = Pattern.compile("\\n");

    private final List<Map<String, Set<String>>> metadataForIndex;

    VCardFieldFormatter() {
        this(null);
    }

    VCardFieldFormatter(List<Map<String, Set<String>>> metadataForIndex) {
        this.metadataForIndex = metadataForIndex;
    }

    private static CharSequence formatMetadata(CharSequence value, Map<String, Set<String>> metadata) {
        StringBuilder withMetadata = new StringBuilder();
        if (metadata != null) {
            for (Map.Entry<String, Set<String>> metadatum : metadata.entrySet()) {
                Set<String> values = metadatum.getValue();
                if (values == null || values.isEmpty()) {
                    continue;
                }
                withMetadata.append(';').append(metadatum.getKey()).append('=');
                if (values.size() > 1) {
                    withMetadata.append('"');
                }
                Iterator<String> valuesIt = values.iterator();
                withMetadata.append(valuesIt.next());
                while (valuesIt.hasNext()) {
                    withMetadata.append(',').append(valuesIt.next());
                }
                if (values.size() > 1) {
                    withMetadata.append('"');
                }
            }
        }
        withMetadata.append(':').append(value);
        return withMetadata;
    }

    @Override
    public CharSequence format(CharSequence value, int index) {
        value = RESERVED_VCARD_CHARS.matcher(value).replaceAll("\\\\$1");
        value = NEWLINE.matcher(value).replaceAll("");
        Map<String, Set<String>> metadata =
                metadataForIndex == null || metadataForIndex.size() <= index ? null : metadataForIndex.get(index);
        value = formatMetadata(value, metadata);
        return value;
    }

}