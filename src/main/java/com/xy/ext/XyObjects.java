package com.xy.ext;

/**
 * Class <code>Objects</code>
 *
 * @author yangnan 2023/2/17 10:28
 * @since 1.8
 */
public class XyObjects {

    public static <V> V cast(Object v) {
        return (V) v;
    }

    /**
     * str encode serial-number
     *
     * @param value
     * @return
     */
    public static final String encodeSerialNumber(String value) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.trim().toCharArray()) {
            int cAscil = (int) c;
            sb.append(Integer.toString(cAscil, 8)).append("9");
        }
        return sb.toString();
    }

    /**
     * str decode serial-number
     *
     * @param value
     * @return
     */
    public static final String decodeSerialNumber(String value) {
        String[] splitInt = value.trim().split("9");
        StringBuilder sb = new StringBuilder();
        for (String s : splitInt) {
            int i = Integer.parseInt(s, 8);
            sb.append((char) i);
        }
        return sb.toString();
    }


}
