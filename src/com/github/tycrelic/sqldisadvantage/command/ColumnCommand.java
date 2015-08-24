package com.github.tycrelic.sqldisadvantage.command;

import com.github.tycrelic.sqldisadvantage.parser.ClientEnvironment;

public class ColumnCommand extends SqlClientCommand {
	private String columnName;
  private String variableName;

	/*public void execute(ClientEnvironment env) {
		
	}*/
	
	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @param columnName the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName.toUpperCase();
	}

	/**
	 * @return the variableName
	 */
	public String getVariableName() {
		return variableName;
	}

	/**
	 * @param variableName the variableName to set
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName.toUpperCase();
	}

}
