package com.mcxiaoke.next.http;

import com.mcxiaoke.next.utils.IOUtils;
import com.mcxiaoke.next.utils.StringUtils;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Response;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * User: mcxiaoke
 * Date: 14-2-8
 * Time: 11:22
 */
public class NextResponse implements Closeable {
    public static final String TAG = NextResponse.TAG;

    private Response mResponse;
    private int mStatusCode;
    private String mMessage;
    private Date mCreatedAt;

    NextResponse(final Response response) {
        mResponse = response;
        mStatusCode = response.code();
        mMessage = response.message();
        mCreatedAt = new Date();
    }

    public Response raw() {
        return mResponse;
    }

    public Date createdAt() {
        return mCreatedAt;
    }

    public boolean successful() {
        return mResponse.isSuccessful();
    }

    public boolean redirect() {
        return mResponse.isRedirect();
    }

    public int code() {
        return mStatusCode;
    }

    public String message() {
        return mMessage;
    }

    public long contentLength() throws IOException {
        return mResponse.body().contentLength();
    }

    public String contentType() {
        return mResponse.body().contentType().toString();
    }

    public Charset charset() {
        return mResponse.body().contentType().charset();
    }

    public Headers headers() {
        return mResponse.headers();
    }

    public String header(String name) {
        return mResponse.headers().get(name);
    }

    public String location() {
        return header(HttpConsts.LOCATION);
    }

    private InputStream getInputStream() throws IOException {
        return mResponse.body().byteStream();
    }

    private byte[] getByteArray() throws IOException {
        return IOUtils.readBytes(mResponse.body().byteStream());
    }

    public InputStream stream() throws IOException {
        return getInputStream();
    }

    public byte[] bytes() throws IOException {
        return getByteArray();
    }

    public Reader reader() throws IOException {
        return mResponse.body().charStream();
    }

    public String string() throws IOException {
        return IOUtils.readString(reader());
    }

    public int writeTo(OutputStream os) throws IOException {
        return IOUtils.copy(getInputStream(), os);
    }

    public boolean writeTo(File file) throws IOException {
        return IOUtils.writeStream(file, getInputStream());
    }

    public void close() throws IOException {
        mResponse.body().close();
    }

    public String dumpContent() {
        try {
            return StringUtils.safeSubString(string(), 256);
        } catch (IOException e) {
            return "IOException";
        }
    }

    public String dumpHeaders() {
        return mResponse.headers().toString();
    }

    @Override
    public String toString() {
        return "NextResponse{" +
                "mCreatedAt=" + mCreatedAt +
                ", statusCode=" + mStatusCode +
                ", statusMessage='" + mMessage + '\'' +
                ", content='" + dumpContent() + '\'' +
                ", headers=" + dumpHeaders() +
                '}';
    }
}
