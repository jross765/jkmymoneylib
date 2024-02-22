package org.kmymoney.api.write;

import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Currency;

import org.kmymoney.base.basetypes.complex.KMMComplAcctID;
import org.kmymoney.base.basetypes.complex.KMMQualifCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.base.basetypes.complex.KMMQualifSecID;
import org.kmymoney.base.basetypes.simple.KMMInstID;
import org.kmymoney.base.basetypes.simple.KMMSecID;
import org.kmymoney.base.numbers.FixedPointNumber;
import org.kmymoney.api.read.KMyMoneyAccount;
import org.kmymoney.api.write.hlp.HasWritableUserDefinedAttributes;
import org.kmymoney.api.write.hlp.KMyMoneyWritableObject;


/**
 * Account that can be modified.<br/>
 * Supported properties for the propertyChangeListeners:
 * <ul>
 * <li>name</li>
 * <li>currencyID</li>
 * <li>currencyNameSpace</li>
 * <li>description</li>
 * <li>type</li>
 * <li>parentAccount</li>
 * <li>transactionSplits (not giving the old value of the list)</li>
 * </ul>
 */
public interface KMyMoneyWritableAccount extends KMyMoneyAccount, 
                                                 KMyMoneyWritableObject,
                                                 HasWritableUserDefinedAttributes
{

	/**
	 * @return the file we belong to
	 */
	KMyMoneyWritableFile getWritableKMyMoneyFile();

	/**
	 * Change the user-definable name. It should contain no newlines but may contain
	 * non-ascii and non-western characters.
	 *
	 * @param name the new name (not null)
	 */
	void setName(String name);

	// :.TODO
//	/**
//	 * Change the user-definable account-number. It should contain no newlines but
//	 * may contain non-ascii and non-western characters.
//	 *
//	 * @param code the new code (not null)
//	 */
//	void setInstitutionID(KMMInstID instID);

	/**
	 * @param desc the user-defined description (may contain multiple lines and
	 *             non-ascii-characters)
	 */
	void setMemo(String desc);

	/**
	 * Get the sum of all transaction-splits affecting this account in the given
	 * time-frame.
	 *
	 * @param from when to start, inclusive
	 * @param to   when to stop, exlusive.
	 * @return the sum of all transaction-splits affecting this account in the given
	 *         time-frame.
	 */
	FixedPointNumber getBalanceChange(LocalDate from, LocalDate to);

	/**
	 * Set the type of the account (income, ...).
	 *
	 * @param type the new type.
	 * @see {@link KMyMoneyAccount#getType()}
	 */
	void setType(KMyMoneyAccount.Type type);

	void setTypeInt(BigInteger typeInt);

	// ----------------------------

	void setQualifSecCurrID(final KMMQualifSecCurrID secCurrID);
	
//	void setQualifSecID(final KMMQualifSecID secID);
//	
//	void setQualifCurrID(final KMMQualifCurrID currID);
	
	void setSecID(final KMMSecID secID);
	
	void setCurrency(final Currency curr);

	void setCurrency(final String currCode);
	
	// ----------------------------

	/**
	 * @param newparent the new account or null to make it a top-level-account
	 */
	void setParentAccount(KMyMoneyAccount newparent);

	/**
	 * If the accountId is invalid, make this a top-level-account.
	 *
	 * @see {@link #setParentAccount(KMyMoneyAccount)}
	 */
	void setParentAccountID(KMMComplAcctID prntAcctID);

	/**
	 * Remove this account from the sytem.<br/>
	 * Throws IllegalStateException if this account has splits or childres.
	 */
	void remove();
	
	// ---------------------------------------------------------------

	/**
	 * Add a PropertyChangeListener to the listener list. The listener is registered
	 * for all properties.
	 *
	 * @param listener The PropertyChangeListener to be added
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Add a PropertyChangeListener for a specific property. The listener will be
	 * invoked only when a call on firePropertyChange names that specific property.
	 *
	 * @param propertyName The name of the property to listen on.
	 * @param listener     The PropertyChangeListener to be added
	 */
	void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

	/**
	 * Remove a PropertyChangeListener for a specific property.
	 *
	 * @param propertyName The name of the property that was listened on.
	 * @param listener     The PropertyChangeListener to be removed
	 */
	void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);

	/**
	 * Remove a PropertyChangeListener from the listener list. This removes a
	 * PropertyChangeListener that was registered for all properties.
	 *
	 * @param listener The PropertyChangeListener to be removed
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);

}
