package org.kmymoney.api.write.impl.hlp;

import java.io.IOException;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * replaces ':' in tag-names and attribute-names by '_'
 */
public class NamespaceAdderWriter extends Writer {

	private static final Logger LOGGER = LoggerFactory.getLogger(NamespaceAdderWriter.class);

	// ---------------------------------------------------------------

	private final Writer output;

	private boolean isInQuotation = false;
	private boolean isInTag = false;

	// ---------------------------------------------------------------

	public NamespaceAdderWriter(final Writer input) {
		super();
		output = input;
	}

	// ---------------------------------------------------------------

	public Writer getWriter() {
		return output;
	}

	@Override
	public void flush() throws IOException {
		output.flush();
	}

	@Override
	public void write(final char[] cbuf, final int off, final int len) throws IOException {

		for ( int i = off; i < off + len; i++ ) {
			if ( isInTag && 
				 ( cbuf[i] == '"' || 
				   cbuf[i] == '\'' ) ) {
				toggleIsInQuotation();
			} else if ( cbuf[i] == '<' && 
					    ! isInQuotation ) {
				isInTag = true;
			} else if ( cbuf[i] == '>' && 
					    ! isInQuotation ) {
				isInTag = false;
			} else if ( cbuf[i] == '_' && 
					    isInTag && 
					    ! isInQuotation ) {

				// do NOT replace the second "_" in but everywhere else inside tag-names
				// cmdty:quote_source
				// cmdty:get_quotes
				// fs:ui_type
				// invoice:billing_id
				// recurrence:period_type

				if ( i <= "fs:ui".length() || 
					 ! (new String(cbuf, i - "fs:ui".length(), "fs:ui".length()).equals("fs:ui")) ) {
					if ( i <= "cmdty:get".length() || 
						 ! (new String(cbuf, i - "cmdty:get".length(), "cmdty:get".length()) .equals("cmdty:get")) ) {
						if ( i <= "cmdty:quote".length() || 
							 ! (new String(cbuf, i - "cmdty:quote".length(), "cmdty:quote".length()).equals("cmdty:quote")) ) {
							if ( i <= "invoice:billing".length() || 
								 ! (new String(cbuf, i - "invoice:billing".length(), "invoice:billing".length()).equals("invoice:billing")) ) {
								if ( i <= "recurrence:period".length() || 
									 ! (new String(cbuf, i - "recurrence:period".length(), "recurrence:period".length()).equals("recurrence:period")) ) {
									cbuf[i] = ':';
								}
							}
						}
					}
				}
			}

		}

		output.write(cbuf, off, len);

		// this is a quick hack to add the missing xmlns-declarations
		if ( len == 7 && new String(cbuf, off, len).equals("<gnc-v2") ) {
			output.write("\n");
		}

	}

	@Override
	public void close() throws IOException {
		output.close();
	}

	private void toggleIsInQuotation() {
		if ( isInQuotation ) {
			isInQuotation = false;
		} else {
			isInQuotation = true;
		}
	}
}