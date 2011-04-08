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

package org.esa.cci.sst;

import org.esa.cci.sst.data.Coincidence;
import org.esa.cci.sst.data.DataFile;
import org.esa.cci.sst.data.DataSchema;
import org.esa.cci.sst.data.Matchup;
import org.esa.cci.sst.data.Observation;
import org.esa.cci.sst.orm.PersistenceManager;
import org.esa.cci.sst.reader.IOHandler;
import org.esa.cci.sst.reader.MmdReader;
import org.esa.cci.sst.util.DataUtil;
import org.esa.cci.sst.util.TimeUtil;
import ucar.ma2.Array;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MmsTool responsible for ingesting mmd files which have been processed by ARC3. Uses {@link IngestionTool} as delegate.
 *
 * @author Thomas Storm
 */
public class MmdIngestionTool extends MmsTool {

    public static void main(String[] args) throws ToolException {
        final MmdIngestionTool tool = new MmdIngestionTool();
        final boolean performWork = tool.setCommandLineArgs(args);
        if (!performWork) {
            return;
        }
        tool.init(args);
        tool.ingestDataFile();
        tool.ingestDataSchema();
        tool.ingestVariableDescriptors();
        tool.ingestObservations();
        tool.ingestCoincidences();
    }

     static final String DATAFILE_ALREADY_INGESTED = "SELECT COUNT (id) " +
                                                            "FROM mm_datafile " +
                                                            "WHERE path = %s";

     static final String DATASCHEMA_ALREADY_INGESTED = "SELECT COUNT (id) " +
                                                              "FROM mm_dataschema " +
                                                              "WHERE name = %s";

     static final String GET_OBSERVATION_ID = "SELECT o1.id " +
                                                     "FROM mm_observation o1, mm_observation oref, mm_matchup m " +
                                                     "WHERE m.id = %d " +
                                                     "AND m.refObs_id = oref.id " +
                                                     "AND o1.time >= oref.time - '12:00:00' and o1.time < oref.time + '12:00:00' " +
                                                     "AND st_intersects(o1.location, oref.point)";

     static final String GET_OBSERVATION = "SELECT o " +
                                                  "FROM Observation o " +
                                                  "WHERE o.id = %d";

     static final String GET_MATCHUP = "SELECT m " +
                                              "FROM Matchup m " +
                                              "WHERE m.id = %d";

    private IngestionTool delegate;
    private File mmdFile;
    private IOHandler ioHandler;

    private final DataSchema dataSchema = DataUtil.createDataSchema(Constants.DATA_SCHEMA_NAME_MMD, "ARC");
    private final Map<File, DataFile> dataFileMap = new HashMap<File, DataFile>();

    MmdIngestionTool() throws ToolException {
        super("mmdingest.sh", "0.1");
    }

    public void init(final String[] args) throws ToolException {
        delegate = new IngestionTool();
        delegate.setCommandLineArgs(args);
        delegate.initialize();
        ioHandler = new MmdReader(delegate.getPersistenceManager());
    }

    private void ingestDataFile() {
        final DataFile dataFile = getDataFile();
        ingestOnce(dataFile, DATAFILE_ALREADY_INGESTED);
    }

    private void ingestDataSchema() {
        ingestOnce(dataSchema, DATASCHEMA_ALREADY_INGESTED);
    }

    private void ingestVariableDescriptors() throws ToolException {
        try {
            delegate.persistVariableDescriptors("mmd", "ARC3", ioHandler);
        } catch (IOException e) {
            throw new ToolException("Unable to persist variable descriptors for sensor 'ARC'.", e,
                                    ToolException.TOOL_ERROR);
        }
    }

    private void ingestObservations() throws ToolException {
        final DataFile dataFile = getDataFile();
        initIOHandler(ioHandler, dataFile);
        final int numRecords = ioHandler.getNumRecords();
        for (int i = 0; i < numRecords; i++) {
            getLogger().info(String.format("ingestion of record '%d/%d\'", (i + 1), numRecords));
            persistObservation(ioHandler, i);
        }
    }

    private void ingestCoincidences() throws ToolException {
        final int[] matchupIds = getMatchupIds();
        for (int matchupId : matchupIds) {
            ingestCoincidences(matchupId);
        }
    }

    private void ingestCoincidences(final int matchupId) {
        final List<Observation> observations = getObservations(matchupId);
        final Matchup matchup = (Matchup) getDatabaseObjectById(GET_MATCHUP, matchupId);
        for (Observation observation : observations) {
            final Coincidence coincidence = createCoincidence(matchup, observation);
            delegate.getPersistenceManager().persist(coincidence);
        }
    }

    private Coincidence createCoincidence(final Matchup matchup, final Observation observation) {
        final Coincidence coincidence = new Coincidence();
        coincidence.setMatchup(matchup);
        coincidence.setObservation(observation);
        setCoincidenceTimeDelta(matchup, observation, coincidence);
        return coincidence;
    }

    private void setCoincidenceTimeDelta(final Matchup matchup, final Observation observation,
                                         final Coincidence coincidence) {
        final int timeDelta = TimeUtil.computeTimeDelta(matchup, observation);
        coincidence.setTimeDifference(timeDelta);
    }

    @SuppressWarnings({"unchecked"})
    List<Observation> getObservations(final int matchupId) {
        final String queryString = String.format(GET_OBSERVATION_ID, matchupId);
        final Query query = delegate.getPersistenceManager().createNativeQuery(queryString);
        return getObservationsForIds((List<Integer>) query.getResultList());
    }

    private List<Observation> getObservationsForIds(final List<Integer> observationIds) {
        List<Observation> observations = new ArrayList<Observation>();
        for (int observationId : observationIds) {
            final Observation observation = (Observation) getDatabaseObjectById(GET_OBSERVATION, observationId);
            observations.add(observation);
        }
        return observations;
    }

    Object getDatabaseObjectById(final String baseQuery, final int id) {
        final String queryString = String.format(baseQuery, id);
        final Query query = delegate.getPersistenceManager().createQuery(queryString);
        return query.getSingleResult();
    }

    private int[] getMatchupIds() throws ToolException {
        final String location = validateMmdFile();
        final NetcdfFile mmdFile = openMmdFile(location);
        final String varNameMatchupEscaped = NetcdfFile.escapeName(MmdReader.VARIABLE_NAME_MATCHUP);
        final Variable matchupVariable = mmdFile.findVariable(varNameMatchupEscaped);
        return getMatchupIds(matchupVariable);
    }

    private int[] getMatchupIds(final Variable matchupVariable) throws ToolException {
        final Dimension matchupDimension = matchupVariable.getDimension(0);
        final int[] shape = {matchupDimension.getLength()};
        final int[] origin = {0};
        return readMatchupIdsFromFile(matchupVariable, origin, shape);
    }

    int[] readMatchupIdsFromFile(final Variable matchupVariable, final int[] origin,
                                 final int[] shape) throws ToolException {
        final Array matchupIds;
        try {
            matchupIds = matchupVariable.read(origin, shape);
        } catch (Exception e) {
            throw new ToolException(
                    MessageFormat.format("Unable to read from variable ''{0}''.", MmdReader.VARIABLE_NAME_MATCHUP), e,
                    ToolException.TOOL_ERROR);
        }
        return toIntArray(matchupIds);
    }

    private int[] toIntArray(final Array array) {
        int[] result = new int[(int) array.getSize()];
        for (int i = 0; i < array.getSize(); i++) {
            result[i] = array.getInt(i);
        }
        return result;
    }

    private NetcdfFile openMmdFile(final String location) throws ToolException {
        final NetcdfFile mmdFile;
        try {
            mmdFile = NetcdfFile.open(location);
        } catch (IOException e) {
            throw new ToolException(MessageFormat.format("Cannot open mmd file ''{0}''.", location), e,
                                    ToolException.TOOL_ERROR);
        }
        return mmdFile;
    }

    private String validateMmdFile() throws ToolException {
        final String location = getMmdFile().getAbsolutePath();
        try {
            NetcdfFile.canOpen(location);
        } catch (IOException e) {
            throw new ToolException(MessageFormat.format("Cannot open mmd file ''{0}''.", location), e,
                                    ToolException.TOOL_ERROR);
        }
        return location;
    }

    private void ingestOnce(final Object data, String queryString) {
        final PersistenceManager persistenceManager = delegate.getPersistenceManager();
        final Query query = persistenceManager.createNativeQuery(queryString, Integer.class);
        int result = (Integer) query.getSingleResult();
        if (result == 0) {
            persistenceManager.persist(data);
        } else {
            getLogger().info(
                    MessageFormat.format("Data of type ''{0}'' already ingested.", data.getClass().getSimpleName()));
        }
    }

    private DataFile getDataFile() {
        final File mmdFile = getMmdFile();
        return getDataFile(mmdFile);
    }

    private DataFile getDataFile(final File file) {
        if (dataFileMap.get(file) == null) {
            final DataFile dataFile = DataUtil.createDataFile(file, dataSchema);
            dataFileMap.put(file, dataFile);
            return dataFile;
        }
        return dataFileMap.get(file);
    }

    private File getMmdFile() {
        final String filename = getConfiguration().getProperty("mms.test.arc3.output.filename", "mmd.nc");
        if (mmdFile == null) {
            mmdFile = new File(filename);
        }
        return mmdFile;
    }

    private void persistObservation(final IOHandler ioHandler, int recordNo) throws ToolException {
        final PersistenceManager persistenceManager = delegate.getPersistenceManager();
        persistenceManager.transaction();
        try {
            delegate.persistObservation(ioHandler, recordNo);
        } catch (Exception e) {
            getErrorHandler().handleError(e, MessageFormat.format("Error persisting observation ''{0}''.", recordNo),
                                          ToolException.TOOL_ERROR);
        } finally {
            persistenceManager.commit();
        }
    }

    private void initIOHandler(final IOHandler ioHandler, final DataFile dataFile) throws ToolException {
        try {
            ioHandler.init(dataFile);
        } catch (IOException e) {
            getErrorHandler().handleError(e, "Error initializing IOHandler for mmd file.", ToolException.TOOL_ERROR);
        }
    }
}
