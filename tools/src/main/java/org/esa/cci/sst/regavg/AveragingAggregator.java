package org.esa.cci.sst.regavg;

import org.esa.cci.sst.common.AbstractAggregator;
import org.esa.cci.sst.common.AggregationContext;
import org.esa.cci.sst.common.AggregationFactory;
import org.esa.cci.sst.common.RegionMaskList;
import org.esa.cci.sst.common.RegionalAggregation;
import org.esa.cci.sst.common.SpatialResolution;
import org.esa.cci.sst.common.SstDepth;
import org.esa.cci.sst.common.TemporalResolution;
import org.esa.cci.sst.common.auxiliary.Climatology;
import org.esa.cci.sst.common.calculator.CoverageUncertaintyProvider;
import org.esa.cci.sst.common.cell.AggregationCell;
import org.esa.cci.sst.common.cell.CellAggregationCell;
import org.esa.cci.sst.common.cell.CellFactory;
import org.esa.cci.sst.common.cell.SpatialAggregationCell;
import org.esa.cci.sst.common.cellgrid.CellGrid;
import org.esa.cci.sst.common.cellgrid.Grid;
import org.esa.cci.sst.common.cellgrid.GridDef;
import org.esa.cci.sst.common.cellgrid.RegionMask;
import org.esa.cci.sst.common.file.FileStore;
import org.esa.cci.sst.common.file.FileType;
import org.esa.cci.sst.regavg.auxiliary.LUT1;
import org.esa.cci.sst.regavg.auxiliary.LUT2;
import org.esa.cci.sst.util.UTC;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Aggregator for the RegionalAveraging Tool.
 *
 * @author Norman Fomferra
 * @author Bettina Scholze
 * @author Ralf Quast
 */
public class AveragingAggregator extends AbstractAggregator {

    private static final Logger LOGGER = Logger.getLogger("org.esa.cci.sst");

    private final AggregationContext context;
    private final RegionMaskList regionMaskList;
    private RegionMask combinedRegionMask;
    private LUT1 lut1;
    private LUT2 lut2;

    public AveragingAggregator(RegionMaskList regionMaskList, FileStore fileStore, Climatology climatology, LUT1 lut1,
                               LUT2 lut2, SstDepth sstDepth) {
        super(fileStore, climatology, sstDepth);

        this.context = new AggregationContext();
        this.regionMaskList = regionMaskList;
        this.combinedRegionMask = RegionMask.combine(regionMaskList);
        this.lut1 = lut1;
        this.lut2 = lut2;
    }

    private static <CSource extends AggregationCell, CTarget extends CellAggregationCell> CellGrid<CTarget> aggregateCellGridToCoarserCellGrid(
            CellGrid<CSource> sourceGrid, CellGrid<CTarget> targetGrid, Grid seaCoverageGrid) {
        final GridDef sourceGridDef = sourceGrid.getGridDef();
        final GridDef targetGridDef = targetGrid.getGridDef();
        final int sourceW = sourceGridDef.getWidth();
        final int sourceH = sourceGridDef.getHeight();
        final int targetW = targetGridDef.getWidth();
        final int targetH = targetGridDef.getHeight();

        for (int sourceY = 0; sourceY < sourceH; sourceY++) {
            for (int sourceX = 0; sourceX < sourceW; sourceX++) {
                final CSource sourceCell = sourceGrid.getCell(sourceX, sourceY);
                if (sourceCell != null && !sourceCell.isEmpty()) {
                    final int targetX = (sourceX * targetW) / sourceW;
                    final int targetY = (sourceY * targetH) / sourceH;
                    final CTarget targetCell = targetGrid.getCellSafe(targetX, targetY);
                    final double seaCoverage = seaCoverageGrid.getSampleDouble(sourceX, sourceY);
                    //noinspection unchecked
                    targetCell.accumulate(sourceCell, seaCoverage);
                }
            }
        }
        return targetGrid;
    }

    @Override
    public List<AveragingTimeStep> aggregate(Date startDate, Date endDate, TemporalResolution temporalResolution)
            throws IOException {
        final List<AveragingTimeStep> results = new ArrayList<AveragingTimeStep>();
        final Calendar calendar = UTC.createCalendar(startDate);

        while (calendar.getTime().before(endDate) || calendar.getTime().equals(endDate)) {
            final Date date1 = calendar.getTime();
            final List<RegionalAggregation> result;

            switch (temporalResolution) {
                case daily: {
                    calendar.add(Calendar.DATE, 1);
                    final Date date2 = calendar.getTime();
                    result = aggregateRegions(date1, date2);
                    break;
                }
                case monthly: {
                    calendar.add(Calendar.MONTH, 1);
                    final Date date2 = calendar.getTime();
                    result = aggregateRegions(date1, date2);
                    break;
                }
                case seasonal: {
                    calendar.add(Calendar.MONTH, 3);
                    final Date date2 = calendar.getTime();
                    final List<AveragingTimeStep> monthlyTimeSteps = aggregate(date1, date2,
                                                                               TemporalResolution.monthly);
                    result = aggregateMonthlyTimeSteps(monthlyTimeSteps);
                    break;
                }
                case annual: {
                    calendar.add(Calendar.YEAR, 1);
                    final Date date2 = calendar.getTime();
                    final List<AveragingTimeStep> monthlyTimeSteps = aggregate(date1, date2,
                                                                               TemporalResolution.monthly);
                    result = aggregateMonthlyTimeSteps(monthlyTimeSteps);
                    break;
                }
                default:
                    throw new IllegalArgumentException(
                            String.format("Temporal resolution '%s' is not supported.", temporalResolution.toString()));
            }

            results.add(new AveragingTimeStep(date1, calendar.getTime(), result));
        }
        return results;
    }

    private List<RegionalAggregation> aggregateRegions(Date date1, Date date2) throws IOException {
        // compute the cell 5 grid for *all* combined regions first
        // re-grid spatially from 0.1/0.5 ° to 5 °, and aggregate given time range (<= monthly)
        final CellGrid<SpatialAggregationCell> cellGrid5 = aggregateTimeSteps(date1, date2);

        final CoverageUncertaintyProvider coverageUncertaintyProvider = createCoverageUncertaintyProvider(date1);
        context.setCoverageUncertaintyProvider(coverageUncertaintyProvider);

        return aggregateRegions(cellGrid5,
                                getClimatology().getSeaCoverageGrid5(),
                                getClimatology().getSeaCoverageGrid90(),
                                regionMaskList,
                                getFileType().getCellFactory90(context),
                                getFileType().getSameMonthAggregationFactory()
        );
    }

    private CellGrid<SpatialAggregationCell> aggregateTimeSteps(Date date1, Date date2) throws IOException {
        // TODO - check if time range is less or equal a month?
        final Climatology climatology = getClimatology();
        final FileType fileType = getFileType();
        final List<File> fileList = getFileStore().getFiles(date1, date2);

        LOGGER.info(String.format("Computing output time step from %s to %s, %d file(s) found.",
                                  UTC.getIsoFormat().format(date1), UTC.getIsoFormat().format(date2), fileList.size()));

        final CoverageUncertaintyProvider coverageUncertaintyProvider = createCoverageUncertaintyProvider(date1);
        context.setCoverageUncertaintyProvider(coverageUncertaintyProvider);
        final CellFactory<SpatialAggregationCell> targetCellFactory = fileType.getCellFactory5(context);
        final GridDef targetGridDef = GridDef.createGlobal(SpatialResolution.DEGREE_5_00.getResolution());
        final CellGrid<SpatialAggregationCell> targetGrid = CellGrid.create(targetGridDef, targetCellFactory);

        for (final File file : fileList) {
            LOGGER.info(String.format("Processing input %s file '%s'", getFileStore().getProductType(), file));

            final long t0 = System.currentTimeMillis();
            final NetcdfFile dataFile = NetcdfFile.open(file.getPath());

            try {
                final Date date = fileType.readDate(dataFile);
                final int dayOfYear = UTC.getDayOfYear(date);
                LOGGER.fine("Day of year is " + dayOfYear);
                context.setClimatologySstGrid(climatology.getSst(dayOfYear));
                context.setSeaCoverageGrid(climatology.getSeaCoverage());
                readSourceGrids(dataFile, context);
                LOGGER.fine("Aggregating grid(s)...");
                final long t1 = System.currentTimeMillis();

                aggregateSourcePixels(context, combinedRegionMask, targetGrid);

                LOGGER.fine(String.format("Aggregating grid(s) took %d ms", (System.currentTimeMillis() - t1)));
            } catch (IOException e) {
                LOGGER.warning(e.getMessage());
            } finally {
                dataFile.close();
            }
            LOGGER.fine(String.format("Processing input %s file took %d ms", getFileStore().getProductType(),
                                      System.currentTimeMillis() - t0));
        }

        return targetGrid;
    }


    private static List<RegionalAggregation> aggregateRegions(
            CellGrid<SpatialAggregationCell> sourceCellGrid5,
            Grid seaCoverageGrid5,
            Grid seaCoverageGrid90,
            RegionMaskList regionMaskList,
            CellFactory<CellAggregationCell<AggregationCell>> cellFactory90,
            AggregationFactory<SameMonthAggregation<AggregationCell>> aggregationFactory) {
        final List<RegionalAggregation> regionalAggregations = new ArrayList<RegionalAggregation>();

        for (final RegionMask regionMask : regionMaskList) {
            final CellGrid<? extends AggregationCell> regionCellGrid5 = getCellGridForRegion(sourceCellGrid5,
                                                                                             regionMask);
            // Check if region is Globe or Hemisphere, if so apply special averaging for all 90 deg grid boxes.
            final boolean mustAggregateTo90 = mustAggregateTo90(regionMask);
            final SameMonthAggregation aggregation = aggregationFactory.createAggregation();
            if (mustAggregateTo90) {
                final CellGrid<CellAggregationCell<AggregationCell>> grid90 = CellGrid.create(
                        GridDef.createGlobal(90.0), cellFactory90);
                // aggregateCell5GridToCell90Grid
                aggregateCellGridToCoarserCellGrid(regionCellGrid5, grid90, seaCoverageGrid5);
                // Removes spatial extent
                aggregateCellGrid(grid90, seaCoverageGrid90, aggregation);
            } else {
                // Removes spatial extent
                aggregateCellGrid(regionCellGrid5, seaCoverageGrid5, aggregation);
            }
            regionalAggregations.add(aggregation);
        }

        return regionalAggregations;
    }


    private static boolean mustAggregateTo90(RegionMask regionMask) {
        return regionMask.getCoverage() == RegionMask.Coverage.GLOBE
               || regionMask.getCoverage() == RegionMask.Coverage.N_HEMISPHERE
               || regionMask.getCoverage() == RegionMask.Coverage.S_HEMISPHERE;
    }

    private static <C extends AggregationCell> void aggregateCellGrid(CellGrid<C> cellGrid,
                                                                      Grid seaCoverageGrid,
                                                                      SameMonthAggregation aggregation) {
        final int w = cellGrid.getGridDef().getWidth();
        final int h = cellGrid.getGridDef().getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final C cell = cellGrid.getCell(x, y);
                if (cell != null && !cell.isEmpty()) {
                    // noinspection unchecked
                    aggregation.accumulate(cell, seaCoverageGrid.getSampleDouble(x, y));
                }
            }
        }
    }

    private static <C extends AggregationCell> CellGrid<C> getCellGridForRegion(CellGrid<C> sourceGrid,
                                                                                RegionMask regionMask) {
        final CellGrid<C> targetGrid = CellGrid.create(sourceGrid.getGridDef(), sourceGrid.getCellFactory());
        final int w = sourceGrid.getWidth();
        final int h = sourceGrid.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (regionMask.getSampleBoolean(x, y)) {
                    final C cell5 = sourceGrid.getCell(x, y);
                    if (cell5 != null && !cell5.isEmpty()) {
                        targetGrid.setCell(x, y, cell5);
                    }
                }
            }
        }
        return targetGrid;
    }

    private List<RegionalAggregation> aggregateMonthlyTimeSteps(List<AveragingTimeStep> monthlyTimeSteps) {
        return aggregateMonthlyTimeSteps(monthlyTimeSteps, regionMaskList.size(),
                                         getFileType().getMultiMonthAggregationFactory());
    }

    private static List<RegionalAggregation> aggregateMonthlyTimeSteps(List<AveragingTimeStep> monthlyTimeSteps,
                                                                       int regionCount,
                                                                       AggregationFactory<MultiMonthAggregation<RegionalAggregation>> aggregationFactory) {
        final MultiMonthAggregation aggregation = aggregationFactory.createAggregation();
        final ArrayList<RegionalAggregation> resultList = new ArrayList<RegionalAggregation>();

        for (int regionIndex = 0; regionIndex < regionCount; regionIndex++) {
            for (final AveragingTimeStep timeStep : monthlyTimeSteps) {
                RegionalAggregation sameMonthRegionalAggregation = timeStep.getRegionalAggregation(regionIndex);
                // noinspection unchecked
                aggregation.accumulate(sameMonthRegionalAggregation);
            }
            resultList.add(aggregation);
        }
        return resultList;
    }

    private CoverageUncertaintyProvider createCoverageUncertaintyProvider(Date date) {
        final int month = UTC.createCalendar(date).get(Calendar.MONTH);

        return new AveragingCoverageUncertaintyProvider(month) {
            @Override
            protected double getMagnitude5(int cellX, int cellY) {
                return lut1.getMagnitudeGrid5().getSampleDouble(cellX, cellY);
            }

            @Override
            protected double getExponent5(int cellX, int cellY) {
                return lut1.getExponentGrid5().getSampleDouble(cellX, cellY);
            }

            @Override
            protected double getMagnitude90(int cellX, int cellY, int month) {
                return lut2.getMagnitude90(month, cellX, cellY);
            }
        };
    }
}