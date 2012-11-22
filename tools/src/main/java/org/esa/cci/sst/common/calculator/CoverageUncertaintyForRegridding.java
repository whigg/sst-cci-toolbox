package org.esa.cci.sst.common.calculator;

import org.esa.beam.util.math.MathUtils;
import org.esa.cci.sst.common.TemporalResolution;
import org.esa.cci.sst.common.auxiliary.LutForXTimeSpace;
import org.esa.cci.sst.regrid.SpatialResolution;

import java.util.Calendar;

/**
 * Equation 1.6 in 'SST_CCI-Tool-Spec-MetOffice-00x-Regridder_draft5.docx' from 06.11.2012
 * <p/>
 * {@author Bettina Scholze}
 * Date: 09.11.12 15:28
 */
public class CoverageUncertaintyForRegridding implements CoverageUncertainty {

    private SpatialResolution spatialResolution;
    private LutForXTimeSpace lutForXTime0;
    private LutForXTimeSpace lutForXSpace0;
    private final double xDay;

    public CoverageUncertaintyForRegridding(TemporalResolution temporalResolution,
                                            SpatialResolution spatialResolution,
                                            LutForXTimeSpace lutCuTime0,
                                            LutForXTimeSpace lutCuSpace0) {
        this.lutForXTime0 = lutCuTime0;
        this.lutForXSpace0 = lutCuSpace0;
        this.spatialResolution = spatialResolution;
        this.xDay = calculateXDay(temporalResolution);
    }

    static double calculateXDay(TemporalResolution temporalResolution) {
        double t0;
        if (TemporalResolution.seasonal.equals(temporalResolution) || TemporalResolution.annual.equals(temporalResolution)) {
            throw new IllegalArgumentException("temporalResolution must be 'daily' or 'monthly'");
        }
        if (TemporalResolution.daily.equals(temporalResolution)) {
            t0 = 0.0;
        } else {
            final Calendar calendar = Calendar.getInstance();
            calendar.setTime(temporalResolution.getDate1());
            final int monthMax = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            t0 = (double) monthMax;
        }
        return t0;
    }

    double calculateXKm(int cellX, int cellY) {
        final double boxSizeInDegree = spatialResolution.getValue();
        final double x1 = cellX * boxSizeInDegree;
        final double y1 = cellY * boxSizeInDegree;
        final double x2 = x1 + boxSizeInDegree;
        final double y2 = y1 + boxSizeInDegree;

        return MathUtils.sphereDistanceDeg(6371.0, x1, y1, x2, y2);
    }

    @Override
    public double calculateCoverageUncertainty(int cellX, int cellY, long n, double stdDeviation) {
        final double xKm = calculateXKm(cellX, cellY);

        double r_bar_time;
        if (xDay == 0.0) {
            r_bar_time = 1.0;
        } else {
            r_bar_time = (getX0Time(cellX, cellY) / xDay) * (1.0 - Math.exp(-xDay / getX0Time(cellX, cellY)));
        }
        double r_bar_space = (getX0Space(cellX, cellY) / xKm) * (1.0 - Math.exp(-xKm / getX0Space(cellX, cellY)));

        double r_bar = r_bar_space * r_bar_time;
        return Math.sqrt((stdDeviation * r_bar * (1.0 - r_bar)) / (1.0 + (n - 1.0) * r_bar));
    }

    private double getX0Time(int cellX, int cellY) {
        return lutForXTime0.getXValue(cellX, cellY);
    }

    private double getX0Space(int cellX, int cellY) {
        return lutForXSpace0.getXValue(cellX, cellY);
    }
}