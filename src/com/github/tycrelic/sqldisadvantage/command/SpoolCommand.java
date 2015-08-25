package com.github.tycrelic.sqldisadvantage.command;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.github.tycrelic.sqldisadvantage.parser.ClientEnvironment;

public class SpoolCommand extends SqlClientCommand {

	private String fileName;
	private String mode;

	public void execute(ClientEnvironment env) {
		String substitutedFileName = env.getSubstitutedText(fileName);
		if (substitutedFileName != null || mode != null) {
			try {
				env.closeOutputStream();
				env.setOutputStreamCloseable(false);
			} catch (IOException ex) {
				Logger.getLogger(SpoolCommand.class.getName()).log(Level.SEVERE, null, ex);
			}

			env.setOutputFileName(substitutedFileName);

			try {
				if (substitutedFileName != null) {
					if ("CRE".equalsIgnoreCase(mode) || "CREATE".equalsIgnoreCase(mode)) {
						// to do
						env.setOutputStream(new BufferedOutputStream(new FileOutputStream(substitutedFileName)));
						env.setOutputStreamCloseable(false);
					} else if (mode == null || "REP".equalsIgnoreCase(mode) || "REPLACE".equalsIgnoreCase(mode)) {
						env.setOutputStream(new BufferedOutputStream(new FileOutputStream(substitutedFileName)));
						env.setOutputStreamCloseable(false);
					} else if ("APP".equalsIgnoreCase(mode) || "APPEND".equalsIgnoreCase(mode)) {
						env.setOutputStream(new BufferedOutputStream(new FileOutputStream(substitutedFileName, true)));
						env.setOutputStreamCloseable(false);
					}
				}

				if ("OFF".equalsIgnoreCase(mode)) {
					env.closeOutputStream();
					env.setOutputStreamCloseable(false);
				} else if ("OUT".equalsIgnoreCase(mode)) {
					// to do 
				}
			} catch (IOException ex) {
				Logger.getLogger(SpoolCommand.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			// to do 
		}


	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the mode
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}
}
