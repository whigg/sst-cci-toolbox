/*
 * SST_cci Tools
 *
 * Copyright (C) 2011-2013 by Brockmann Consult GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.esa.cci.sst.common.calculator;

import org.esa.cci.sst.common.SpatialResolution;
import org.esa.cci.sst.common.TemporalResolution;

/**
 * Calculates average spatial and temporal separations for calculating synoptic uncertainties.
 * <p/>
 *
 * @author Bettina Scholze
 * @author Ralf Quast
 */
public class AverageSeparations {

    private final double spatialResolution;
    private final TemporalResolution temporalResolution;

    /**
     * Creates a new instance of this class.
     *
     * @param spatialResolution  The spatial target resolution.
     * @param temporalResolution The temporal target resolution.
     */
    public AverageSeparations(SpatialResolution spatialResolution, TemporalResolution temporalResolution) {
        this.spatialResolution = spatialResolution.getResolution();
        this.temporalResolution = temporalResolution;
    }

    /**
     * Calculates the average synoptic separation distance.
     *
     * @param y The index of a cell in the target grid.
     *
     * @return the average separation distance (km).
     */
    public double getDxy(int y) {
        if (spatialResolution <= 0.05) {
            return 0.0;
        }

        double aPole = 37.2069;
        double bPole = -0.101691;
        double dPole = aPole * spatialResolution + bPole;

        double aEquator = 57.8881;
        double bEquator = 0.272744;
        double dEquator = aEquator * spatialResolution + bEquator;

        final double lat = 90.0 - spatialResolution * (y + 0.5);
        final double f = Math.abs(lat) / 90.0;

        return dPole * f + dEquator * (1.0 - f);
    }

    /**
     * Returns the average synoptic time separation.
     *
     * @return the average time separation (days).
     */
    public double getDt() {
        if (TemporalResolution.daily.equals(temporalResolution)) {
            return 0.0;
        } else if (TemporalResolution.weekly5d.equals(temporalResolution)) {
            if (spatialResolution <= 1.5) {
                return 2.0;
            } else if (spatialResolution <= 2.5) {
                return 1.0;
            } else {
                return 0.0;
            }
        } else if (TemporalResolution.weekly7d.equals(temporalResolution)) {
            if (spatialResolution <= 1.75) {
                return 2.0;
            } else if (spatialResolution <= 2.5) {
                return 1.0;
            } else {
                return 0.0;
            }
        } else if (TemporalResolution.monthly.equals(temporalResolution)) {
            if (spatialResolution <= 0.5) {
                return 10.0;
            } else if (spatialResolution <= 0.75) {
                return 9.0;
            } else if (spatialResolution <= 0.8) {
                return 8.5;
            } else if (spatialResolution <= 1.0) {
                return 6.0;
            } else if (spatialResolution <= 1.2) {
                return 3.5;
            } else if (spatialResolution <= 1.25) {
                return 3.0;
            } else if (spatialResolution <= 2.00) {
                return 0.5;
            } else if (spatialResolution <= 2.25) {
                return 0.25;
            } else if (spatialResolution <= 2.5) {
                return 0.2;
            } else if (spatialResolution <= 3.0) {
                return 0.1;
            } else {
                return 0.0;
            }
        } else {
            return 0.0;
        }
    }

    public double calculateEta(int y, long sampleCount) {
        final double dt = getDt();
        final double dxy = getDxy(y);

        final double lxy = 100.0; // km
        final double lt = 1.0; // day

        final double r = Math.exp(-0.5 * (dxy / lxy + dt / lt));

        return sampleCount / (1 + r * (sampleCount - 1));
    }

}
