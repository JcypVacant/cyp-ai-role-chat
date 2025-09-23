package com.cyp.cypairolechat.baiduyun;

import com.baidu.aip.speech.AipSpeech;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class AsrService {
    @Resource
    private AipSpeech aipSpeechClient;

    public String recognize(File file) {
        JSONObject res = aipSpeechClient.asr(file.getAbsolutePath(), "wav", 16000, null);
        log.info("ASR返回: {}", res.toString());
        if (res.has("result")) {
            return res.getJSONArray("result").getString(0);
        }
        return "";
    }
}
