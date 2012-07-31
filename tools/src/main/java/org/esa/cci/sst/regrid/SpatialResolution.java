/*
 * Copyright (C) 2012 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.cci.sst.regrid;

import org.esa.cci.sst.util.GridDef;

import java.io.IOException;
import java.util.Arrays;

public enum SpatialResolution {

    DEGREE_0_05(0.05),
    DEGREE_0_10(0.10),
    DEGREE_0_15(0.15),
    DEGREE_0_20(0.20),
    DEGREE_0_25(0.25),
    DEGREE_0_30(0.30),
    DEGREE_0_40(0.40),
    DEGREE_0_50(0.50),
    DEGREE_0_60(0.60),
    DEGREE_0_75(0.75),
    DEGREE_0_80(0.80),
    DEGREE_1_00(1.00),
    DEGREE_1_20(1.20),
    DEGREE_1_25(1.25),
    DEGREE_2_00(2.00),
    DEGREE_2_25(2.25),
    DEGREE_2_40(2.40),
    DEGREE_2_50(2.50),
    DEGREE_3_00(3.00),
    DEGREE_3_75(3.75),
    DEGREE_4_00(4.00),
    DEGREE_4_50(4.50),
    DEGREE_5_00(5.00),
    DEGREE_10_00(10.00);

    private double value;
    private GridDef gridDef;

    private SpatialResolution(double value) {
        this.value = value;
    }

    public static String getDefaultValueAsString() {
        return String.valueOf(DEGREE_0_05.getValue());
    }

    public static String getValuesAsString() {
        SpatialResolution[] values = SpatialResolution.values();
        double[] resolutions = new double[values.length];
        int i = 0;
        for (SpatialResolution spatialResolution : values) {
            resolutions[i++] = spatialResolution.getValue();
        }
        return Arrays.toString(resolutions);
    }

    public static SpatialResolution getFromValue(String value) {
        for (SpatialResolution spatialResolution : SpatialResolution.values()) {
            if (Double.valueOf(value) == spatialResolution.value) {
                return spatialResolution;
            }
        }
        throw new IllegalArgumentException("argument must be one of the values of SpatialResolution.");
    }

    public GridDef getAssociatedGridDef() {
        if (this.gridDef == null) {
            this.gridDef = GridDef.createGlobal(this.value);
        }
        return gridDef;
    }

    public double getValue() {
        return value;
    }

    public static int[] convertShape(SpatialResolution targetResolution, int[] sourceShape, GridDef sourceGridDef) throws IOException {
        int dimension = sourceShape.length;
        int[] shape = new int[dimension];
        double sourceResolution = sourceGridDef.getResolutionX();

        if (sourceResolution != sourceGridDef.getResolutionY()) {
            throw new IOException("Different resolution on lat and lon are not supported.");
        }

        for (int i = 0; i < dimension; i++) {
            int sourceDim = sourceShape[i];
            if (sourceDim == sourceGridDef.getHeight()) {
                shape[i] = (int) (180 / targetResolution.value);
            } else if (sourceDim == sourceGridDef.getWidth()) {
                shape[i] = (int) (360 / targetResolution.value);
            } else {
                shape[i] = sourceDim;
            }
        }
        return shape;
    }
}