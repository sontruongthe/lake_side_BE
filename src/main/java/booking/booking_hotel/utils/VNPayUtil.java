package booking.booking_hotel.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.http.HttpServletRequest;

/**
 * VNPay Utility Class - Giống như trong demo của VNPay
 * Chứa các hàm helper để tạo secure hash
 */
public class VNPayUtil {
    
    /**
     * Tạo HMAC SHA512 hash
     * @param key - Secret key
     * @param data - Dữ liệu cần hash
     * @return String hash
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Tạo secure hash từ sorted params
     * @param params - Map params đã sort
     * @param secretKey - Secret key
     * @return Secure hash
     */
    public static String hashAllFields(java.util.Map<String, String> params, String secretKey) {
        java.util.List<String> fieldNames = new java.util.ArrayList<>(params.keySet());
        java.util.Collections.sort(fieldNames);
        
        StringBuilder sb = new StringBuilder();
        java.util.Iterator<String> itr = fieldNames.iterator();
        
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
                if (itr.hasNext()) {
                    sb.append("&");
                }
            }
        }
        
        return hmacSHA512(secretKey, sb.toString());
    }
    
    /**
     * Get client IP address
     * @param request - HttpServletRequest
     * @return IP address
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
    
    /**
     * Get random number for transaction reference
     * @param len - Length
     * @return Random number string
     */
    public static String getRandomNumber(int len) {
        String chars = "0123456789";
        java.util.Random rnd = new java.util.Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
