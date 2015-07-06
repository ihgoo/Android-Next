package com.mcxiaoke.next.http;

import com.mcxiaoke.next.Charsets;

import java.nio.charset.Charset;

/**
 * User: mcxiaoke
 * Date: 14-2-8
 * Time: 11:56
 */
public interface HttpConsts {

    public static final String ENCODING_UTF8 = Charsets.ENCODING_UTF_8;
    public static final Charset CHARSET_UTF8 = Charsets.UTF_8;

    public static final int BUFFER_SIZE = 10 * 1024;

    public static final int CONNECT_TIMEOUT = 10 * 1000;
    public static final int READ_TIMEOUT = 10 * 1000;
    public static final int WRITE_TIMEOUT = 10 * 1000;

    static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String DEFAULT_CONTENT_TYPE = "application/x-www-form-urlencoded";
    public static final String EMPTY_STRING = "";
    static final String DEFAULT_NAME = "nofilename";

    public static final char QUERY_STRING_SEPARATOR = '?';
    public static final String PARAM_SEPARATOR = "&";
    public static final String PAIR_SEPARATOR = "=";


    /**
     * HEADERS
     */
    public static final String HOST = "Host";
    public static final String REFERER = "Referer";
    public static final String ENCODING_GZIP = "gzip";
    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final String ACCEPT_ENCODING = "Accept-Encoding";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_ENCODING = "Content-Encoding";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String LOCATION = "Location";
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String USER_AGENT = "User-Agent";
}
