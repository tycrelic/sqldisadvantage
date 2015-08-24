package com.github.tycrelic.sqldisadvantage.command;

import com.github.tycrelic.sqldisadvantage.parser.ClientEnvironment;

public class Command {
	private int beginLine;
	private int beginColumn;
	private int endLine;
	private int endColumn;
	private String text;

	public void execute(ClientEnvironment env) {
	}
	
	/**
	 * @return the beginLine
	 */
	public int getBeginLine() {
		return beginLine;
	}

	/**
	 * @param beginLine the beginLine to set
	 */
	public void setBeginLine(int beginLine) {
		this.beginLine = beginLine;
	}

	/**
	 * @return the beginColumn
	 */
	public int getBeginColumn() {
		return beginColumn;
	}

	/**
	 * @param beginColumn the beginColumn to set
	 */
	public void setBeginColumn(int beginColumn) {
		this.beginColumn = beginColumn;
	}

	/**
	 * @return the endLine
	 */
	public int getEndLine() {
		return endLine;
	}

	/**
	 * @param endLine the endLine to set
	 */
	public void setEndLine(int endLine) {
		this.endLine = endLine;
	}

	/**
	 * @return the endColumn
	 */
	public int getEndColumn() {
		return endColumn;
	}

	/**
	 * @param endColumn the endColumn to set
	 */
	public void setEndColumn(int endColumn) {
		this.endColumn = endColumn;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.text = text;
	}
	
}
