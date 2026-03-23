package com.example.final_project.ui.ghiam;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PCMUtilActivity {

    public static float[] readPCM16(String pcmPath) throws Exception {
        FileInputStream fis = new FileInputStream(pcmPath);
        byte[] bytes = new byte[fis.available()];
        fis.read(bytes);
        fis.close();

        int samples = bytes.length / 2;
        float[] audio = new float[samples];

        ByteBuffer buffer = ByteBuffer.wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN);

        for (int i = 0; i < samples; i++) {
            short s = buffer.getShort();
            audio[i] = s / 32768.0f;
        }
        return audio;
    }
}
