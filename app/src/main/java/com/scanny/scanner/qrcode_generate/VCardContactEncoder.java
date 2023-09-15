package com.scanny.scanner.qrcode_generate;

import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class VCardContactEncoder extends ContactEncoder {

    private static final char TERMINATOR = '\n';

    private static List<Map<String, Set<String>>> buildPhoneMetadata(Collection<String> phones, List<String> phoneTypes) {
        if (phoneTypes == null || phoneTypes.isEmpty()) {
            return null;
        }
        List<Map<String, Set<String>>> metadataForIndex = new ArrayList<>();
        for (int i = 0; i < phones.size(); i++) {
            if (phoneTypes.size() <= i) {
                metadataForIndex.add(null);
            } else {
                Map<String, Set<String>> metadata = new HashMap<>();
                metadataForIndex.add(metadata);
                Set<String> typeTokens = new HashSet<>();
                metadata.put("TYPE", typeTokens);
                String typeString = phoneTypes.get(i);
                Integer androidType = maybeIntValue(typeString);
                if (androidType == null) {
                    typeTokens.add(typeString);
                } else {
                    String purpose = vCardPurposeLabelForAndroidType(androidType);
                    String context = vCardContextLabelForAndroidType(androidType);
                    if (purpose != null) {
                        typeTokens.add(purpose);
                    }
                    if (context != null) {
                        typeTokens.add(context);
                    }
                }
            }
        }
        return metadataForIndex;
    }

    private static Integer maybeIntValue(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    private static String vCardPurposeLabelForAndroidType(int androidType) {
        switch (androidType) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
                return "fax";
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
                return "pager";
            case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
                return "textphone";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
                return "text";
            default:
                return null;
        }
    }

    private static String vCardContextLabelForAndroidType(int androidType) {
        switch (androidType) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                return "home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
                return "work";
            default:
                return null;
        }
    }

    @Override
    public String[] encode(List<String> names,
                           String organization,
                           List<String> addresses,
                           List<String> phones,
                           List<String> phoneTypes,
                           List<String> emails,
                           List<String> urls,
                           String note) {
        StringBuilder newContents = new StringBuilder(100);
        newContents.append("BEGIN:VCARD").append(TERMINATOR);
        newContents.append("VERSION:3.0").append(TERMINATOR);

        StringBuilder newDisplayContents = new StringBuilder(100);

        Formatter fieldFormatter = new VCardFieldFormatter();

        appendUpToUnique(newContents, newDisplayContents, "N", names, 1, null, fieldFormatter, TERMINATOR);

        append(newContents, newDisplayContents, "ORG", organization, fieldFormatter, TERMINATOR);

        appendUpToUnique(newContents, newDisplayContents, "ADR", addresses, 1, null, fieldFormatter, TERMINATOR);

        List<Map<String, Set<String>>> phoneMetadata = buildPhoneMetadata(phones, phoneTypes);
        appendUpToUnique(newContents, newDisplayContents, "TEL", phones, Integer.MAX_VALUE,
                new VCardTelDisplayFormatter(phoneMetadata),
                new VCardFieldFormatter(phoneMetadata), TERMINATOR);

        appendUpToUnique(newContents, newDisplayContents, "EMAIL", emails, Integer.MAX_VALUE, null,
                fieldFormatter, TERMINATOR);

        appendUpToUnique(newContents, newDisplayContents, "URL", urls, Integer.MAX_VALUE, null,
                fieldFormatter, TERMINATOR);

        append(newContents, newDisplayContents, "NOTE", note, fieldFormatter, TERMINATOR);

        newContents.append("END:VCARD").append(TERMINATOR);

        return new String[]{newContents.toString(), newDisplayContents.toString()};
    }

}
