package com.mesung.objectEquals.symmetry;

import java.awt.*;

public class MainPoint {

    public static void main(String [] args) {

        //1.
        Point p = new Point(1, 2);
        ColorPoint cp = new ColorPoint(1, 2, Color.RED);

        System.out.println(p.equals(cp));
        System.out.println(cp.equals(p));


        //2. ColorPoint equals 변경
        ColorPoint p1 = new ColorPoint(1, 2, Color.RED);
        Point p2 = new Point(1, 2);
        ColorPoint p3 = new ColorPoint(1, 2, Color.BLUE);

        SmallPoint sm = new SmallPoint(1, 2, Color.RED);
        System.out.println(p2.equals(sm));
    }
}
