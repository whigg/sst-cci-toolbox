package org.esa.cci.sst.common.cellgrid;/*
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

public class XTransposer extends AbstractSamplePermuter {

    public XTransposer(Grid grid) {
        super(grid);
    }

    @Override
    protected int getSourceX(int x) { // swap left and right halves
        final int h = getGridDef().getWidth() / 2;
        return x < h ? x + h : x - h;
    }

    @Override
    protected int getSourceY(int y) {
        return y;
    }
}