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

package org.esa.cci.sst.common.cellgrid;

import org.esa.cci.sst.common.cell.Cell;
import org.esa.cci.sst.common.cell.CellFactory;
import org.esa.cci.sst.common.cell.CellFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * A sparsely filled, regular, rectangular arrangement of {@link org.esa.cci.sst.common.cell.Cell}s used for numeric aggregations.
 *
 * @author Norman Fomferra
 */
public class CellGrid<C extends Cell> {
    private final GridDef gridDef;
    private final int width;
    private final int height;
    private final CellFactory<C> cellFactory;
    private final C[] cells;

    public CellGrid(GridDef gridDef, CellFactory<C> cellFactory) {
        this.gridDef = gridDef;
        this.cellFactory = cellFactory;
        width = gridDef.getWidth();
        height = this.gridDef.getHeight();
        cells = (C[]) new Cell[gridDef.getWidth() * gridDef.getHeight()];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public GridDef getGridDef() {
        return gridDef;
    }

    public CellFactory<C> getCellFactory() {
        return cellFactory;
    }

    public C getCellSafe(int x, int y) {
        C cell = getCell(x, y);
        if (cell == null) {
            cell = createCell(x, y);
            setCell(x, y, cell);
        }
        return cell;
    }

    public C getCell(int x, int y) {
        //noinspection unchecked
        return cells[y * width + x];
    }

    public void setCell(C cell) {
        setCell(cell.getX(), cell.getY(), cell);
    }

    public void setCell(int x, int y, C cell) {
        if (x != cell.getX() || y != cell.getY()) {
            throw new IllegalArgumentException("Cell location does not match");
        }
        cells[y * width + x] = cell;
    }

    public C createCell(int x, int y) {
        return getCellFactory().createCell(x, y);
    }

    public List<C> getCells() {
        return getCells(CellFilter.ALL);
    }

    public List<C> getCells(CellFilter<C> filter) {
        ArrayList<C> cellList = new ArrayList<C>(getWidth() * getHeight());
        for (C cell : cells) {
            if (cell != null && filter.accept(cell)) {
                cellList.add(cell);
            }
        }
        return cellList;
    }

}