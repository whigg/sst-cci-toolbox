package org.esa.cci.sst.rules;

import org.esa.cci.sst.data.DescriptorBuilder;
import org.esa.cci.sst.data.VariableDescriptor;
import ucar.ma2.DataType;

/**
 * Converts times (Julian Date) into seconds since 1978-01-01 00:00:00.
 */
final class JulianDateToSeconds implements Rule {

    @Override
    public VariableDescriptor apply(VariableDescriptor sourceDescriptor) throws RuleException {
        Assert.type(DataType.DOUBLE, sourceDescriptor);
        Assert.unit("Julian Date", sourceDescriptor);

        final DescriptorBuilder builder = new DescriptorBuilder(sourceDescriptor);
        builder.setUnit("seconds since 1978-01-01 00:00:00");
        final Number sourceFillValue = sourceDescriptor.getFillValue();
        if (sourceFillValue != null) {
            builder.setFillValue(apply(sourceFillValue.doubleValue(), sourceDescriptor));
        }

        return builder.build();
    }

    @Override
    public Double apply(Number number, VariableDescriptor sourceDescriptor) throws RuleException {
        Assert.condition(number instanceof Double, "number instanceof Double");

        return (number.doubleValue() - 2443509.5) * 86400.0;
    }
}
