package org.kmymoney.api.read;

import java.util.Locale;

import org.kmymoney.api.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.api.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.api.basetypes.complex.KMMComplAcctID;
import org.kmymoney.api.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.api.basetypes.simple.KMMSpltID;
import org.kmymoney.api.basetypes.simple.KMMTrxID;
import org.kmymoney.api.numbers.FixedPointNumber;

/**
 * This denotes a single addition or removal of some
 * value from one account in a transaction made up of
 * multiple such splits.
 */
public interface KMyMoneyTransactionSplit extends Comparable<KMyMoneyTransactionSplit> {

    // For the following states cf.:
    //  - https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneyenums.h
    //  - https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneysplit.cpp
    //    (for actual strings)

    // namespace eMyMoney::Split::Action
    public enum Action {
	
        // ::MAGIC
	CHECK             ( "Check" ),
	DEPOSIT           ( "Deposit" ),
	TRANSFER          ( "Transfer" ),
	WITHDRAWAL        ( "Withdrawal" ),
	ATM               ( "ATM" ),
	AMORTIZATION      ( "Amortization" ),
	INTEREST          ( "Interest" ),
	BUY_SHARES        ( "Buy" ),
	SELL_SHARES       ( "Sell" ),   // actually not used
                                        // (instead, BUY_SHARES w/ neg. value)!
	DIVIDEND          ( "Dividend" ),
	REINVEST_DIVIDEND ( "Reinvest" ),
	YIELD             ( "Yield" ),
	ADD_SHARES        ( "Add" ),
	REMOVE_SHARES     ( "Remove" ), // actually not used
                                        // (instead, ADD_SHARES w/ neg. value)!
	SPLIT_SHARES      ( "Split" ),
	INTEREST_INCOME   ( "IntIncome" );
	
	// ---
	      
	private String code = "UNSET";

	// ---
	      
	Action(String code) {
	    this.code = code;
	}
	      
	// ---
		
	public String getCode() {
	    return code;
	}
		
	// no typo!
	public static Action valueOff(String code) {
	    for ( Action act : values() ) {
		if ( act.getCode().equals(code) ) {
		    return act;
		}
	    }
		    
	    return null;
	}
    }
    
    // Also called "ReconFlag"
    public enum State {

	NOT_RECONCILED ( 0 ),
	CLEARED        ( 1 ),
	RECONCILED     ( 2 ),
	FROZEN         ( 3 );
	
	// ---
	      
	private int index = -1;

	// ---
	      
	State(int index) {
	    this.index = index;
	}
	      
	// ---
		
	public int getIndex() {
	    return index;
	}
		
	// no typo!
	public static State valueOff(int index) {
	    for ( State stat : values() ) {
		if ( stat.getIndex() == index ) {
		    return stat;
		}
	    }
		    
	    return null;
	}
    }
	
    // ---------------------------------------------------------------
    
    /**
     *
     * @return the unique-id to identify this object with across name- and hirarchy-changes
     */
    KMMSpltID getID();

    KMMQualifSpltID getQualifID();

    /**
     *
     * @return the id of the account we transfer from/to.
     */
    KMMComplAcctID getAccountID();

    /**
     * This may be null if an account-id is specified in
     * the gnucash-file that does not belong to an account.
     * @return the account of the account we transfer from/to.
     */
    KMyMoneyAccount getAccount();

    KMMTrxID getTransactionID();

    /**
     * @return the transaction this is a split of.
     */
    KMyMoneyTransaction getTransaction();


    /**
     * The value is in the currency of the transaction!
     * @return the value-transfer this represents
     */
    FixedPointNumber getValue();

    /**
     * The value is in the currency of the transaction!
     * @return the value-transfer this represents
     */
    String getValueFormatted();
    /**
     * The value is in the currency of the transaction!
     * @param lcl the locale to use
     * @return the value-transfer this represents
     */
    String getValueFormatted(Locale lcl);
    /**
     * The value is in the currency of the transaction!
     * @return the value-transfer this represents
     */
    String getValueFormattedForHTML();
    /**
     * The value is in the currency of the transaction!
     * @param lcl the locale to use
     * @return the value-transfer this represents
     */
    String getValueFormattedForHTML(Locale lcl);

    /**
     * @return the balance of the account (in the account's currency)
     *         up to this split.
     */
    FixedPointNumber getAccountBalance();

    /**
     * @return the balance of the account (in the account's currency)
     *         up to this split.
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getAccountBalanceFormatted() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     * @see KMyMoneyAccount#getBalanceFormatted()
     */
    String getAccountBalanceFormatted(Locale lcl) throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * The quantity is in the currency of the account!
     * @return the number of items added to the account
     */
    FixedPointNumber getShares();

    /**
     * The quantity is in the currency of the account!
     * @return the number of items added to the account
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getSharesFormatted() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * The quantity is in the currency of the account!
     * @param lcl the locale to use
     * @return the number of items added to the account
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getSharesFormatted(Locale lcl) throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * The quantity is in the currency of the account!
     * @return the number of items added to the account
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getSharesFormattedForHTML() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * The quantity is in the currency of the account!
     * @param lcl the locale to use
     * @return the number of items added to the account
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getSharesFormattedForHTML(Locale lcl) throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * @return the user-defined description for this object
     *         (may contain multiple lines and non-ascii-characters)
     */
    String getMemo();

      /**
     * Get the type of association this split has with
     * an invoice's lot.
     * @return null, or one of the ACTION_xyz values defined
     * @throws UnknownSplitActionException 
     */
    Action getAction() throws UnknownSplitActionException;

    State getState() throws UnknownSplitStateException;
    
}
