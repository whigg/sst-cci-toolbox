package org.esa.cci.sst.util;

import org.postgis.Point;

import java.util.List;

import static java.lang.Math.*;

/**
 * Utility class for doing something with PostGIS geometries.
 */
public class PgUtil {

    private PgUtil() {
    }

    /**
     * Finds out if the polygon defined by list of points has clockwise orientation.
     * The list of points must correspond to a polygon on the Earth's surface; the
     * first point in the list must be the same as the last, i.e. the polygon must
     * be closed.
     * <p/>
     * The algorithm makes the assumption that the first point is a corner point.
     *
     * @param geoPolygon The polygon.
     *
     * @return {@code true} if the polygon has clockwise orientation, {@code false}
     *         otherwise.
     */
    public static boolean isClockwise(List<Point> geoPolygon) {
        // points defining the corner
        final Point p1 = geoPolygon.get(geoPolygon.size() - 2);
        final Point p2 = geoPolygon.get(0);
        final Point p3 = geoPolygon.get(1);

        final double[] a = toXYZ(p1);
        final double[] b = toXYZ(p2);
        final double[] c = toXYZ(p3);
        // difference vector a - b
        final double[] d = new double[]{a[0] - b[0], a[1] - b[1], a[2] - b[2]};
        // difference vector c - b
        final double[] e = new double[]{c[0] - b[0], c[1] - b[1], c[2] - b[2]};
        // cross product of (a - b) and (c - b)
        final double[] f = new double[]{
                d[1] * e[2] - d[2] * e[1],
                d[2] * e[0] - d[0] * e[2],
                d[0] * e[1] - d[1] * e[0]
        };
        // scalar product of f and b
        final double s = f[0] * b[0] + f[1] * b[1] + f[2] * b[2];

        return s > 0;
    }

    private static double[] toXYZ(Point p) {
        final double u = toRadians(p.getX());
        final double v = toRadians(p.getY());
        final double w = cos(v);

        final double ax = cos(u) * w;
        final double ay = sin(u) * w;
        final double az = sin(v);

        return new double[]{ax, ay, az};
    }

}
