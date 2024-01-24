package org.kmymoney.api.read;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.kmymoney.api.basetypes.complex.InvalidQualifSecCurrIDException;
import org.kmymoney.api.basetypes.complex.InvalidQualifSecCurrTypeException;
import org.kmymoney.api.basetypes.complex.KMMComplAcctID;
import org.kmymoney.api.basetypes.complex.KMMQualifSecCurrID;
import org.kmymoney.api.basetypes.complex.KMMQualifSpltID;
import org.kmymoney.api.numbers.FixedPointNumber;
import org.kmymoney.api.read.hlp.HasUserDefinedAttributes;

/**
 * A KMyMoney account satisfies the "normal" definition of the term in 
 * accounting (<a href="https://en.wikipedia.org/wiki/Account_(bookkeeping)">Wikipedia</a>).
 * <br>
 * You can also see it as a collection of transactions that start or end there. 
 * <br>
 * An account has a balance.  
 * <br>
 * All accounts taken together define the so-called chart of accounts,
 * organized in a tree (the top node of the tree being the root account). 
 * That means that each account may have a parent-account as well as one or 
 * several child-accounts.
 * <br>
 * Cf. <a href="https://docs.kde.org/stable5/en/kmymoney/kmymoney/makingmostof.mapping.html#makingmostof.mapping.accounts">KMyMoney handbook</a>
 */
public interface KMyMoneyAccount extends Comparable<KMyMoneyAccount>,
                                         HasUserDefinedAttributes
{

    // For the following types cf.:
    // https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneyaccount.h
    //
    /**
     * The current assignment is as follows:
     *
     * - Asset
     *   - Asset
     *   - Checkings
     *   - Savings
     *   - Cash
     *   - Currency
     *   - Investment
     *   - MoneyMarket
     *   - CertificateDep
     *   - AssetLoan
     *   - Stock
     *
     * - Liability
     *   - Liability
     *   - CreditCard
     *   - Loan
     *
     * - Income
     *   - Income
     *
     * - Expense
     *   - Expense
     *
     * - Equity
     *   - Equity
     */
    
    // For the following types cf.:
    // https://github.com/KDE/kmymoney/blob/master/kmymoney/mymoney/mymoneyenums.h
    /*
     * Checkings,         Standard checking account
     * Savings,              Typical savings account
     * Cash,                 Denotes a shoe-box or pillowcase stuffed with cash
     * CreditCard,           Credit card accounts
     * Loan,                 Loan and mortgage accounts (liability)
     * CertificateDep,       Certificates of Deposit
     * Investment,           Investment account
     * MoneyMarket,          Money Market Account
     * Asset,                Denotes a generic asset account
     * Liability,            Denotes a generic liability account.
     * Currency,             Denotes a currency trading account.
     * Income,               Denotes an income account
     * Expense,              Denotes an expense account
     * AssetLoan,            Denotes a loan (asset of the owner of this object)
     * Stock,                Denotes an security account as sub-account for an investment
     * Equity,               Denotes an equity account e.g. opening/closing balance
    */

    public enum Type {
    	
        // ::MAGIC
    	CHECKING            (  1 ),
    	SAVINGS             (  2 ),
    	CASH                (  3 ),
    	CREDIT_CARD         (  4 ),
    	LOAN                (  5 ),
    	CERTIFICATE_DEPOSIT (  6 ),
    	INVESTMENT          (  7 ),
    	MONEY_MARKET        (  8 ),
    	ASSET               (  9 ),
    	LIABILITY           ( 10 ),
    	CURRENCY            ( 11 ),
    	INCOME              ( 12 ),
    	EXPENSE             ( 13 ),
    	ASSET_LOAN          ( 14 ),
    	STOCK               ( 15 ),
    	EQUITY              ( 16 );

    	// ---
	      
    	private int code = 0;

    	// ---
    	      
    	Type(int code) {
    	    this.code = code;
    	}
    	      
    	// ---
    		
    	public int getCode() {
    	    return code;
    	}
    		
    	public BigInteger getCodeBig() {
    	    return BigInteger.valueOf(getCode());
    	}
    		
    	// no typo!
    	public static Type valueOff(int code) {
    	    for ( Type type : values() ) {
    		if ( type.getCode() == code ) {
    		    return type;
    		}
    	    }
    		    
    	    return null;
    	}
    }
    
    // -----------------------------------------------------------------
    
    public static String SEPARATOR = "::";

    // -----------------------------------------------------------------

    /**
     * @return the unique id for that account (not meaningfull to human users)
     */
    KMMComplAcctID getID();

    /**
     * @return a user-defined description to acompany the name of the account. Can
     *         encompass many lines.
     */
    String getMemo();

    /**
     * @return the account-number
     */
    String getNumber();

    /**
     * @return user-readable name of this account. Does not contain the name of
     *         parent-accounts
     */
    String getName();

    /**
     * get name including the name of the parent.accounts.
     *
     * @return e.g. "Asset::Barvermögen::Bargeld"
     */
    String getQualifiedName();

    /**
     * @return null if the account is below the root
     */
    KMMComplAcctID getParentAccountID();
    
    boolean isRootAccount();

    /**
     * @return the parent-account we are a child of or null if we are a top-level
     *         account
     */
    KMyMoneyAccount getParentAccount();

    /**
     * The returned collection is never null and is sorted by Account-Name.
     *
     * @return all child-accounts
     * @see #getChildren()
     */
    Collection<KMyMoneyAccount> getSubAccounts();

    /**
     * The returned collection is never null and is sorted by Account-Name.
     *
     * @return all child-accounts
     */
    Collection<KMyMoneyAccount> getChildren();

    /**
     * @param account the account to test
     * @return true if this is a child of us or any child's or us.
     */
    boolean isChildAccountRecursive(KMyMoneyAccount account);

    // ----------------------------

    Type getType() throws UnknownAccountTypeException;

    KMMQualifSecCurrID getSecCurrID() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    // -----------------------------------------------------------------

    /**
     * The returned list ist sorted by the natural order of the Transaction-Splits.
     *
     * @return all splits
     * @link KMyMoneyTransactionSplit
     */
    List<? extends KMyMoneyTransactionSplit> getTransactionSplits();

    /**
     * @param id the split-id to look for
     * @return the identified split or null
     */
    KMyMoneyTransactionSplit getTransactionSplitByID(final KMMQualifSpltID id);

    /**
     * Gets the last transaction-split before the given date.
     *
     * @param date if null, the last split of all time is returned
     * @return the last transaction-split before the given date
     */
    KMyMoneyTransactionSplit getLastSplitBeforeRecursive(final LocalDate date);

    /**
     * @param split split to add to this transaction
     */
    void addTransactionSplit(final KMyMoneyTransactionSplit split);

    // ----------------------------

    /**
     * @return true if ${@link #getTransactionSplits()}.size()>0
     */
    boolean hasTransactions();

    /**
     * @return true if ${@link #hasTransactions()} is true for this or any
     *         sub-accounts
     */
    boolean hasTransactionsRecursive();

    /**
     * The returned list ist sorted by the natural order of the Transaction-Splits.
     *
     * @return all splits
     * @link KMyMoneyTransaction
     */
    List<KMyMoneyTransaction> getTransactions();

    // -----------------------------------------------------------------

    /**
     * same as getBalance(new Date()).<br/>
     * ignores transactions after the current date+time<br/>
     * Be aware that the result is in the currency of this account!
     *
     * @return the balance
     */
    FixedPointNumber getBalance();

    /**
     * Be aware that the result is in the currency of this account!
     *
     * @param date if non-null transactions after this date are ignored in the
     *             calculation
     * @return the balance formatted using the current locale
     */
    FixedPointNumber getBalance(final LocalDate date);

    /**
     * Be aware that the result is in the currency of this account!
     *
     * @param date  if non-null transactions after this date are ignored in the
     *              calculation
     * @param after splits that are after date are added here.
     * @return the balance formatted using the current locale
     */
    FixedPointNumber getBalance(final LocalDate date, Collection<KMyMoneyTransactionSplit> after);

    /**
     * @param lastIncludesSplit last split to be included
     * @return the balance up to and including the given split
     */
    FixedPointNumber getBalance(final KMyMoneyTransactionSplit lastIncludesSplit);

    // ----------------------------

    /**
     * same as getBalance(new Date()). ignores transactions after the current
     * date+time
     *
     * @return the balance formatted using the current locale
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getBalanceFormatted() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * same as getBalance(new Date()). ignores transactions after the current
     * date+time
     *
     * @param lcl the locale to use (does not affect the currency)
     * @return the balance formatted using the given locale
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getBalanceFormatted(final Locale lcl) throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    // ----------------------------

    /**
     * same as getBalanceRecursive(new Date()).<br/>
     * ignores transactions after the current date+time<br/>
     * Be aware that the result is in the currency of this account!
     *
     * @return the balance including sub-accounts
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    FixedPointNumber getBalanceRecursive() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * Gets the balance including all sub-accounts.
     *
     * @param date if non-null transactions after this date are ignored in the
     *             calculation
     * @return the balance including all sub-accounts
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    FixedPointNumber getBalanceRecursive(final LocalDate date) throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * Ignores accounts for which this conversion is not possible.
     *
     * @param date     ignores transactions after the given date
     * @param curr 
     * @param currency the currency the result shall be in
     * @return Gets the balance including all sub-accounts.
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     * @see KMyMoneyAccount#getBalanceRecursive(LocalDate)
     */
    FixedPointNumber getBalanceRecursive(final LocalDate date, final Currency curr) throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * Ignores accounts for which this conversion is not possible.
     *
     * @param date              ignores transactions after the given date
     * @param secCurrID         the currency the result shall be in
     * @return Gets the balance including all sub-accounts.
     * @throws InvalidQualifSecCurrIDException 
     * @see KMyMoneyAccount#getBalanceRecursive(Date, Currency)
     */
    FixedPointNumber getBalanceRecursive(final LocalDate date, final KMMQualifSecCurrID secCurrID) throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    // ----------------------------

    /**
     * same as getBalanceRecursive(new Date()). ignores transactions after the
     * current date+time
     *
     * @return the balance including sub-accounts formatted using the current locale
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getBalanceRecursiveFormatted() throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

    /**
     * Gets the balance including all sub-accounts.
     *
     * @param date if non-null transactions after this date are ignored in the
     *             calculation
     * @return the balance including all sub-accounts
     * @throws InvalidQualifSecCurrIDException 
     * @throws InvalidQualifSecCurrTypeException 
     */
    String getBalanceRecursiveFormatted(final LocalDate date) throws InvalidQualifSecCurrTypeException, InvalidQualifSecCurrIDException;

}
