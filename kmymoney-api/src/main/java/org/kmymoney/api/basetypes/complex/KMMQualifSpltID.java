package org.kmymoney.api.basetypes.complex;

import java.util.Objects;

import org.kmymoney.api.basetypes.simple.KMMSpltID;
import org.kmymoney.api.basetypes.simple.KMMTrxID;

/**
 * KMyMoney's Split "IDs" are in fact no IDs, because they are only unique within
 * the scope of their respective transaction.
 * 
 * In order to get the ID "real" (like globally unique), we have to pair it
 * with its transaction ID.
 */
public class KMMQualifSpltID {
    
    private KMMTrxID  trxID;
    private KMMSpltID spltID;
    
    // ---------------------------------------------------------------

    public KMMQualifSpltID(KMMTrxID trxID, KMMSpltID spltID) {
	setTransactionID(trxID);
	setSplitID(spltID);
    }
    
    public KMMQualifSpltID(String trxID, String spltID) {
	setTransactionID(trxID);
	setSplitID(spltID);
    }
    
    // ---------------------------------------------------------------

    public KMMTrxID getTransactionID() {
        return trxID;
    }

    public void setTransactionID(KMMTrxID trxID) {
        this.trxID = trxID;
    }

    public void setTransactionID(String trxIDStr) {
        setTransactionID(new KMMTrxID(trxIDStr));
    }

    public KMMSpltID getSplitID() {
        return spltID;
    }

    public void setSplitID(KMMSpltID spltID) {
        this.spltID = spltID;
    }

    public void setSplitID(String spltIDStr) {
        setSplitID(new KMMSpltID(spltIDStr));
    }

    // ---------------------------------------------------------------
    
    public void set(KMMTrxID trxID, String spltID) {
	setTransactionID(trxID);
	setSplitID(spltID);
    }

    public void set(KMMQualifSpltID spltID) {
	setTransactionID(spltID.getTransactionID());
	setSplitID(spltID.getSplitID());
    }

    // ---------------------------------------------------------------

    @Override
    public int hashCode() {
	return Objects.hash(spltID, trxID);
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!(obj instanceof KMMQualifSpltID)) {
	    return false;
	}
	KMMQualifSpltID other = (KMMQualifSpltID) obj;
	return Objects.equals(spltID, other.spltID) && 
	       Objects.equals(trxID, other.trxID);
    }

    // ---------------------------------------------------------------

    @Override
    public String toString() {
	return toStringShort();
    }

    public String toStringShort() {
	return trxID + ":" + spltID;
    }

    public String toStringLong() {
	return "KMMSplitID [trxID=" + trxID + ", spltID=" + spltID + "]";
    }

}
