package org.esa.cci.sst.regrid.filetypes;

import org.esa.cci.sst.regrid.FileType;
import org.esa.cci.sst.util.ArrayGrid;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ucar.nc2.NetcdfFile;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * @author Bettina Scholze
 *         Date: 23.07.12 14:14
 */
public class NotSupportedFileType implements FileType {

    @Override
    public Map<String, ArrayGrid> readSourceGrids(NetcdfFile file) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public Date parseDate(File file) throws ParseException {
        throw new NotImplementedException();
    }
}