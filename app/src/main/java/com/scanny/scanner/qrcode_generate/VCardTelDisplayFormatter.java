package com.scanny.scanner.qrcode_generate;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class VCardTelDisplayFormatter implements Formatter {

    private final List<Map<String, Set<String>>> metadataForIndex;

    VCardTelDisplayFormatter() {
        this(null);
    }

    VCardTelDisplayFormatter(List<Map<String, Set<String>>> metadataForIndex) {
        this.metadataForIndex = metadataForIndex;
    }

    private static CharSequence formatMetadata(CharSequence value, Map<String, Set<String>> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return value;
        }
        StringBuilder withMetadata = new StringBuilder();
        for (Map.Entry<String, Set<String>> metadatum : metadata.entrySet()) {
            Set<String> values = metadatum.getValue();
            if (values == null || values.isEmpty()) {
                continue;
            }
            Iterator<String> valuesIt = values.iterator();
            withMetadata.append(valuesIt.next());
            while (valuesIt.hasNext()) {
                withMetadata.append(',').append(valuesIt.next());
            }
        }
        if (withMetadata.length() > 0) {
            withMetadata.append(' ');
        }
        withMetadata.append(value);
        return withMetadata;
    }

    @Override
    public CharSequence format(CharSequence value, int index) {
        value = ContactEncoder.formatPhone(value.toString());
        Map<String, Set<String>> metadata =
                metadataForIndex == null || metadataForIndex.size() <= index ? null : metadataForIndex.get(index);
        value = formatMetadata(value, metadata);
        return value;
    }

}
