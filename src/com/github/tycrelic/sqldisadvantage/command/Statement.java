package com.github.tycrelic.sqldisadvantage.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.github.tycrelic.sqldisadvantage.parser.ClientEnvironment;

public class Statement extends Command {

	private boolean queryable;
	private String sql;

	public void execute(ClientEnvironment env) {
		String substitutedSql = env.getSubstitutedText(getSql());
		try {
			env.runStatement(substitutedSql, queryable);
		} catch (SQLException ex) {
			Logger.getLogger(Statement.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(Statement.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void substitute() {
	}

	/**
	 * @return the queryable
	 */
	public boolean isQueryable() {
		return queryable;
	}

	/**
	 * @param queryable the queryable to set
	 */
	public void setQueryable(boolean queryable) {
		this.queryable = queryable;
	}

	/**
	 * @return the sql
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * @param sql the sql to set
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}
}
