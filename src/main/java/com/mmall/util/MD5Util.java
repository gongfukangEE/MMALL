package com.mmall.util;


import java.security.MessageDigest;

/**
 * @Auther gongfukang
 * @Date 2018/6/6 14:39
 */
public class MD5Util {
    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    /**
     * 返回大写 MD5
     */
    private static String MD5Encode(String origin, String charsetname) throws Exception {
        String resultString = null;
        resultString = new String(origin);
        MessageDigest md = MessageDigest.getInstance("MD5");
        if (charsetname == null || "".equals(charsetname))
            resultString = byteArrayToHexString(md.digest(resultString.getBytes()));
        else
            resultString = byteArrayToHexString(md.digest(resultString.getBytes(charsetname)));
        return resultString.toUpperCase();
    }

    public static String MD5EncodeUtf8(String orign) throws Exception{
        orign = orign + PropertiesUtil.getProperty("password.salt","");
        return MD5Encode(orign,"utf-8");
    }


    private static final String hexDigits[] = {"0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
}
