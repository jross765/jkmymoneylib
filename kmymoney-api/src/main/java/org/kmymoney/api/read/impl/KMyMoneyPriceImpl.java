package org.kmymoney.api.read.impl;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Currency;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.Const;
import org.kmymoney.api.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.api.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.api.basetypes.complex.KMMCurrPair;
import org.kmymoney.api.basetypes.complex.KMMPriceID;
import org.kmymoney.api.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.api.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.api.basetypes.complex.KMMQualifSecID;
import org.kmymoney.api.numbers.FixedPointNumber;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.KMyMoneyCurrency;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.generated.PRICE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KMyMoneyPriceImpl implements KMyMoneyPrice {

    private static final Logger LOGGER = LoggerFactory.getLogger(KMyMoneyPriceImpl.class);

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(Const.STANDARD_DATE_FORMAT);
    
    // -----------------------------------------------------------

    private final PRICE jwsdpPeer;

    private final KMyMoneyFile file;

    // -----------------------------------------------------------
    
    private KMyMoneyPricePair parent = null;

    /**
     * The currency-format to use for formatting.<br/>
     */
    private NumberFormat currencyFormat = null;

    // -----------------------------------------------------------

    /**
     * @param newPeer the JWSDP-object we are wrapping.
     */
    @SuppressWarnings("exports")
    public KMyMoneyPriceImpl(final KMyMoneyPricePair parent, final PRICE newPeer, final KMyMoneyFile file) {
	super();
		
	this.parent    = parent;
	this.jwsdpPeer = newPeer;
	this.file      = file;
    }

    // -----------------------------------------------------------
    
    @Override
    public KMMPriceID getID() throws InvalidQualifSecCurrIDException, InvalidQualifSecCurrTypeException {
	return new KMMPriceID(parent.getFromSecCurrStr(),
		              parent.getToCurrStr(),
		              DATE_FORMAT.format(getDate()));
    }

    @Override
    public KMMCurrPair getParentPricePairID() throws InvalidQualifSecCurrIDException, InvalidQualifSecCurrTypeException {
	return parent.getID();
    }

    @Override
    public KMyMoneyPricePair getParentPricePair() {
	return parent;
    }

    // ---------------------------------------------------------------
    
    @Override
    public String getFromSecCurrStr() {
	return parent.getFromSecCurrStr();
    }

    @Override
    public String getToCurrStr() {
	return parent.getToCurrStr();
    }
    
    // ----------------------------

    @Override
    public KMMQualifSecCurrID getFromSecCurrQualifID() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException {
	return parent.getFromSecCurrQualifID();
    }

    @Override
    public KMMQualifSecID getFromSecurityQualifID() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException {
	KMMQualifSecCurrID secCurrID = getFromSecCurrQualifID();
	if ( secCurrID.getType() != KMMQualifSecCurrID.Type.SECURITY )
	    throw new InvalidQualifSecCurrTypeException();
	    
	return new KMMQualifSecID(secCurrID);
    }

    @Override
    public KMMQualifCurrID getFromCurrencyQualifID() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException {
	KMMQualifSecCurrID secCurrID = getFromSecCurrQualifID();
	if ( secCurrID.getType() != KMMQualifSecCurrID.Type.CURRENCY )
	    throw new InvalidQualifSecCurrTypeException();

	return new KMMQualifCurrID(secCurrID);
    }

    @Override
    public KMyMoneySecurity getFromSecurity() throws InvalidQualifSecCurrIDException, InvalidQualifSecCurrTypeException {
	return parent.getFromSecurity();
    }
    
    @Override
    public String getFromCurrencyCode() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException {
	return getFromCurrencyQualifID().getCurrency().getCurrencyCode();
    }

    @Override
    public KMyMoneyCurrency getFromCurrency() throws InvalidQualifSecCurrIDException, InvalidQualifSecCurrTypeException {
	return parent.getFromCurrency();
    }
    
    // ----------------------------
    
    @Override
    public KMMQualifCurrID getToCurrencyQualifID() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException {
	return parent.getToCurrencyQualifID();
    }

    @Override
    public String getToCurrencyCode() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException {
	return getToCurrencyQualifID().getCode();
    }

    @Override
    public KMyMoneyCurrency getToCurrency() throws InvalidQualifSecCurrIDException, InvalidQualifSecCurrTypeException {
	return parent.getToCurrency();
    }

    // ---------------------------------------------------------------
    
    /**
     * @return The currency-format to use for formatting.
     * @throws InvalidQualifSecCurrTypeException 
     * @throws InvalidQualifSecCurrIDException 
     */
    private NumberFormat getCurrencyFormat() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException {
	if (currencyFormat == null) {
	    currencyFormat = NumberFormat.getCurrencyInstance();
	}

//	// the currency may have changed
//	if ( ! getCurrencyQualifID().getType().equals(SecurityCurrID.Type.CURRENCY) )
//	    throw new InvalidSecCurrTypeException();
	    
	Currency currency = Currency.getInstance(getToCurrencyCode());
	currencyFormat.setCurrency(currency);

	return currencyFormat;
    }

    @Override
    public LocalDate getDate() {
	if ( jwsdpPeer.getDate() == null )
	    return null;
	
	XMLGregorianCalendar cal = jwsdpPeer.getDate();
	try {
	    return LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
	} catch (Exception e) {
	    IllegalStateException ex = new IllegalStateException("unparsable date '" + cal + "' in price!");
	    ex.initCause(e);
	    throw ex;
	}
    }

    @Override
    public String getSource() {
	if ( jwsdpPeer.getSource() == null )
	    return null;
	
	return jwsdpPeer.getSource();
    }

    @Override
    public FixedPointNumber getValue() {
	if ( jwsdpPeer.getPrice() == null )
	    return null;
	
	return new FixedPointNumber(jwsdpPeer.getPrice());
    }

    @Override
    public String getValueFormatted() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException {
	return getCurrencyFormat().format(getValue());
    }

    // ---------------------------------------------------------------
    
    @Override
    public String toString() {
	String result = "KMMPriceImpl [";
	
	try {
	    result += "id='" + getID() + "'";
	} catch (Exception e) {
	    result += "id=" + "ERROR";
	}
	
	try {
	    result += ", from-sec-curr-qualif-id='" + getFromSecCurrQualifID() + "'";
	} catch (Exception e) {
	    result += ", from-sec-curr-qualif-id=" + "ERROR";
	}
	
	try {
	    result += ", to-curr-qualif-id='" + getToCurrencyQualifID() + "'";
	} catch (Exception e) {
	    result += ", to-curr-qualif-id=" + "ERROR";
	}
	
	result += ", date=" + getDate(); 
	result += ", source='" + getSource() + "'"; 
	
	try {
	    result += ", value=" + getValueFormatted() + "]";
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    result += ", value=" + "ERROR" + "]";
	}
	
	return result;
    }

}
