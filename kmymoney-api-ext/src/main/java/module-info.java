module kmymoney.apiext {
	requires static org.slf4j;
	// requires java.desktop;
	
	requires transitive kmymoney.base;
	requires transitive kmymoney.api;

	exports org.kmymoney.apiext.secacct;
}