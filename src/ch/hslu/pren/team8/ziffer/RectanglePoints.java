package ch.hslu.pren.team8.ziffer;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gebs on 3/16/17.
 */
class RectanglePoints {
    private int id;
    private MatOfPoint2f points;
    private Point point;
    private PointPosition position;
    private boolean isSinglePoint;
    RectanglePoints(){}

    RectanglePoints(int _id,MatOfPoint2f _points){
        this.id = _id;
        this.points = _points;
        this.isSinglePoint = false;
    }
    RectanglePoints(Point _point,PointPosition _position){
        this.point = _point;
        this.position = _position;
        this.isSinglePoint = true;
    }

    int getId(){
        return id;
    }
    MatOfPoint2f getPoints(){
        return points;
    }
    PointPosition getPosition(){return position;}
    void setPoints(MatOfPoint2f _points){
        this.points = points;
    }
    void addPoints(MatOfPoint2f _points){
        List<Point> pts = new ArrayList<Point>();
        pts.addAll(this.points.toList());
        List<Point> pts2 = _points.toList();
        pts.addAll(pts2);
        this.points.fromList(pts);

    }
    Point getPoint(){return point;}

}
