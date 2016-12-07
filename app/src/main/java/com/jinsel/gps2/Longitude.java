package com.jinsel.gps2;

import java.math.BigDecimal;
import java.math.RoundingMode;

//Traditional longitude class

public class Longitude {
    String direction;
    int degrees;
    int minutes;
    double seconds;

    Longitude(){}

    Longitude(double decimal) { //Sets variables
        convertFromDecimal(decimal);
        findDirection(decimal);
    }

    private void convertFromDecimal(double decimal) { //Converts from decimal coordinates
        BigDecimal bd = new BigDecimal(decimal);
        bd = bd.setScale(0, RoundingMode.DOWN);
        degrees = bd.intValue();
        bd = new BigDecimal((Math.abs(decimal)*60));
        bd = bd.setScale(0, RoundingMode.DOWN);
        minutes = bd.intValue()%60;
        bd = new BigDecimal((Math.abs(decimal)*3600)%60);
        bd = bd.setScale(1, RoundingMode.HALF_DOWN);
        seconds = bd.doubleValue();
    }

    private void findDirection(double decimal) { //Finds direction
        if (decimal > 0) {
            direction = "E";
        }
        else {
            direction = "W";
        }
    }

    public String toString() { //To be displayed in GPS
        String longitude = degrees + "° " + minutes + "' " + seconds + '"' + " " + direction;
        return longitude;
    }
}
