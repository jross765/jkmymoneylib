package org.kmymoney.api.read.impl.hlp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kmymoney.api.generated.KMYMONEYFILE;
import org.kmymoney.api.generated.SECURITY;
import org.kmymoney.api.read.KMyMoneySecurity;
import org.kmymoney.api.read.NoEntryFoundException;
import org.kmymoney.api.read.TooManyEntriesFoundException;
import org.kmymoney.api.read.impl.KMyMoneyFileImpl;
import org.kmymoney.api.read.impl.KMyMoneySecurityImpl;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.base.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSecurityManager {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FileSecurityManager.class);

	// ---------------------------------------------------------------

	protected KMyMoneyFileImpl kmmFile;

	private Map<KMMSecID, KMyMoneySecurity> secMap;
	private Map<String, KMMSecID>           symbMap;
	private Map<String, KMMSecID>           codeMap;

	// ---------------------------------------------------------------

	public FileSecurityManager(KMyMoneyFileImpl kmmFile) {
		this.kmmFile = kmmFile;
		init(kmmFile.getRootElement());
	}

	// ---------------------------------------------------------------

	private void init(final KMYMONEYFILE pRootElement) {
		secMap = new HashMap<KMMSecID, KMyMoneySecurity>();
		symbMap = new HashMap<String, KMMSecID>();
		codeMap = new HashMap<String, KMMSecID>();

		for ( SECURITY jwsdpSec : pRootElement.getSECURITIES().getSECURITY() ) {
			try {
				KMyMoneySecurityImpl sec = createSecurity(jwsdpSec);
				secMap.put(sec.getID(), sec);
				symbMap.put(sec.getSymbol(), new KMMSecID(jwsdpSec.getId()));
				codeMap.put(sec.getCode(), new KMMSecID(jwsdpSec.getId()));
			} catch (RuntimeException e) {
				LOGGER.error("init: [RuntimeException] Problem in " + getClass().getName() + ".init: "
						+ "ignoring illegal Security-Entry with id=" + jwsdpSec.getId(), e);
			}
		} // for

		LOGGER.debug("init: No. of entries in security map: " + secMap.size());
	}

	protected KMyMoneySecurityImpl createSecurity(final SECURITY jwsdpSec) {
		KMyMoneySecurityImpl sec = new KMyMoneySecurityImpl(jwsdpSec, kmmFile);
		LOGGER.debug("createSecurity: Generated new security: " + sec.getID());
		return sec;
	}

	// ---------------------------------------------------------------

	public void addSecurity(KMyMoneySecurity sec) {
		secMap.put(sec.getID(), sec);

		if ( sec.getSymbol() != null )
			symbMap.put(sec.getSymbol(), sec.getQualifID().getSecID());

		if ( sec.getCode() != null )
			codeMap.put(sec.getCode(), sec.getQualifID().getSecID());

		LOGGER.debug("addSecurity: Added security to cache: " + sec.getID());
	}

	public void removeSecurity(KMyMoneySecurity sec) {
		secMap.remove(sec.getID());

		for ( String symb : symbMap.keySet() ) {
			if ( symbMap.get(symb).equals(sec.getQualifID().getSecID()) )
				symbMap.remove(symb);
		}

		for ( String code : codeMap.keySet() ) {
			if ( codeMap.get(code).equals(sec.getQualifID().getSecID()) )
				codeMap.remove(code);
		}

		LOGGER.debug("removeSecurity: Removed security from cache: " + sec.getID());
	}

	// ---------------------------------------------------------------

	public KMyMoneySecurity getSecurityByID(final KMMSecID id) {
		if ( secMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		KMyMoneySecurity retval = secMap.get(id);
		if ( retval == null ) {
			LOGGER.warn("getSecurityById: No Security with ID '" + id + "'. We know " + secMap.size() + " securities.");
		}

		return retval;
	}

	public KMyMoneySecurity getSecurityByID(final String idStr) {
		if ( idStr == null ) {
			throw new IllegalStateException("null string given");
		}

		if ( idStr.trim().equals("") ) {
			throw new IllegalStateException("Search string is empty");
		}

		KMMSecID secID = new KMMSecID(idStr);
		return getSecurityByID(secID);
	}

	public KMyMoneySecurity getSecurityByQualifID(final KMMQualifSecID secID) {
		return getSecurityByID(secID.getCode());
	}

	public KMyMoneySecurity getSecurityByQualifID(final String qualifIDStr) {
		if ( qualifIDStr == null ) {
			throw new IllegalStateException("null string given");
		}

		if ( qualifIDStr.trim().equals("") ) {
			throw new IllegalStateException("Search string is empty");
		}

		KMMQualifSecID secID = KMMQualifSecID.parse(qualifIDStr);
		return getSecurityByQualifID(secID);
	}

	public KMyMoneySecurity getSecurityBySymbol(final String symb) {
		if ( secMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		if ( symbMap.size() != secMap.size() ) {
			// ::CHECK
			// CAUTION: Don't throw an exception, at least not in all cases,
			// because this is not necessarily an error: Only if the KMyMoney
			// file does not contain quotes for foreign currencies (i.e. currency-
			// commodities but only security-commodities is this an error.
			// throw new IllegalStateException("Sizes of root elements are not equal");
			LOGGER.debug("getSecurityBySymbol: Sizes of root elements are not equal.");
		}

		KMMSecID qualifID = symbMap.get(symb);
		if ( qualifID == null ) {
			LOGGER.warn("getSecurityBySymbol: No Security with symbol '" + symb + "'. We know " + symbMap.size()
					+ " securities in map 2.");
		}

		KMyMoneySecurity retval = secMap.get(qualifID);
		if ( retval == null ) {
			LOGGER.warn("getSecurityBySymbol: Security with qualified ID '" + qualifID + "'. We know " + secMap.size()
					+ " securities in map 1.");
		}

		return retval;
	}

	public KMyMoneySecurity getSecurityByCode(final String code) {
		if ( secMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		if ( codeMap.size() != secMap.size() ) {
			// ::CHECK
			// CAUTION: Don't throw an exception, at least not in all cases,
			// because this is not necessarily an error: Only if the KMyMoney
			// file does not contain quotes for foreign currencies (i.e. currency-
			// commodities but only security-commodities is this an error.
			// throw new IllegalStateException("Sizes of root elements are not equal");
			LOGGER.debug("getSecurityByCode: Sizes of root elements are not equal.");
		}

		KMMSecID qualifID = codeMap.get(code);
		if ( qualifID == null ) {
			LOGGER.warn("getSecurityByCode: No Security with symbol '" + code + "'. We know " + codeMap.size()
					+ " securities in map 2.");
		}

		KMyMoneySecurity retval = secMap.get(qualifID);
		if ( retval == null ) {
			LOGGER.warn("getSecurityByCode: No Security with qualified ID '" + qualifID + "'. We know " + secMap.size()
					+ " securities in map 1.");
		}

		return retval;
	}

	public List<KMyMoneySecurity> getSecuritiesByName(final String expr) {
		return getSecuritiesByName(expr, true);
	}

	public List<KMyMoneySecurity> getSecuritiesByName(final String expr, final boolean relaxed) {
		if ( secMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		List<KMyMoneySecurity> result = new ArrayList<KMyMoneySecurity>();

		for ( KMyMoneySecurity sec : getSecurities() ) {
			if ( sec.getName() != null ) // yes, that can actually happen!
			{
				if ( relaxed ) {
					if ( sec.getName().toLowerCase().contains(expr.trim().toLowerCase()) ) {
						result.add(sec);
					}
				} else {
					if ( sec.getName().equals(expr) ) {
						result.add(sec);
					}
				}
			}
		}

		result.sort(Comparator.naturalOrder()); 

		return result;
	}

	public KMyMoneySecurity getSecurityByNameUniq(final String expr)
			throws NoEntryFoundException, TooManyEntriesFoundException {
		List<KMyMoneySecurity> cmdtyList = getSecuritiesByName(expr, false);
		if ( cmdtyList.size() == 0 )
			throw new NoEntryFoundException();
		else if ( cmdtyList.size() > 1 )
			throw new TooManyEntriesFoundException();
		else
			return cmdtyList.get(0);
	}

	public Collection<KMyMoneySecurity> getSecurities() {
		if ( secMap == null ) {
			throw new IllegalStateException("no root-element loaded");
		}

		return Collections.unmodifiableCollection(secMap.values());
	}

	// ---------------------------------------------------------------

	public int getNofEntriesSecurityMap() {
		return secMap.size();
	}

}
