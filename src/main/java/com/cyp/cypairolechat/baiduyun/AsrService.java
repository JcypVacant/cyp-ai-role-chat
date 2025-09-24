package com.cyp.cypairolechat.baiduyun;

import com.baidu.aip.speech.AipSpeech;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;

@Service
@Slf4j
public class AsrService {
    @Resource
    private AipSpeech aipSpeechClient;

    public String recognize(File file) throws Exception {
        // 1️⃣ 转换采样率到 16kHz
        File convertedFile = convertTo16kHz(file);
        JSONObject res = aipSpeechClient.asr(convertedFile.getAbsolutePath(), "wav", 16000, null);
        log.info("ASR返回: {}", res.toString());
        if (res.has("result")) {
            return res.getJSONArray("result").getString(0);
        }
        return "";
    }
    /**
     * 将 wav 文件转换为 16kHz、单声道、16bit PCM
     */
    private File convertTo16kHz(File sourceFile) throws Exception {
        AudioInputStream sourceAIS = AudioSystem.getAudioInputStream(sourceFile);
        AudioFormat sourceFormat = sourceAIS.getFormat();

        AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                16000,                     // 采样率
                16,                        // 采样位数
                1,                         // 声道数
                2,                         // 每帧字节数 = 位深/8 * 通道数
                16000,                     // 帧率
                false                      // 小端
        );

        AudioInputStream convertedAIS = AudioSystem.getAudioInputStream(targetFormat, sourceAIS);
        File tempFile = File.createTempFile("converted_", ".wav");
        AudioSystem.write(convertedAIS, AudioFileFormat.Type.WAVE, tempFile);

        sourceAIS.close();
        convertedAIS.close();
        return tempFile;
    }
}
