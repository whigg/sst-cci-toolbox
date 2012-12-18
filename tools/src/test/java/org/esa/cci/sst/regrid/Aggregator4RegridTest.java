package org.esa.cci.sst.regrid;

import org.esa.cci.sst.common.SstDepth;
import org.esa.cci.sst.common.TemporalResolution;
import org.esa.cci.sst.common.TimeStep;
import org.esa.cci.sst.common.cell.AggregationCell;
import org.esa.cci.sst.common.cell.SpatialAggregationCell;
import org.esa.cci.sst.common.cellgrid.CellGrid;
import org.esa.cci.sst.common.cellgrid.GridDef;
import org.esa.cci.sst.tool.ToolException;
import org.esa.cci.sst.util.UTC;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

/**
 * {@author Bettina Scholze}
 * Date: 17.12.12 14:45
 */
public class Aggregator4RegridTest {

    private Aggregator4Regrid aggregator4Regrid;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Before
    public void setUp() throws Exception {
        this.aggregator4Regrid = new Aggregator4Regrid(null, null, null, null, null, null, null,
                SstDepth.skin, 0.0, SpatialResolution.DEGREE_0_50) {

            @Override
            CellGrid<SpatialAggregationCell> aggregateTimeRangeAndRegrid(Date date1, Date date2, SpatialResolution spatialResolution, TemporalResolution temporalResolution) throws IOException {
                return new CellGrid<SpatialAggregationCell>(GridDef.createGlobal(spatialResolution.getValue()), null);
            }

            @Override
            CellGrid<AggregationCell> aggregateMultiMonths(List<? extends TimeStep> monthlyTimeSteps) {
                return new CellGrid<AggregationCell>(GridDef.createGlobal(SpatialResolution.DEGREE_0_50.getValue()), null);

            }
        };
    }

    @Test
    public void testWeeklyWith7days() throws Exception {
        final TemporalResolution daily = TemporalResolution.weekly7d;
        Date startDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-11-01");

        //execution 1
        Date endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-11-07");
        List<RegriddingTimeStep> results = aggregator4Regrid.aggregate(startDate, endDate, daily);
        //verification 1
        assertEquals("7 days, will give 1 week", 1, results.size());

        //execution 2
        endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-11-23");
        results = aggregator4Regrid.aggregate(startDate, endDate, daily);
        //verification 2
        assertEquals("23 days, will give 3 weeks plus 1 incomplete week", 4, results.size());
        assertEquals("2012-11-01", df.format(results.get(0).getStartDate()));
        assertEquals("2012-11-08", df.format(results.get(0).getEndDate()));
        assertEquals("2012-11-08", df.format(results.get(1).getStartDate()));
        assertEquals("2012-11-15", df.format(results.get(1).getEndDate()));
        assertEquals("2012-11-15", df.format(results.get(2).getStartDate()));
        assertEquals("2012-11-22", df.format(results.get(2).getEndDate()));
        assertEquals("2012-11-22", df.format(results.get(3).getStartDate()));
        assertEquals("2012-11-29", df.format(results.get(3).getEndDate()));

    }

    @Test
    public void testWeeklyWith5days() throws Exception {
        final TemporalResolution daily = TemporalResolution.weekly5d;
        Date startDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-11-01");

        //execution 1
        Date endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-11-05");
        List<RegriddingTimeStep> results = aggregator4Regrid.aggregate(startDate, endDate, daily);
        //verification 1
        assertEquals(1, results.size());

        //execution 2
        endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-11-23");
        results = aggregator4Regrid.aggregate(startDate, endDate, daily);
        //verification 2
        assertEquals("", 5, results.size());
        assertEquals("2012-11-01", df.format(results.get(0).getStartDate()));
        assertEquals("2012-11-06", df.format(results.get(0).getEndDate()));
        assertEquals("2012-11-06", df.format(results.get(1).getStartDate()));
        assertEquals("2012-11-11", df.format(results.get(1).getEndDate()));
        assertEquals("2012-11-11", df.format(results.get(2).getStartDate()));
        assertEquals("2012-11-16", df.format(results.get(2).getEndDate()));
        assertEquals("2012-11-16", df.format(results.get(3).getStartDate()));
        assertEquals("2012-11-21", df.format(results.get(3).getEndDate()));
        assertEquals("2012-11-21", df.format(results.get(4).getStartDate()));
        assertEquals("2012-11-26", df.format(results.get(4).getEndDate()));
    }

    @Test
    public void testDaily() throws Exception {
        final TemporalResolution daily = TemporalResolution.daily;
        Date startDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-11-01");

        //execution 1
        Date endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-11-07");
        List<RegriddingTimeStep> results = aggregator4Regrid.aggregate(startDate, endDate, daily);
        //verification 1
        assertEquals("7 days, will give 7 coarser files", 7, results.size());
        assertEquals("2012-11-01", df.format(results.get(0).getStartDate()));
        assertEquals("2012-11-02", df.format(results.get(0).getEndDate()));
        assertEquals("2012-11-02", df.format(results.get(1).getStartDate()));
        assertEquals("2012-11-03", df.format(results.get(1).getEndDate()));
        assertEquals("2012-11-03", df.format(results.get(2).getStartDate()));
        assertEquals("2012-11-04", df.format(results.get(2).getEndDate()));
        assertEquals("2012-11-04", df.format(results.get(3).getStartDate()));
        assertEquals("2012-11-05", df.format(results.get(3).getEndDate()));
        assertEquals("2012-11-05", df.format(results.get(4).getStartDate()));
        assertEquals("2012-11-06", df.format(results.get(4).getEndDate()));
        assertEquals("2012-11-06", df.format(results.get(5).getStartDate()));
        assertEquals("2012-11-07", df.format(results.get(5).getEndDate()));
        assertEquals("2012-11-07", df.format(results.get(6).getStartDate()));
        assertEquals("2012-11-08", df.format(results.get(6).getEndDate()));

        //execution 2
        endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-11-23");
        results = aggregator4Regrid.aggregate(startDate, endDate, daily);
        //verification 2
        assertEquals("23 days, will give 23 coarser files", 23, results.size());
    }

    @Test
    public void testMonthly() throws Exception {
        final TemporalResolution daily = TemporalResolution.monthly;
        Date startDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-09-01");

        //execution 1
        Date endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-10-31");
        List<RegriddingTimeStep> results = aggregator4Regrid.aggregate(startDate, endDate, daily);
        //verification 1
        assertEquals("2 months, will give 3 coarser files", 2, results.size());
        assertEquals("2012-09-01", df.format(results.get(0).getStartDate()));
        assertEquals("2012-10-01", df.format(results.get(0).getEndDate()));
        assertEquals("2012-10-01", df.format(results.get(1).getStartDate()));
        assertEquals("2012-11-01", df.format(results.get(1).getEndDate()));

        //execution 2
        endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-11-01");
        results = aggregator4Regrid.aggregate(startDate, endDate, daily);
        //verification 2
        assertEquals("2 and a started 3rd months, will give 3 coarser files", 3, results.size());
        assertEquals("2012-09-01", df.format(results.get(0).getStartDate()));
        assertEquals("2012-10-01", df.format(results.get(0).getEndDate()));
        assertEquals("2012-10-01", df.format(results.get(1).getStartDate()));
        assertEquals("2012-11-01", df.format(results.get(1).getEndDate()));
        assertEquals("2012-11-01", df.format(results.get(2).getStartDate()));
        assertEquals("2012-12-01", df.format(results.get(2).getEndDate()));

        //execution 3
        startDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-03-04");
        endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-04-03");
        results = aggregator4Regrid.aggregate(startDate, endDate, daily);
        //verification 3
        assertEquals("1 month, will give 1 coarser file", 1, results.size());
        assertEquals("2012-03-04", df.format(results.get(0).getStartDate()));
        assertEquals("2012-04-04", df.format(results.get(0).getEndDate()));

    }

    @Test
    public void testSeasonal() throws Exception {
        final TemporalResolution daily = TemporalResolution.seasonal;
        Date startDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-01-01");

        //execution 1
        Date endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-09-30");
        List<RegriddingTimeStep> results = aggregator4Regrid.aggregate(startDate, endDate, daily);
        //verification 1
        assertEquals("3 seasons, will give 3 coarser files", 3, results.size());
        assertEquals("2012-01-01", df.format(results.get(0).getStartDate()));
        assertEquals("2012-04-01", df.format(results.get(0).getEndDate()));
        assertEquals("2012-04-01", df.format(results.get(1).getStartDate()));
        assertEquals("2012-07-01", df.format(results.get(1).getEndDate()));
        assertEquals("2012-07-01", df.format(results.get(2).getStartDate()));
        assertEquals("2012-10-01", df.format(results.get(2).getEndDate()));
    }

    @Ignore
    @Test
    public void testNotSupportedTemporalResolution() throws Exception {
        final TemporalResolution daily = TemporalResolution.weekly5d;
        Date startDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-09-01");

        //execution
        Date endDate = UTC.getDateFormat("yyyy-MM-dd").parse("2012-10-31");
        try {
            aggregator4Regrid.aggregate(startDate, endDate, daily);
            fail("ToolException expected");
        } catch (IOException e) {
            fail("ToolException expected");
        } catch (ToolException expected) {
            assertEquals("Not supported: weekly5d", expected.getMessage());
        }

    }
}
