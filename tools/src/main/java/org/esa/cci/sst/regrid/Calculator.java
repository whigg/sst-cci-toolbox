package org.esa.cci.sst.regrid;

/**
 * @author Bettina Scholze
 *         Date: 30.07.12 15:37
 */
public interface Calculator {

    double calculate(int targetCellIndex, double[] sourceData, int numberOfCellsToAggregateInEachDimension, int sourceWidth);
}