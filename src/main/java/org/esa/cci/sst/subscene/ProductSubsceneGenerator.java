/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package org.esa.cci.sst.subscene;

import com.bc.ceres.core.Assert;
import org.esa.beam.dataio.netcdf.util.DataTypeUtils;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.GeoPos;
import org.esa.beam.framework.datamodel.MetadataAttribute;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.PixelPos;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductData;
import org.esa.beam.util.ProductUtils;
import org.esa.cci.sst.Constants;
import org.esa.cci.sst.orm.PersistenceManager;
import org.postgis.LinearRing;
import org.postgis.PGgeometry;
import org.postgis.Point;
import org.postgis.Polygon;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriteable;

import javax.persistence.Query;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Abstract implementation of {@link org.esa.cci.sst.subscene.SubsceneGeneratorTool.SubsceneGenerator} responsible for
 * creating and applying subscenes on data products, which can be read using the BEAM API.
 * Sensor-specific subclasses of this class merely need to provide the dimension size that shall be used for the
 * subscene.
 *
 * @author Thomas Storm
 */
abstract class ProductSubsceneGenerator extends AbstractSubsceneGenerator {

    /**
     * all matchup-ids for matchups with a reference observation oref, which has
     * - a time point within 24 hours of the product's time point
     * - a location within the product bounds
     */
    private static final String GET_MATCHUP_IDS =
            "select m.id"
            + " from mm_matchup m, mm_observation oref"
            + " where m.refobs_id = oref.id"
            + " and oref.sensor = ?"
            + " and oref.time >= ? and oref.time < ?"
            + " and ST_intersects(geometry(?), oref.point)";

    /**
     * the reference point for a given matchup id
     */
    private static final String GET_POINT_FOR_MATCHUP =
            "select oref.point"
            + " from mm_matchup m, mm_observation oref"
            + " where m.id = ?"
            + " and m.refobs_id = oref.id";


    private final String sensorName;

    ProductSubsceneGenerator(PersistenceManager persistenceManager, String sensorName) {
        super(persistenceManager);
        this.sensorName = sensorName;
    }

    @Override
    public void createSubscene(SubsceneGeneratorTool.SubsceneIO subsceneIO) throws IOException {
        PersistenceManager persistenceManager = getPersistenceManager();
        String inputFilename = subsceneIO.getInputFilename();
        String outputFilename = subsceneIO.getOutputFilename();
        Product product = ProductIO.readProduct(inputFilename);
        Date productsTime = getTimeStamp(product);
        GeoCoding geoCoding = product.getGeoCoding();
        GeoPos upperLeft = new GeoPos();
        GeoPos lowerRight = new GeoPos();
        geoCoding.getGeoPos(new PixelPos(0, 0), upperLeft);
        geoCoding.getGeoPos(new PixelPos(product.getSceneRasterWidth(), product.getSceneRasterHeight()), lowerRight);
        NetcdfFileWriteable ncFile = null;
        persistenceManager.transaction();
        try {
            List<Integer> matchupIds = getMatchupIds(sensorName, productsTime, createRegion(upperLeft, lowerRight));
            ncFile = createNcFile(outputFilename, product, matchupIds);
            for (Band band : product.getBands()) {
                writeBand(ncFile, band, matchupIds);
            }
        } catch (InvalidRangeException e) {
            throw new IOException("Error writing to netcdf file.", e);
        } finally {
            persistenceManager.commit();
            if (ncFile != null) {
                ncFile.close();
            }
        }
    }

    /**
     * Create a PGgeometry that may be checked within the database if it contains the reference points of matchups.
     * @param upperLeft
     * @param lowerRight
     * @return
     */
    String createRegion(GeoPos upperLeft, GeoPos lowerRight) {
        // todo - ts 17. Mar 2011 - this is wrong

        final LinearRing[] rings = new LinearRing[]{
                new LinearRing(new Point[]{
                        new Point(upperLeft.lat, upperLeft.lon),
                        new Point(upperLeft.lat, lowerRight.lon),
                        new Point(lowerRight.lat, lowerRight.lon),
                        new Point(lowerRight.lat, upperLeft.lon),
                        new Point(upperLeft.lat, upperLeft.lon)
                })
        };
        return new Polygon(rings).toString();
    }

    void writeBand(NetcdfFileWriteable ncFile, Band band, List<Integer> matchupIds) throws IOException,
                                                                                           InvalidRangeException {
        final GeoCoding geoCoding = band.getGeoCoding();
        for (int matchupId : matchupIds) {
            final Point centralPoint = getPoint(matchupId);
            final Rectangle2D sourceBounds = createBounds(centralPoint, band);
            int[] shape = createShape(matchupIds.size(), sourceBounds);
            Array values = Array.factory(DataTypeUtils.getNetcdfDataType(band), shape);
            final PixelPos startPixelPos = new PixelPos();
            geoCoding.getPixelPos(new GeoPos((float) sourceBounds.getX(), (float) sourceBounds.getY()), startPixelPos);
            int index = 0;
            for (int x = (int) startPixelPos.x; x < startPixelPos.x + sourceBounds.getWidth(); x++) {
                for (int y = (int) startPixelPos.y; y < startPixelPos.y + sourceBounds.getHeight(); y++) {
                    Object value = getValue(band, x, y);
                    values.setObject(index, value);
                    index++;
                }
            }

            ncFile.write(band.getName(), values);
        }
    }

    Object getValue(final Band band, final int x, final int y) {
        Object value = null;
        switch (band.getDataType()) {
            case ProductData.TYPE_FLOAT64: {
                value = ProductUtils.getGeophysicalSampleDouble(band, x, y, 0);
                break;
            }
            case ProductData.TYPE_FLOAT32: {
                value = (float) ProductUtils.getGeophysicalSampleDouble(band, x, y, 0);
                break;
            }
            case ProductData.TYPE_INT8:
            case ProductData.TYPE_INT16:
            case ProductData.TYPE_INT32:
            case ProductData.TYPE_UINT8:
            case ProductData.TYPE_UINT16:
            case ProductData.TYPE_UINT32: {
                value = ProductUtils.getGeophysicalSampleLong(band, x, y, 0);
                break;
            }
        }
        return value;
    }

    Rectangle2D createBounds(Point centralPoint, Band band) {
        final int sensorDimensionSize = getSensorDimensionSize();
        final GeoCoding geoCoding = band.getGeoCoding();
        PixelPos center = new PixelPos();
        geoCoding.getPixelPos(new GeoPos((float) centralPoint.x, (float) centralPoint.y), center);
        Assert.state(center.isValid(),
                     "Central geo-coordinate '" + centralPoint + "' not within bounds of band '" + band.getName() + "'.");
        float upper = center.x - sensorDimensionSize;
        float left = center.y - sensorDimensionSize;
        return new Rectangle2D.Double(left, upper, sensorDimensionSize, sensorDimensionSize);
    }

    Point getPoint(int matchupId) {
        final Query query = getPersistenceManager().createNativeQuery(GET_POINT_FOR_MATCHUP);
        query.setParameter(1, matchupId);
        try {
            String queryResult = query.getSingleResult().toString();
            return (Point) PGgeometry.geomFromString(queryResult);
        } catch (Exception e) {
            throw new IllegalStateException("No point for matchup-id '" + matchupId + "'.", e);
        }
    }

    int[] createShape(int matchupIdCount, Rectangle2D bounds) {
        int[] shape = new int[3];
        shape[0] = matchupIdCount;
        shape[1] = (int) bounds.getWidth();
        shape[2] = (int) bounds.getHeight();
        return shape;
    }

    @SuppressWarnings({"ConstantConditions"})
    Date getTimeStamp(Product product) {
        ProductData.UTC startTime = product.getStartTime();
        Assert.argument(startTime != null, "Product '" + product + "' has no start time.");
        ProductData.UTC endTime = product.getEndTime();
        if (endTime == null) {
            return startTime.getAsDate();
        }
        return new Date((startTime.getAsDate().getTime() + endTime.getAsDate().getTime()) / 2);
    }

    NetcdfFileWriteable createNcFile(String outputFilename, Product product, List<Integer> matchupIds) throws
                                                                                                       IOException {
        NetcdfFileWriteable ncFile = NetcdfFileWriteable.createNew(outputFilename);
        Group rootGroup = ncFile.getRootGroup();
        ncFile.addDimension(rootGroup, new Dimension(Constants.DIMENSION_NAME_MATCHUP, matchupIds.size(), true));
        ncFile.addDimension(rootGroup, new Dimension("ni", getSensorDimensionSize(), true));
        ncFile.addDimension(rootGroup, new Dimension("nj", getSensorDimensionSize(), true));
        for (Band band : product.getBands()) {
            String dimString = Constants.DIMENSION_NAME_MATCHUP + " ni nj";
            ncFile.addVariable(rootGroup, band.getName(), DataTypeUtils.getNetcdfDataType(band), dimString);
        }
        for (MetadataElement element : product.getMetadataRoot().getElements()) {
            for (MetadataAttribute attribute : element.getAttributes()) {
                ProductData data = attribute.getData();
                ncFile.addGlobalAttribute(element.getName(), data.getElemString());
            }
        }
        ncFile.create();
        return ncFile;
    }

    @SuppressWarnings({"unchecked"})
    List<Integer> getMatchupIds(String sensorName, Date productsTime, String bounds) {
        Query query = createQuery(sensorName, productsTime, bounds);
        return query.getResultList();
    }

    Query createQuery(final String sensorName, final Date productsTime, final String bounds) {
        final long time = productsTime.getTime();
        final long twelveHours = 12 * 60 * 60 * 1000;
        final Date minDate = new Date(time - twelveHours);
        final Date maxDate = new Date(time + twelveHours);
        final String queryString = String.format(GET_MATCHUP_IDS);
        Query query = getPersistenceManager().createNativeQuery(queryString);
        query.setParameter(1, sensorName);
        query.setParameter(2, minDate);
        query.setParameter(3, maxDate);
        query.setParameter(4, bounds);
        return query;
    }

}
