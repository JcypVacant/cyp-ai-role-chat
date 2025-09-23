package com.cyp.cypairolechat.baiduyun;

import com.baidu.aip.speech.AipSpeech;
import org.json.JSONObject;

public class Sample {
    //设置APPID/AK/SK
    public static final String APP_ID = "120152683";
    public static final String API_KEY = "7AvpeoLeLZsgNmnOektnoSWj";
    public static final String SECRET_KEY = "VULTFxN0qcrcfUMgYkkAxdknpBghRovY";

    public void asr(AipSpeech client,String path)
    {
        // 对本地语音文件进行识别
        JSONObject asrRes = client.asr(path, "wav", 16000, null);
        System.out.println(asrRes);

    }
    public static void main(String[] args) {
        // 初始化一个AipSpeech
        AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(20000);
        client.setSocketTimeoutInMillis(60000);
        String path = "D:\\Java\\JavaProject\\cyp-ai-role-chat\\voice\\nls-sample-16k.wav";

        // 调用接口
        Sample sample = new Sample();
        sample.asr(client, path);
        
    }

}
