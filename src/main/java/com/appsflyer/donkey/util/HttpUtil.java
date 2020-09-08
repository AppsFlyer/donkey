package com.appsflyer.donkey.util;

public class HttpUtil {
  
  private static final String MULTIPART_FORM_DATA = "multipart/form-data";
  private static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
  
  public static boolean isMultiPartForm(String contentType) {
    return MULTIPART_FORM_DATA.equals(contentType);
  }
  
  public static boolean isFormUrlEncoded(String contentType) {
    return FORM_URLENCODED.equals(contentType);
  }
}
