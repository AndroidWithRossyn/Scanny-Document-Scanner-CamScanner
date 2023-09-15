package com.scanny.scanner.qrcode_generate;

interface Formatter {

    /**
     * @param value value to format
     * @param index index of value in a list of values to be formatted
     * @return formatted value
     */
    CharSequence format(CharSequence value, int index);

}