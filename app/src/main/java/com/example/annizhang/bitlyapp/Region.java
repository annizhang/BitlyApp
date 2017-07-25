package com.example.annizhang.bitlyapp;

import java.util.List;

/**
 * Created by annizhang on 7/25/17.
 */

public class Region {
    private List<Line> lines;
    public List<Line> getLines(){
        return lines;
    }

    @Override
    public String toString(){
        String regionWords = "";
        for (Line line : lines){
            regionWords += line.toString();
            regionWords += " ";
        }
        return regionWords;
    }
}
