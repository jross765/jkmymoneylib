package org.kmymoney.basetypes.simple;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPackage extends TestCase {
    public static void main(String[] args) throws Exception {
	junit.textui.TestRunner.run(suite());
    }

    @SuppressWarnings("exports")
    public static Test suite() throws Exception {
	TestSuite suite = new TestSuite();

	suite.addTest(org.kmymoney.basetypes.simple.TestKMMAcctID.suite());
	suite.addTest(org.kmymoney.basetypes.simple.TestKMMInstID.suite());
	suite.addTest(org.kmymoney.basetypes.simple.TestKMMPyeID.suite());
	suite.addTest(org.kmymoney.basetypes.simple.TestKMMSecID.suite());
	suite.addTest(org.kmymoney.basetypes.simple.TestKMMSpltID.suite());
	suite.addTest(org.kmymoney.basetypes.simple.TestKMMTrxID.suite());

	return suite;
    }
}
