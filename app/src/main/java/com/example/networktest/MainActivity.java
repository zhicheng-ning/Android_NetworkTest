package com.example.networktest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    TextView responseText;
    Button btnSendRequest;
    private static final   String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSendRequest = findViewById(R.id.send_request);
        responseText = findViewById(R.id.response_text);
        btnSendRequest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send_request) {
//            sendRequestWithHttpURLConnection();
            sendRequestWithOkHttp();
        }
    }

    /**
     * 推荐使用
     * OkHttp更为简单的发起一个http请求并且获得response的内容
     */
    private void sendRequestWithOkHttp() {
//        注意开启一个子线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
//              解析XML文件   https://inzc.top/nzc/androidTest/get_data.xml
                Request request = new Request.Builder().url("https://inzc.top/nzc/androidTest/get_data.json").build();
                try {
                    Response response = client.newCall(request).execute();
                    String res = response.body().string();
//                    showResponse(res);
//                    parseXMLWithPull(res);
//                    parseXMLWithSAX(res);
//                    parseJSONWithJSONObject(res);
                    parseJSONWithGSON(res);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 使用谷歌提供的GSON开源库可以让解析JSON数据
     * @param jsonData
     */
    private void parseJSONWithGSON(String jsonData) {
        Gson gson = new Gson();
        List<App> apps = new ArrayList<>();
        apps = gson.fromJson(jsonData, new TypeToken<List<App>>() {
        }.getType());
        for (App app : apps) {
            Log.d(TAG, "id is" + app.getId());
            Log.d(TAG, "name is" + app.getName());
            Log.d(TAG, "version is" + app.getVersion());
        }
    }

    /**
     * [{"id":"5","version":"5.5","name":"Clash of Clans"},
     * {"id":"6","version":"7.0","name":"Boom Beach"},
     * {"id":"7","version":"3.5","name":"Clash Royale"}]
     * @param jsonData
     */
    private void parseJSONWithJSONObject(String jsonData) {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                String version = jsonObject.getString("version");
                Log.d(TAG, "id is " + id);
                Log.d(TAG, "name is " + name);
                Log.d(TAG, "version is " + version);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    /**
     * parseXMLWithSAX() 方法中先是创建了一个SAXParserFactory 的对象， 然后再获取
     * 到XMLReader 对象， 接着将我们编写的ContentHandler的实例设置到XMLReader中， 最后调
     * 用parse() 方法开始执行解析就好了。
     * @param xmlData
     */
    private void parseXMLWithSAX(String xmlData) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader xmlReader = factory.newSAXParser().getXMLReader();
            ContentHandler handler = new ContentHandler();
// 将ContentHandler的实例设置到XMLReader中
            xmlReader.setContentHandler(handler);
// 开始执行解析
            xmlReader.parse(new InputSource(new StringReader(xmlData)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 推荐使用
     * 这里首先要获取到一个XmlPullParserFactory 的实例， 并借助这个实例得到XmlPullParser 对象， 然后调
     * 用XmlPullParser 的setInput() 方法将服务器返回的XML数据设置进去就可以开始解析
     * 了。 解析的过程也非常简单， 通过getEventType() 可以得到当前的解析事件， 然后在一个
     * while循环中不断地进行解析， 如果当前的解析事件不等于XmlPullParser.END_DOCUMENT， 说
     * 明解析工作还没完成， 调用next() 方法后可以获取下一个解析事件。
     * 在while 循环中， 我们通过getName() 方法得到当前节点的名字， 如果发现节点名等于id、
     * name或version， 就调用nextText() 方法来获取节点内具体的内容， 每当解析完一个app节点
     * 后就将获取到的内容打印出来。
     *
     * @param xmlData
     */
    private void parseXMLWithPull(String xmlData) {
        try {
            String ID = "id", NAME = "name", VERSION = "version";
            String id = "";
            String name = "";
            String version = "";
            XmlPullParser x = Xml.newPullParser();
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType = xmlPullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String nodeName = xmlPullParser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (nodeName.equals(ID)) {
                            id = xmlPullParser.nextText();
                        } else if (nodeName.equals(NAME)) {
                            name = xmlPullParser.nextText();
                        } else if (nodeName.equals(VERSION)) {
                            version = xmlPullParser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (nodeName.equals("app")) {
                            Log.d(TAG, "id is " + id);
                            Log.d(TAG, "name is " + name);
                            Log.d(TAG, "version is " + version);
                        }
                        break;
                    default:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 系统传统发起http请求，不推荐
     */
    private void sendRequestWithHttpURLConnection() {
//        开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                BufferedReader reader = null;
                try {
                    URL url = new URL("https://www.inzc.top/nzc/site01");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
//                    注意从inputStream中读取的一般流程
                    InputStream in = connection.getInputStream();
//                    下面对获取到的输入流进行读取
                    reader = new BufferedReader(new InputStreamReader(in));
//                    response为服务器返回的数据
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    showResponse(response.toString());

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void showResponse(final String response) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                responseText.setText(response);
            }
        });
    }
}
