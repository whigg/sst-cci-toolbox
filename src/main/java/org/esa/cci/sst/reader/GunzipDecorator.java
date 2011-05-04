package org.esa.cci.sst.reader;

import org.esa.cci.sst.data.DataFile;
import org.esa.cci.sst.data.Observation;
import org.esa.cci.sst.data.VariableDescriptor;
import org.postgis.PGgeometry;
import ucar.nc2.NetcdfFileWriteable;

import javax.naming.OperationNotSupportedException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.GZIPInputStream;

/**
 * A decorator for IO handlers that deflates a gzip-compressed input file into
 * a temporary directory but otherwise forwards IO operations to the decorated
 * handler.
 * <p/>
 * Compressed input file are recognized due to the ".gz" file extension. The
 * decorator gracefully handles non-compressed files by simply delegating to
 * the decorated IO handler.
 *
 * @author Martin Boettcher
 */
class GunzipDecorator implements IOHandler {

    private final IOHandler delegate;
    private File tmpFile;

    GunzipDecorator(IOHandler delegate) {
        this.delegate = delegate;
    }

    /**
     * Maybe deflates gz files, initialises reader.
     *
     * @param dataFile The file to be ingested.
     *
     * @throws IOException if decompressing or opening the file with the decorated reader fails.
     */
    @Override
    public final void init(DataFile dataFile) throws IOException {
        if (dataFile.getPath().endsWith(".gz")) {
            // deflate product to tmp file in tmp dir
            tmpFile = tmpFileFor(dataFile.getPath());
            decompress(new File(dataFile.getPath()), tmpFile);

            // temporarily read from tmp path
            final String origPath = dataFile.getPath();
            try {
                dataFile.setPath(tmpFile.getPath());
                delegate.init(dataFile);
            } finally {
                dataFile.setPath(origPath);
            }
        } else {
            tmpFile = null;
            delegate.init(dataFile);
        }
    }

    /**
     * Closes the product and deletes the tmp file.
     */
    @Override
    public final void close() {
        delegate.close();
        if (tmpFile != null && tmpFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            tmpFile.delete();
        }
        tmpFile = null;
    }

    /**
     * Delegates to decorated IO handler.
     */
    @Override
    public final int getNumRecords() {
        return delegate.getNumRecords();
    }

    /**
     * Delegates to decorated IO handler.
     */
    @Override
    public final Observation readObservation(int recordNo) throws IOException {
        return delegate.readObservation(recordNo);
    }

    /**
     * Delegates to decorated IO handler.
     */
    @Override
    public final VariableDescriptor[] getVariableDescriptors() throws IOException {
        return delegate.getVariableDescriptors();
    }

    /**
     * Delegates to decorated IO handler.
     */
    @Override
    public void write(NetcdfFileWriteable targetFile,
                      Observation sourceObservation,
                      String sourceVariableName,
                      String targetVariableName,
                      int targetRecordNumber,
                      PGgeometry refPoint,
                      Date refTime) throws IOException {
        delegate.write(targetFile, sourceObservation, sourceVariableName, targetVariableName, targetRecordNumber,
                       refPoint, refTime);
    }


    /**
     * Delegates to decorated IO handler.
     */
    @Override
    public final InsituRecord readInsituRecord(int recordNo) throws IOException, OperationNotSupportedException {
        return delegate.readInsituRecord(recordNo);
    }

    /**
     * Delegates to decorated IO handler.
     */
    @Override
    public final DataFile getDataFile() {
        return delegate.getDataFile();
    }

    /**
     * Constructs File with suffix of original file without "dotgz" in tmp dir.
     * The tmp dir can be configured with property java.io.tmpdir.
     * Else, it is a system default.
     *
     * @param dataFilePath path of the .gz file
     *
     * @return File in tmp dir
     *
     * @throws IOException if the tmp file could not be created
     */
    private static File tmpFileFor(String dataFilePath) throws IOException {
        // chop of path before filename and ".gz" suffix to determine filename
        final int slashPosition = dataFilePath.lastIndexOf(File.separator);
        final int dotGzPosition = dataFilePath.length() - ".gz".length();
        final String fileName = dataFilePath.substring(slashPosition + 1, dotGzPosition);
        // use filename without suffix as prefix and "." + suffix as suffix
        final int dotPosition = fileName.lastIndexOf('.');
        final String prefix = (dotPosition > -1) ? fileName.substring(0, dotPosition) : fileName;
        final String suffix = (dotPosition > -1) ? fileName.substring(dotPosition) : null;
        // create temporary file in tmp dir, either property java.io.tmpdir or system default
        return File.createTempFile(prefix, suffix);
    }

    /**
     * Uncompresses a file in gzip format to a tmp file.
     *
     * @param gzipFile existing file in gzip format
     * @param tmpFile  new file for the uncompressed content
     *
     * @throws IOException if reading the input, decompression, or writing the output fails
     */
    private static void decompress(File gzipFile, File tmpFile) throws IOException {
        final byte[] buffer = new byte[8192];

        InputStream in = null;
        OutputStream out = null;
        try {
            in = new GZIPInputStream(new FileInputStream(gzipFile), 8192);
            out = new BufferedOutputStream(new FileOutputStream(tmpFile));
            int noOfBytesRead;
            while ((noOfBytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, noOfBytesRead);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
