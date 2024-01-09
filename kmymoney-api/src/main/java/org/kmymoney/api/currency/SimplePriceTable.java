package org.kmymoney.api.currency;

import java.util.Collection;

import org.kmymoney.api.numbers.FixedPointNumber;

public interface SimplePriceTable {

    /**
     * @param code
     * @return
     */
    FixedPointNumber getConversionFactor(final String code);

    /**
     * @param code
     * @param factor
     */
    void setConversionFactor(final String code, final FixedPointNumber factor);

    // ---------------------------------------------------------------

    /**
     * @param value
     * @param code
     * @return
     */
    boolean convertFromBaseCurrency(FixedPointNumber value, final String code);

    /**
     * @param value
     * @param code
     * @return
     */
    boolean convertToBaseCurrency(FixedPointNumber value, final String code);

    // ---------------------------------------------------------------

    /**
     * @return
     */
    Collection<String> getCurrencies();

    /**
     * 
     */
    void clear();

}
