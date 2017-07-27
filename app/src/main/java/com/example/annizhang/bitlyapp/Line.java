package com.example.annizhang.bitlyapp;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by annizhang on 7/25/17.
 */

public class Line {
    private JSONObject line;
    private List<Word> words;
    public List<Word> getWords(){
        return words;
    }

    public JSONObject getLine(){
        return line;
    }

    @Override
    public String toString(){
        String allWords ="";
        for (Word word: words){
            allWords += word.toString();
            allWords += " ";
        }
        return allWords;
    }

}
