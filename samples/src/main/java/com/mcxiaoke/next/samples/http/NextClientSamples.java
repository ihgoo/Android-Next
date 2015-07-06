package com.mcxiaoke.next.samples.http;

import android.os.Bundle;
import android.util.Log;
import com.mcxiaoke.next.http.HttpMethod;
import com.mcxiaoke.next.http.NextClient;
import com.mcxiaoke.next.http.NextParams;
import com.mcxiaoke.next.http.NextRequest;
import com.mcxiaoke.next.http.NextResponse;
import com.mcxiaoke.next.samples.BaseActivity;
import com.mcxiaoke.next.samples.BuildConfig;
import com.mcxiaoke.next.samples.SampleUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: mcxiaoke
 * Date: 14-5-30
 * Time: 17:19
 */
public class NextClientSamples extends BaseActivity {
    public static final String TAG = NextClientSamples.class.getSimpleName();
    private static final boolean DEBUG = true;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread() {
            @Override
            public void run() {
                try {
                    testGet();
//                    testPost();
                    testPostJson();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();


    }

    private void testGet() throws IOException {
        final String url = "https://api.douban.com/v2/user/1000001";
        final NextRequest request = new NextRequest(HttpMethod.GET, url)
                .debug(true)
                .query("platform", "Android")
                .query("udid", "a0b609c99ca4bfdcef3d03a234d78d253d25e924")
                .forms("douban", "yes")
                .query("app_version", "1.5.2");
        final NextClient client = new NextClient().setDebug(true);
//

        final NextResponse response = client.execute(request);
        // get body as string
        Log.v(TAG, "http response content: "
                + SampleUtils.prettyPrintJson(response.string()));
    }

    private void testPostForm() throws IOException {
        final String url = "https://moment.douban.com/api/post/114309/like";
        final NextRequest request = new NextRequest(HttpMethod.POST, url)
                .debug(true)
                .header("X-UDID", "a0b609c99ca4bfdcef3d03a234d78d253d25e924")
                .query("platform", "Android")
                .query("udid", "a0b609c99ca4bfdcef3d03a234d78d253d25e924")
                .forms("version", "6")
                .query("app_version", "1.2.3");
        final NextClient client = new NextClient();
        final NextResponse response = client.execute(request);
        // get body as string
        Log.v(TAG, "http response content: "
                + SampleUtils.prettyPrintJson(response.string()));
    }

    private void testPostJson() throws JSONException, IOException {
        final String url = "https://api.github.com/gists";
        final NextRequest request = new NextRequest(HttpMethod.POST, url)
                .debug(true)
                .header("X-UDID", "a0b609c99ca4bfdcef3d03a234d78d253d25e924")
                .query("platform", "Android")
                .query("udid", "a0b609c99ca4bfdcef3d03a234d78d253d25e924")
                .forms("version", "6")
                .query("app_version", "1.2.3");
        request.userAgent("Samples test " + BuildConfig.APPLICATION_ID
                + "/" + BuildConfig.VERSION_NAME);
        JSONObject file1 = new JSONObject();
        file1.put("content", "gsgdsgsdgsdgsdgdsg gsdgjdslgk根深蒂固送到公司的");
        JSONObject file2 = new JSONObject();
        file2.put("content", "421414gsgdsgsdgsdgsdgdsg gsfdsfsddgjdslgk根深蒂固送到公司的");
        JSONObject files = new JSONObject();
        files.put("file1.txt", file1);
        files.put("file2.md", file2);
        JSONObject json = new JSONObject();
        json.put("description", "this is a gist for http post test");
        json.put("public", true);
        json.put("files", files);
        Log.v(TAG, "json string: " + json.toString());
        request.body(json.toString().getBytes());
        final NextClient client = new NextClient();
        final NextResponse response = client.execute(request);
        // get body as string
        Log.v(TAG, "http response content: "
                + SampleUtils.prettyPrintJson(response.string()));

        final Map<String, String> queries = new HashMap<>();
        final Map<String, String> forms = new HashMap<>();
        final Map<String, String> headers = new HashMap<>();
        final NextParams params = new NextParams();

        /**
        client.head(url);
        client.head(url, queries);
        client.head(url, queries, headers);

        client.get(url);
        client.get(url, queries);
        client.get(url, queries, headers);
        client.get(url, params);

        client.delete(url);
        client.delete(url, queries);
        client.delete(url, queries, headers);
        client.delete2(url, forms);
        client.delete2(url, forms, headers);
        client.delete(url, params);

        client.post(url, forms);
        client.post(url, forms, headers);
        client.post(url, params);

        client.put(url, forms);
        client.put(url, forms, headers);
        client.put(url, params);
         **/

    }
}
