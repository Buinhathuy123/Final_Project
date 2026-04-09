package com.example.final_project.text;

import android.content.Context;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

public class TokenizerHelper {

    private HashMap<String, Integer> wordIndex = new HashMap<>();
    private int oovIndex = 1;

    public TokenizerHelper(Context context) {
        try {
            InputStream is = context.getAssets().open("tokenizer.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");

            JSONObject obj = new JSONObject(json);
            JSONObject config = obj.getJSONObject("config");

            String wordIndexStr = config.getString("word_index");
            JSONObject word_index = new JSONObject(wordIndexStr);

            Iterator<String> keys = word_index.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                int value = word_index.getInt(key);
                wordIndex.put(key, value);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int[] textToSequence(String text, int maxLen) {

        text = text.toLowerCase().trim();

        String[] words = text.split("\\s+");

        int[] sequence = new int[maxLen];

        for (int i = 0; i < maxLen; i++) {
            if (i < words.length) {
                sequence[i] = wordIndex.getOrDefault(words[i], oovIndex);
            } else {
                sequence[i] = 0;
            }
        }

        return sequence;
    }
}