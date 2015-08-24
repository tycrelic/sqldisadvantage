package com.github.tycrelic.sqldisadvantage.command;

import com.github.tycrelic.sqldisadvantage.parser.ClientEnvironment;

public class SetCommand extends SqlClientCommand {

	private String name;
	private String value;

	public void execute(ClientEnvironment env) {
		String substitutedValue = env.getSubstitutedText(value);
		env.setSystemVariable(name, substitutedValue);

		if ("DEF".equalsIgnoreCase(name) || "DEFINE".equalsIgnoreCase(name)) {
			if ("ON".equals(substitutedValue)) {
				env.setScannable(true);
			} else if ("OFF".equals(substitutedValue)) {
				env.setScannable(false);
			}
			// else // to do
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
