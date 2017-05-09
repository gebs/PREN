package ch.hslu.pren.team8.ziffer;

import org.opencv.core.Point;

/**
 * Created by gebs on 5/6/17.
 */
public class RomanNumeralLine {
    private double angle;
    private Point pt1;
    private Point pt2;

    public RomanNumeralLine(double angle, Point pt1, Point pt2) {
        this.angle = angle;
        this.pt1 = pt1;
        this.pt2 = pt2;
    }

    public boolean isStraightLine() {
        return (angle < 100 && angle > 80);
    }

    public boolean isVLine() {
        return ((angle > 100 && angle < 180) || (angle > 0 && angle < 80));
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public boolean isNear(Point npt1, Point npt2) {
        double ofpt1 = Math.abs(pt1.x - npt1.x);
        double ofpt2 = Math.abs(pt2.x - npt2.x);

        return (ofpt1 < 10 || ofpt2 < 10);
    }
}
