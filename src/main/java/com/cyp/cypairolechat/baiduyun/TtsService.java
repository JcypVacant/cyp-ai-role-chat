package com.cyp.cypairolechat.baiduyun;

import com.baidu.aip.speech.AipSpeech;
import com.baidu.aip.speech.TtsResponse;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TtsService {

    private final AipSpeech aipSpeechClient;

    public File synthesize(String text) {
        JSONObject options = new JSONObject();
        options.put("spd", "5"); // 语速
        options.put("pit", "5"); // 音调
        options.put("vol", "7"); // 音量
        options.put("per", "0"); // 发音人

        // 1. 将 JSONObject 转成 HashMap<String, Object>
        HashMap<String, Object> map = new HashMap<>(options.toMap());

        TtsResponse res = aipSpeechClient.synthesis(text, "zh", 1, map);

        byte[] data = res.getData();
        if (data != null) {
            try {
                File outFile = File.createTempFile(UUID.randomUUID().toString(), ".mp3");
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(data);
                }
                return outFile;
            } catch (Exception e) {
                throw new RuntimeException("TTS保存文件失败", e);
            }
        }
        throw new RuntimeException("TTS合成失败: " + res.getResult());
    }
}
