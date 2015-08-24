package com.github.tycrelic.sqldisadvantage.command;

import com.github.tycrelic.sqldisadvantage.parser.ClientEnvironment;

public class DefineCommand extends SqlClientCommand {

	private String name;
	private String value;

	public void execute(ClientEnvironment env) {
		super.execute(env);
		String substitutedValue = env.getSubstitutedText(value);
		env.setSubstitutionVariable(name, substitutedValue);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.toUpperCase();
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
