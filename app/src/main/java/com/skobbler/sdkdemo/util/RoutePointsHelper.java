package com.skobbler.sdkdemo.util;

import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKAnnotationText;
import com.skobbler.ngx.routing.SKExtendedRoutePosition;
import com.skobbler.ngx.util.SKComputingDistance;

import java.util.List;

/**
 * Helps process route gps points
 *
 * @version  1.01 2014/08/02
 *
 * @author Yuriy Pigovsky
 */
public class RoutePointsHelper {
    /**
     * Size of image, which is put on marks
     */
    public static final int MARK_DRAWING_SIZE = 10;

    /**
     * Takes list of route points and measures distance from the route start to each point
     * in this list.
     *
     * @param routePoints list of route points to compute distances from
     * @return array of doubles, which contain distances from begin of the route to its every point
     * @throws NullPointerException if <code>routePoints</code> is null
     */
    public static double[] computeDistanceToPoints(List<SKExtendedRoutePosition> routePoints) {
        SKExtendedRoutePosition previousPoint = routePoints.get(0);
        SKExtendedRoutePosition currentPoint;


        double routeLength = 0d;
        double[] distanceToPoints = new double[routePoints.size()];

        for (int i = 1; i < routePoints.size(); i++, previousPoint = currentPoint) {
            currentPoint = routePoints.get(i);

            double distanceFromPreviousToCurrent = SKComputingDistance.distanceBetween(
                    previousPoint.getLongitude(), previousPoint.getLatitude(),
                    currentPoint.getLongitude(), currentPoint.getLatitude()
            );

            routeLength += distanceFromPreviousToCurrent;
            distanceToPoints[i] = routeLength;
        }
        return distanceToPoints;
    }

    /**
     * Puts annotations with image <code>imagePath</code> on even distances <code>distanceToPutMark</code>
     * throughout route and returns them in an array.
     *
     * @param routePoints route described by a list of its gps points
     * @param distanceToPutMark distance between subsequent marks
     * @param imagePath path to image used for marking
     * @return array of calculated annotations
     */
    public static SKAnnotation[] calculateMarksOnRegularDistances(List<SKExtendedRoutePosition> routePoints,
                                                                  double distanceToPutMark, String imagePath) {
        if (routePoints == null) {
            return new SKAnnotation[0];
        }

        /* Route mark annotations indexes started from 2 as 0 and 1 are in use by start and finish
        * annotations */
        int annotationId = 2;
        int k = 1;
        double[] distanceToPoints = computeDistanceToPoints(routePoints);
        double previousToCurrent = distanceToPoints[1];

        int numberOfMarks = (int) Math.floor(distanceToPoints[distanceToPoints.length - 1] / distanceToPutMark);
        SKAnnotation[] annotations = new SKAnnotation[numberOfMarks];
        for (int i = 1; i <= numberOfMarks; ++i) {
            double markPosition = i * distanceToPutMark;
            while (distanceToPoints[k] < markPosition) {
                k++;
                previousToCurrent = distanceToPoints[k] - distanceToPoints[k - 1];
            }
            double previousToMark = markPosition - distanceToPoints[k - 1];
            double alpha = previousToMark / previousToCurrent;

            SKCoordinate markLocation = new SKCoordinate(
                    routePoints.get(k - 1).getLongitude() * (1d - alpha) + routePoints.get(k).getLongitude() * alpha,
                    routePoints.get(k - 1).getLatitude() * (1d - alpha) + routePoints.get(k).getLatitude() * alpha
            );

            SKAnnotation annotation = new SKAnnotation();
            // The image should be a power of 2. _( 32x32, 64x64, etc)
            annotation.setImagePath(imagePath);

            annotation.setImageSize(MARK_DRAWING_SIZE);
            annotation.setLocation(markLocation);
            SKAnnotationText label = new SKAnnotationText();
            label.setText(String.format("%d", i));

            annotation.setText(label);
            annotation.setUniqueID(annotationId++);
            annotations[i - 1] = annotation;
        }
        return annotations;
    }

    /**
     * Endpoint types used for route setup
     */
    public static enum EndpointType {
        No,
        Start,
        Finish
    }
}
