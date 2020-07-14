package com.mesung.objectEquals.symmetry;

import java.awt.*;
import java.util.Objects;

public class InheritanceColorPoint {
    private final Point point;
    private final Color color;

    public InheritanceColorPoint(int x, int y, Color color) {
        point = new Point(x, y);
        this.color = Objects.requireNonNull(color);
    }

    public Point asPoint() {
        return point;
    }

    public boolean equals(Object o) {
        if(!(o instanceof InheritanceColorPoint)) {
            return false;
        }

        InheritanceColorPoint cp = (InheritanceColorPoint) o;
        return cp.point.equals(point) && cp.color.equals(color);
    }
}
