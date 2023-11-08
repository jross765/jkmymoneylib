package org.example.kmymoney.read;

import java.io.File;

import org.kmymoney.read.KMyMoneyTransaction;
import org.kmymoney.read.KMyMoneyTransactionSplit;
import org.kmymoney.read.impl.KMyMoneyFileImpl;

public class GetTrxInfo {
    // BEGIN Example data -- adapt to your needs
    private static String kmmFileName = null;
    private static String trxID = null;
    // END Example data

    public static void main(String[] args) {
	try {
	    GetTrxInfo tool = new GetTrxInfo();
	    tool.kernel();
	} catch (Exception exc) {
	    System.err.println("Execution exception. Aborting.");
	    exc.printStackTrace();
	    System.exit(1);
	}
    }

    protected void kernel() throws Exception {
	KMyMoneyFileImpl kmmFile = new KMyMoneyFileImpl(new File(kmmFileName));

	KMyMoneyTransaction trx = kmmFile.getTransactionById(trxID);

	try {
	    System.out.println("ID:              " + trx.getId());
	} catch (Exception exc) {
	    System.out.println("ID:              " + "ERROR");
	}

	try {
	    System.out.println("toString:        " + trx.toString());
	} catch (Exception exc) {
	    System.out.println("toString:        " + "ERROR");
	}

	try {
	    System.out.println("Balance:         " + trx.getBalanceFormatted());
	} catch (Exception exc) {
	    System.out.println("Balance:         " + "ERROR");
	}

//    try
//    {
//      System.out.println("Cmdty/Curr:      '" + trx.getCmdtyCurrID() + "'");
//    }
//    catch ( Exception exc )
//    {
//      System.out.println("Cmdty/Curr:      " + "ERROR");
//    }

	try {
	    System.out.println("Description:     '" + trx.getMemo() + "'");
	} catch (Exception exc) {
	    System.out.println("Description:     " + "ERROR");
	}

	// ---

	showSplits(trx);
    }

    // -----------------------------------------------------------------

    private void showSplits(KMyMoneyTransaction trx) {
	System.out.println("");
	System.out.println("Splits:");

	for (KMyMoneyTransactionSplit splt : trx.getSplits()) {
	    System.out.println(" - " + splt.toString());
	}
    }
}