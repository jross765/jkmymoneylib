package org.kmymoney.api.read.impl.hlp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kmymoney.api.Const;
import org.kmymoney.api.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.api.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.api.basetypes.complex.KMMPricePairID;
import org.kmymoney.api.basetypes.complex.KMMPriceID;
import org.kmymoney.api.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.api.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.api.basetypes.complex.KMMQualifSecID;
import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.generated.PRICE;
import org.kmymoney.api.generated.PRICEPAIR;
import org.kmymoney.api.generated.PRICES;
import org.kmymoney.api.numbers.FixedPointNumber;
import org.kmymoney.api.read.KMyMoneyFile;
import org.kmymoney.api.read.KMyMoneyPrice;
import org.kmymoney.api.read.KMyMoneyPricePair;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneyPriceImpl;
import org.kmymoney.api.read.impl.KMyMoneyPricePairImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilePriceManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FilePriceManager.class);

	public static final DateFormat PRICE_QUOTE_DATE_FORMAT = new SimpleDateFormat(Const.STANDARD_DATE_FORMAT);

	private static final int RECURS_DEPTH_MAX = 5; // ::MAGIC

	// ---------------------------------------------------------------

	protected KMyMoneyFileImpl kmmFile;

	private PRICES                              priceDB  = null;
	private Map<KMMPricePairID, KMyMoneyPricePair> prcPrMap = null;
	private Map<KMMPriceID, KMyMoneyPrice>      prcMap   = null;

	// ---------------------------------------------------------------

	public FilePriceManager(KMyMoneyFileImpl kmmFile) {
		this.kmmFile = kmmFile;
		init(kmmFile.getRootElement());
	}

	// ---------------------------------------------------------------

	private void init(final KMYMONEYFILE pRootElement) {
		prcPrMap = new HashMap<KMMPricePairID, KMyMoneyPricePair>();
		prcMap   = new HashMap<KMMPriceID, KMyMoneyPrice>();

		initPriceDB(pRootElement);
		List<PRICEPAIR> prices = priceDB.getPRICEPAIR();
		for ( PRICEPAIR jwsdpPrcPr : prices ) {
			String fromCurr = jwsdpPrcPr.getFrom();
			String toCurr = jwsdpPrcPr.getTo();
			KMMPricePairID currPair = new KMMPricePairID(fromCurr, toCurr);
			KMyMoneyPricePair pricePair = createPricePair(jwsdpPrcPr);
			prcPrMap.put(currPair, pricePair);
			for ( PRICE jwsdpPrc : jwsdpPrcPr.getPRICE() ) {
				XMLGregorianCalendar cal = jwsdpPrc.getDate();
				if ( cal != null ) {
					LocalDate date = LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
					String dateStr = date.toString();
					KMMPriceID priceID = new KMMPriceID(fromCurr, toCurr, dateStr);
					KMyMoneyPriceImpl price = createPrice(pricePair, jwsdpPrc);
					prcMap.put(priceID, price);
				} else {
					LOGGER.error("init: Found Price without or with invalid date: (" + fromCurr + "/" + toCurr + ")");
				}
			}
		}

		LOGGER.debug("init: No. of entries in price pair map: " + prcPrMap.size());
		LOGGER.debug("init: No. of entries in price map: " + prcMap.size());
	}

	private void initPriceDB(final KMYMONEYFILE pRootElement) {
		priceDB = pRootElement.getPRICES();
	}

	protected KMyMoneyPricePairImpl createPricePair(final PRICEPAIR jwsdpPrcPr) {
		KMyMoneyPricePairImpl prcPr = new KMyMoneyPricePairImpl(jwsdpPrcPr, kmmFile);
		LOGGER.debug("createPricePair: Generated new price pair: " + prcPr.getID());
		return prcPr;
	}

	protected KMyMoneyPriceImpl createPrice(final KMyMoneyPricePair prcPr, final PRICE jwsdpPrc) {
		KMyMoneyPriceImpl prc = new KMyMoneyPriceImpl(prcPr, jwsdpPrc, kmmFile);
		LOGGER.info("createPrice: Generated new price: " + prc.getID());
		return prc;
	}

	// ---------------------------------------------------------------

	public void addPricePair(KMyMoneyPricePair prcPr) {
		addPricePair(prcPr, true);
	}

	public void addPricePair(KMyMoneyPricePair prcPr, boolean withPrc) {
		prcPrMap.put(prcPr.getID(), prcPr);

		if ( withPrc ) {
			for ( KMyMoneyPrice splt : prcPr.getPrices() ) {
				addPrice(splt, false);
			}
		}

		LOGGER.debug("addPricePair: Added price pair to cache: " + prcPr.getID());
	}

	public void removePricePair(KMyMoneyPricePair prcPr) {
		removePricePair(prcPr, true);
	}

	public void removePricePair(KMyMoneyPricePair prcPr, boolean withPrc) {
		if ( withPrc ) {
			for ( KMyMoneyPrice splt : prcPr.getPrices() ) {
				removePrice(splt, false);
			}
		}

		prcPrMap.remove(prcPr.getID());

		LOGGER.debug("removePricePair: Removed price pair from cache: " + prcPr.getID());
	}

	// ---------------------------------------------------------------

	public void addPrice(KMyMoneyPrice prc) {
		addPrice(prc, true);
	}

	public void addPrice(KMyMoneyPrice prc, boolean withPrcPr) {
		prcMap.put(prc.getID(), prc);
		LOGGER.debug("addPrice: Added price to cache: " + prc.getID());

		if ( withPrcPr ) {
			addPricePair(prc.getParentPricePair(), false);
		}
	}

	public void removePrice(KMyMoneyPrice prc) {
		removePrice(prc, true);
	}

	public void removePrice(KMyMoneyPrice prc, boolean withPrcPr) {
		if ( withPrcPr ) {
			removePricePair(prc.getParentPricePair(), false);
		}

		prcMap.remove(prc.getID());
		LOGGER.debug("removePrice: Removed price from cache: " + prc.getID());
	}

	// ---------------------------------------------------------------

	public PRICES getPriceDB() {
		return priceDB;
	}
	
	// ----------------------------

	public KMyMoneyPricePair getPricePairByID(KMMPricePairID prcPrID) {
		if ( prcPrMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}
		
		// CAUTION: The following line does not work reliably in all relevant cases.
		// That has to do with the intricacies of the definition of KMMCurrPair
		// (cf. test cases for KMMCurrPair):
		// return prcPrMap.get(prcPrID);
		
		// Instead:
		for ( KMMPricePairID elt : prcPrMap.keySet() ) {
			if ( elt.toString().equals(prcPrID.toString()) ) { // <-- important: toString()
				return prcPrMap.get(elt);
			}
		}
		
		return null;
	}

	public Collection<KMyMoneyPricePair> getPricePairs() {
		if ( prcPrMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		return prcPrMap.values();
	}

	// ----------------------------

	public KMyMoneyPrice getPriceByID(KMMPriceID prcID) {
		if ( prcMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		return prcMap.get(prcID);
	}

	public Collection<KMyMoneyPrice> getPrices() {
		if ( prcMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		return prcMap.values();
	}

	public FixedPointNumber getLatestPrice(final String secCurrIDStr)
			throws InvalidQualifSecCurrIDException, InvalidQualifSecCurrTypeException {
		if ( secCurrIDStr.startsWith("E0") ) { // ::MAGIC
			return getLatestPrice(new KMMQualifSecID(secCurrIDStr));
		} else {
			return getLatestPrice(new KMMQualifCurrID(secCurrIDStr));
		}
	}

	public FixedPointNumber getLatestPrice(final KMMQualifSecCurrID secCurrID)
			throws InvalidQualifSecCurrIDException, InvalidQualifSecCurrTypeException {
		return getLatestPrice(secCurrID, 0);
	}

	// ----------------------------

	/**
	 * @param pCmdtySpace the name space for pCmdtyId
	 * @param pCmdtyId    the currency-name
	 * @param depth       used for recursion. Always call with '0' for aborting
	 *                    recursive quotes (quotes to other then the base- currency)
	 *                    we abort if the depth reached 6.
	 * @return the latest price-quote in the kmymoney-file in the default-currency
	 * @throws InvalidQualifSecCurrTypeException
	 * @throws InvalidQualifSecCurrIDException
	 * @see {@link KMyMoneyFile#getLatestPrice(String, String)}
	 * @see #getDefaultCurrencyID()
	 */
	private FixedPointNumber getLatestPrice(final KMMQualifSecCurrID secCurrID, final int depth)
			throws InvalidQualifSecCurrIDException, InvalidQualifSecCurrTypeException {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("null parameter 'secCurrID' given");
		}
		// System.err.println("depth: " + depth);

		LocalDate latestDate = null;
		FixedPointNumber latestQuote = null;
		FixedPointNumber factor = new FixedPointNumber(1); // factor is used if the quote is not to our base-currency
		final int maxRecursionDepth = RECURS_DEPTH_MAX;

		for ( KMyMoneyPrice prc : prcMap.values() ) {
			KMMQualifSecCurrID fromSecCurr = prc.getParentPricePairID().getFromSecCurr();
			KMMQualifCurrID toCurr = prc.getParentPricePairID().getToCurr();

			if ( fromSecCurr == null ) {
				LOGGER.warn("getLatestPrice: KMyMoney-file contains price-quotes without from-currency: '"
						+ prc.toString() + "'");
				continue;
			}

			if ( toCurr == null ) {
				LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without to-currency: '"
						+ prc.toString() + "'");
				continue;
			}

			try {
				if ( prc.getDate() == null ) {
					LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without date: " + "'"
							+ prc.toString() + "'");
					continue;
				}

				if ( prc.getValue() == null ) {
					LOGGER.warn("getLatestPrice: KMyMoney file contains price-quotes without price value: " + "'"
							+ prc.toString() + "'");
					continue;
				}

				if ( !fromSecCurr.getCode().equals(secCurrID.getCode()) ) {
					continue;
				}

				// BEGIN core
				if ( !toCurr.getCode().equals(kmmFile.getDefaultCurrencyID()) ) {
					if ( depth > maxRecursionDepth ) {
						LOGGER.warn("getLatestPrice: Ignoring price-quote that is not in "
								+ kmmFile.getDefaultCurrencyID() + " but in '" + toCurr + "'");
						continue;
					}
					factor = getLatestPrice(new KMMQualifCurrID(toCurr), depth + 1);
				}
				// END core

				LocalDate date = prc.getDate();

				if ( latestDate == null || latestDate.isBefore(date) ) {
					latestDate = date;
					latestQuote = prc.getValue();
					LOGGER.debug("getLatestPrice: pSecCurrId='" + secCurrID.toString() + "' converted " + latestQuote
							+ " <= " + prc.getValue());
				}

			} catch (NumberFormatException e) {
				LOGGER.error("getLatestPrice: [NumberFormatException] Problem in " + getClass().getName()
						+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
						+ "(" + prc.toString() + ")", e);
			} catch (NullPointerException e) {
				LOGGER.error("getLatestPrice: [NullPointerException] Problem in " + getClass().getName()
						+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
						+ "(" + prc.toString() + ")", e);
			} catch (ArithmeticException e) {
				LOGGER.error("getLatestPrice: [ArithmeticException] Problem in " + getClass().getName()
						+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
						+ "(" + prc.toString() + ")", e);
			}
		} // for

		LOGGER.debug("getLatestPrice: pSecCurrId='" + secCurrID.toString() + "'= " + latestQuote + " from " + latestDate);

		if ( latestQuote == null ) {
			return null;
		}

		if ( factor == null ) {
			factor = new FixedPointNumber(1);
		}

		return factor.multiply(latestQuote);
	}

	private FixedPointNumber getLatestPrice_readAfresh(final KMMQualifSecCurrID secCurrID, final int depth)
			throws InvalidQualifSecCurrIDException, InvalidQualifSecCurrTypeException {
		if ( secCurrID == null ) {
			throw new IllegalArgumentException("null parameter 'secCurrID' given");
		}
		// System.err.println("depth: " + depth);

		LocalDate latestDate = null;
		FixedPointNumber latestQuote = null;
		FixedPointNumber factor = new FixedPointNumber(1); // factor is used if the quote is not to our base-currency
		final int maxRecursionDepth = RECURS_DEPTH_MAX;

		for ( PRICEPAIR pricePair : priceDB.getPRICEPAIR() ) {
			String fromSecCurr = pricePair.getFrom();
			String toCurr = pricePair.getTo();
			// System.err.println("pricepair: " + fromCurr + " -> " + toCurr);

			if ( fromSecCurr == null ) {
				LOGGER.warn("getLatestPrice_readAfresh: KMyMoney-file contains price-quotes without from-currency: '"
						+ pricePair.toString() + "'");
				continue;
			}

			if ( toCurr == null ) {
				LOGGER.warn("getLatestPrice_readAfresh: KMyMoney file contains price-quotes without to-currency: '"
						+ pricePair.toString() + "'");
				continue;
			}

			for ( PRICE prc : pricePair.getPRICE() ) {
				if ( prc == null ) {
					LOGGER.warn(
							"getLatestPrice_readAfresh: KMyMoney file contains null price-quotes - there may be a problem with JWSDP");
					continue;
				}

				try {
					if ( prc.getDate() == null ) {
						LOGGER.warn("getLatestPrice_readAfresh: KMyMoney file contains price-quotes without date: "
								+ "(" + pricePair.getFrom() + "/" + pricePair.getTo() + ")");
						continue;
					}

					if ( prc.getPrice() == null ) {
						LOGGER.warn(
								"getLatestPrice_readAfresh: KMyMoney file contains price-quotes without price value: "
										+ "(" + pricePair.getFrom() + "/" + pricePair.getTo() + ")");
						continue;
					}

					if ( !fromSecCurr.equals(secCurrID.getCode()) ) {
						continue;
					}

					// BEGIN core
					if ( toCurr.startsWith("E0") ) {
						// is security
						if ( depth > maxRecursionDepth ) {
							LOGGER.warn(
									"getLatestPrice_readAfresh: Ignoring price-quote that is not in an ISO4217-currency"
											+ " but in '" + toCurr + "'");
							continue;
						}
						factor = getLatestPrice(new KMMQualifSecID(toCurr), depth + 1);
					} else {
						// is currency
						if ( !toCurr.equals(kmmFile.getDefaultCurrencyID()) ) {
							if ( depth > maxRecursionDepth ) {
								LOGGER.warn("getLatestPrice_readAfresh: Ignoring price-quote that is not in "
										+ kmmFile.getDefaultCurrencyID() + " but in '" + toCurr + "'");
								continue;
							}
							factor = getLatestPrice(new KMMQualifCurrID(toCurr), depth + 1);
						}
					}
					// END core

					XMLGregorianCalendar dateCal = prc.getDate();
					LocalDate date = LocalDate.of(dateCal.getYear(), dateCal.getMonth(), dateCal.getDay());

					if ( latestDate == null || latestDate.isBefore(date) ) {
						latestDate = date;
						latestQuote = new FixedPointNumber(prc.getPrice());
						LOGGER.debug("getLatestPrice_readAfresh: pSecCurrId='" + secCurrID.toString() + "' converted "
								+ latestQuote + " <= " + prc.getPrice());
					}

				} catch (NumberFormatException e) {
					LOGGER.error("getLatestPrice_readAfresh: [NumberFormatException] Problem in " + getClass().getName()
							+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
							+ "(" + pricePair.getFrom() + "/" + pricePair.getTo() + ")", e);
				} catch (NullPointerException e) {
					LOGGER.error("getLatestPrice_readAfresh: [NullPointerException] Problem in " + getClass().getName()
							+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
							+ "(" + pricePair.getFrom() + "/" + pricePair.getTo() + ")", e);
				} catch (ArithmeticException e) {
					LOGGER.error("getLatestPrice_readAfresh: [ArithmeticException] Problem in " + getClass().getName()
							+ ".getLatestPrice(pSecCurrId='" + secCurrID.toString() + "')! Ignoring a bad price-quote '"
							+ "(" + pricePair.getFrom() + "/" + pricePair.getTo() + ")", e);
				}
			} // for price
		} // for pricepair

		LOGGER.debug("getLatestPrice_readAfresh: pSecCurrId='" + secCurrID.toString() + "'= " + latestQuote + " from "
				+ latestDate);

		if ( latestQuote == null ) {
			return null;
		}

		if ( factor == null ) {
			factor = new FixedPointNumber(1);
		}

		return factor.multiply(latestQuote);
	}

	// ---------------------------------------------------------------

	public int getNofEntriesPricePairMap() {
		return prcPrMap.size();
	}

	public int getNofEntriesPriceMap() {
		return prcMap.size();
	}

}
