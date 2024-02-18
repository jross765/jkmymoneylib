package org.example.kmymoneyapi.write;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.kmymoney.api.basetypes.simple.KMMAcctID;
import org.kmymoney.api.basetypes.simple.KMMPyeID;
import org.kmymoney.api.numbers.FixedPointNumber;
import org.kmymoney.api.write.KMyMoneyWritableTransaction;
import org.kmymoney.api.write.KMyMoneyWritableTransactionSplit;
import org.kmymoney.api.write.impl.KMyMoneyWritableFileImpl;

public class GenTrx {
    // BEGIN Example data -- adapt to your needs
    private static String kmmInFileName  = "example_in.xml";
    private static String kmmOutFileName = "example_out.xml";
    
    // ---
    
    // Following two account IDs: sic, not KMMComplAcctID
    private static KMMAcctID        fromAcct1ID  = new KMMAcctID("A000066"); // Asset:Barvermögen:Bargeld:Kasse Ada
    private static KMMAcctID        toAcct1ID    = new KMMAcctID("A000010"); // Expense:Bildung:Zeitungen
    private static KMMPyeID         pye1ID       = new KMMPyeID("P000009");  // optional, may be null
    private static KMyMoneyWritableTransactionSplit.Action act1 = null;      // Do not set here 
    private static FixedPointNumber amt1         = new FixedPointNumber("1234/100");
    private static FixedPointNumber qty1         = amt1;
    private static LocalDate        datPst1      = LocalDate.of(2024, 2, 15);
    private static String           descr1       = "Bahnhof Zeitungskiosk";
    
    // ---
    
    // Following two account IDs: sic, not KMMComplAcctID
    private static KMMAcctID        fromAcct2ID  = new KMMAcctID("A000004"); // Asset:Barvermögen:Giro RaiBa
    private static KMMAcctID        toAcct21ID   = new KMMAcctID("A000063"); // Asset:Finanzanlagen:Depot RaiBa:DE0007164600 SAP
    private static KMMAcctID        toAcct22ID   = new KMMAcctID("A000020"); // Expense:Sonstiges:Bankgebühren
    private static KMMPyeID         pye2ID       = null;                     // Do not set here
    private static KMyMoneyWritableTransactionSplit.Action act2 = KMyMoneyWritableTransactionSplit.Action.BUY_SHARES;
    private static FixedPointNumber qty22        = new FixedPointNumber("15");
    private static FixedPointNumber prc1         = new FixedPointNumber("1/1");       // optional
    private static FixedPointNumber prc2         = new FixedPointNumber("15574/100"); // half-mandatory
    private static FixedPointNumber prc3         = prc1;                              // optional
    private static FixedPointNumber amt22        = qty22.multiply(prc2);              // net
    private static FixedPointNumber amt23        = new FixedPointNumber("95/10");     // fees & commissions
    private static FixedPointNumber amt21        = amt22.add(amt23);                  // gross
    private static FixedPointNumber qty21        = amt21;
    private static FixedPointNumber qty23        = amt23;
    private static LocalDate        datPst2      = LocalDate.of(2024, 1, 15);
    private static String           descr2       = "Aktienkauf";
    // END Example data

    // -----------------------------------------------------------------

    public static void main(String[] args) {
	try {
	    GenTrx tool = new GenTrx();
	    tool.kernel();
	} catch (Exception exc) {
	    System.err.println("Execution exception. Aborting.");
	    exc.printStackTrace();
	    System.exit(1);
	}
    }

    protected void kernel() throws Exception {
	KMyMoneyWritableFileImpl kmmFile = new KMyMoneyWritableFileImpl(new File(kmmInFileName));

	System.out.println("---------------------------");
	System.out.println("Generate transaction no. 1:");
	System.out.println("---------------------------");
	genTrx1(kmmFile);

	System.out.println("");
	System.out.println("---------------------------");
	System.out.println("Generate transaction no. 2:");
	System.out.println("---------------------------");
	genTrx2(kmmFile);
	
	System.out.println("");
	System.out.println("---------------------------");
	System.out.println("Write file:");
	System.out.println("---------------------------");
	kmmFile.writeFile(new File(kmmOutFileName));
	
	System.out.println("OK");
    }

    private void genTrx1(KMyMoneyWritableFileImpl kmmFile) throws IOException {
	System.err.println("Account 1 name (from): '" + kmmFile.getAccountByID(fromAcct1ID).getQualifiedName() + "'");
	System.err.println("Account 2 name (to):   '" + kmmFile.getAccountByID(toAcct1ID).getQualifiedName() + "'");

	// ---

	KMyMoneyWritableTransaction trx = kmmFile.createWritableTransaction();
	// Does not work like that: The description/memo on transaction
	// level is purely internal:
	// trx.setDescription(description);
	trx.setDescription("Generated by GenTrx, " + LocalDateTime.now());

	// ---

	KMyMoneyWritableTransactionSplit splt1 = trx.createWritableSplit(kmmFile.getAccountByID(fromAcct1ID));
	splt1.setValue(new FixedPointNumber(amt1.negate()));
	splt1.setShares(new FixedPointNumber(qty1.negate()));
	splt1.setPayeeID(pye1ID);
	// This is what we actually want (cf. above):
	splt1.setDescription(descr1);
	System.out.println("Split 1 to write: " + splt1.toString());

	// ---

	KMyMoneyWritableTransactionSplit splt2 = trx.createWritableSplit(kmmFile.getAccountByID(toAcct1ID));
	splt2.setValue(new FixedPointNumber(amt1));
	splt2.setShares(new FixedPointNumber(qty1));
	splt2.setPayeeID(pye1ID);
	// Cf. above
	splt2.setDescription(descr1);
	System.out.println("Split 2 to write: " + splt2.toString());

	// ---

	trx.setDatePosted(datPst1);
	trx.setDateEntered(LocalDate.now());

	// ---

	System.out.println("Transaction to write: " + trx.toString());
    }

    private void genTrx2(KMyMoneyWritableFileImpl kmmFile) throws IOException {
	System.err.println("Account 1 name (from): '" + kmmFile.getAccountByID(fromAcct2ID).getQualifiedName() + "'");
	System.err.println("Account 2 name (to):   '" + kmmFile.getAccountByID(toAcct21ID).getQualifiedName() + "'");
	System.err.println("Account 3 name (to):   '" + kmmFile.getAccountByID(toAcct22ID).getQualifiedName() + "'");

	// ---

	KMyMoneyWritableTransaction trx = kmmFile.createWritableTransaction();
	// Does not work like that: The description/memo on transaction
	// level is purely internal:
	// trx.setDescription(description);
	trx.setDescription("Generated by GenTrx, " + LocalDateTime.now());

	// ---

	KMyMoneyWritableTransactionSplit splt1 = trx.createWritableSplit(kmmFile.getAccountByID(fromAcct2ID));
	splt1.setValue(new FixedPointNumber(amt21.negate()));
	splt1.setShares(new FixedPointNumber(qty21.negate()));
	splt1.setPrice(prc1); // completely optional 
	// This is what we actually want (cf. above):
	splt1.setDescription(descr2);
	System.out.println("Split 1 to write: " + splt1.toString());

	// ---

	KMyMoneyWritableTransactionSplit splt2 = trx.createWritableSplit(kmmFile.getAccountByID(toAcct21ID));
	splt2.setValue(new FixedPointNumber(amt22));
	splt2.setShares(new FixedPointNumber(qty22));
	splt2.setPrice(prc2); // optional (sic), but advisable
	splt2.setAction(act2);
	// Cf. above
	splt2.setDescription(descr2);
	System.out.println("Split 2 to write: " + splt2.toString());

	// ---

	KMyMoneyWritableTransactionSplit splt3 = trx.createWritableSplit(kmmFile.getAccountByID(toAcct22ID));
	splt3.setValue(new FixedPointNumber(amt23));
	splt3.setShares(new FixedPointNumber(qty23));
	splt3.setPrice(prc3); // completely optional
	// Cf. above
	splt3.setDescription(descr2);
	System.out.println("Split 3 to write: " + splt3.toString());

	// ---

	trx.setDatePosted(datPst2);
	trx.setDateEntered(LocalDate.now());

	// ---

	System.out.println("Transaction to write: " + trx.toString());
    }

}
