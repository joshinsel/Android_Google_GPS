package com.jinsel.gps2;

import java.math.BigDecimal;
import java.math.RoundingMode;

//Traditional latitude class

public class Latitude {
    String direction;
    double degrees;
    double minutes;
    double seconds;

    Latitude(){}

    Latitude(double decimal) { //Sets variables
        convertFromDecimal(decimal);
        findDirection(decimal);
    }

    private void convertFromDecimal(double decimal) { //Converts from decimal coordinates
        BigDecimal bd = new BigDecimal(decimal);
        bd = bd.setScale(0, RoundingMode.DOWN);
        degrees = bd.doubleValue();
        bd = new BigDecimal((Math.abs(decimal)*60));
        bd = bd.setScale(0, RoundingMode.DOWN);
        minutes = bd.doubleValue()%60;
        bd = new BigDecimal((Math.abs(decimal)*3600)%60);
        bd = bd.setScale(1, RoundingMode.HALF_DOWN);
        seconds = bd.doubleValue();
    }

    private void findDirection(double decimal) { //Finds direction of coordinates
        if (decimal > 0) {
            direction = "N";
        }
        else {
            direction = "S";
        }
    }

    public String toString() { //To be displayed in GPS
        String latitude = degrees + "° " + minutes + "' " + seconds + '"' + " " + direction;
        return latitude;
    }
}

