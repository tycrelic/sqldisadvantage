package com.github.tycrelic.sqldisadvantage.parser;

import com.github.tycrelic.sqldisadvantage.io.Exporter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.*;
import com.github.tycrelic.sqldisadvantage.command.Command;
import java.util.*;

public class ClientEnvironment {

  private static final String[][] SYSTEM_VARIABLE_PAIRS = {
    {"APPI", "APPINFO"},
    {"APPINFO", "APPINFO"},
    {"ARRAY", "ARRAYSIZE"},
    {"ARRAYSIZE", "ARRAYSIZE"},
    {"AUTO", "AUTOCOMMIT"},
    {"AUTOCOMMIT", "AUTOCOMMIT"},
    {"AUTOP", "AUTOPRINT"},
    {"AUTOPRINT", "AUTOPRINT"},
    {"AUTORECOVERY", "AUTORECOVERY"},
    {"AUTORECOVERY", "AUTORECOVERY"},
    {"AUTOT", "AUTOTRACE"},
    {"AUTOTRACE", "AUTOTRACE"},
    {"BLO", "BLOCKTERMINATOR"},
    {"BLOCKTERMINATOR", "BLOCKTERMINATOR"},
    {"CMDS", "CMDSEP"},
    {"CMDSEP", "CMDSEP"},
    {"COLSEP", "COLSEP"},
    {"CON", "CONCAT"},
    {"CONCAT", "CONCAT"},
    {"COPYC", "COPYCOMMIT"},
    {"COPYCOMMIT", "COPYCOMMIT"},
    {"COPYTYPECHECK", "COPYTYPECHECK"},
    {"DEF", "DEFINE"},
    {"DEFINE", "DEFINE"},
    {"DESCRIBE", "DESCRIBE"},
    {"ECHO", "ECHO"},
    {"EDITF", "EDITFILE"},
    {"EDITFILE", "EDITFILE"},
    {"EMB", "EMBEDDED"},
    {"EMBEDDED", "EMBEDDED"},
    {"ERRORL", "ERRORLOGGING"},
    {"ERRORLOGGING", "ERRORLOGGING"},
    {"ESC", "ESCAPE"},
    {"ESCAPE", "ESCAPE"},
    {"ESCCHAR", "ESCCHAR"},
    {"EXITC", "EXITCOMMIT"},
    {"EXITCOMMIT", "EXITCOMMIT"},
    {"FEED", "FEEDBACK"},
    {"FEEDBACK", "FEEDBACK"},
    {"FLAGGER", "FLAGGER"},
    {"FLU", "FLUSH"},
    {"FLUSH", "FLUSH"},
    {"HEA", "HEADING"},
    {"HEADING", "HEADING"},
    {"HEADSEP", "HEADSEP"},
    {"HEADSEP", "HEADSEP"},
    {"INSTANCE", "INSTANCE"},
    {"LIN", "LINESIZE"},
    {"LINESIZE", "LINESIZE"},
    {"LOBOF", "LOBOFFSET"},
    {"LOBOFFSET", "LOBOFFSET"},
    {"LOGSOURCE", "LOGSOURCE"},
    {"LONG", "LONG"},
    {"LONGC", "LONGCHUNKSIZE"},
    {"LONGCHUNKSIZE", "LONGCHUNKSIZE"},
    {"MARK", "MARKUP"},
    {"MARKUP", "MARKUP"},
    {"NEW", "NEWPAGE"},
    {"NEWPAGE", "NEWPAGE"},
    {"NULL", "NULL"},
    {"NUM", "NUMWIDTH"},
    {"NUMF", "NUMFORMAT"},
    {"NUMFORMAT", "NUMFORMAT"},
    {"NUMWIDTH", "NUMWIDTH"},
    {"OUTPUTFORMAT", "OUTPUTFORMAT"}, // extension
    {"PAGES", "PAGESIZE"},
    {"PAGESIZE", "PAGESIZE"},
    {"PAU", "PAUSE"},
    {"PAUSE", "PAUSE"},
    {"RECSEP", "RECSEP"},
    {"RECSEPCHAR", "RECSEPCHAR"},
    {"SERVEROUT", "SERVEROUTPUT"},
    {"SERVEROUTPUT", "SERVEROUTPUT"},
    {"SHIFT", "SHIFTINOUT"},
    {"SHIFTINOUT", "SHIFTINOUT"},
    {"SHOW", "SHOWMODE"},
    {"SHOWMODE", "SHOWMODE"},
    {"SQLBL", "SQLBLANKLINES"},
    {"SQLBLANKLINES", "SQLBLANKLINES"},
    {"SQLC", "SQLCASE"},
    {"SQLCASE", "SQLCASE"},
    {"SQLCO", "SQLCONTINUE"},
    {"SQLCONTINUE", "SQLCONTINUE"},
    {"SQLN", "SQLNUMBER"},
    {"SQLNUMBER", "SQLNUMBER"},
    {"SQLP", "SQLPROMPT"},
    {"SQLPLUSCOMPAT", "SQLPLUSCOMPATIBILITY"},
    {"SQLPLUSCOMPATIBILITY", "SQLPLUSCOMPATIBILITY"},
    {"SQLPRE", "SQLPREFIX"},
    {"SQLPREFIX", "SQLPREFIX"},
    {"SQLPROMPT", "SQLPROMPT"},
    {"SQLT", "SQLTERMINATOR"},
    {"SQLTERMINATOR", "SQLTERMINATOR"},
    {"SUF", "SUFFIX"},
    {"SUFFIX", "SUFFIX"},
    {"TAB", "TAB"},
    {"TERM", "TERMOUT"},
    {"TERMOUT", "TERMOUT"},
    {"TI", "TIME"},
    {"TIME", "TIME"},
    {"TIMI", "TIMING"},
    {"TIMING", "TIMING"},
    {"TRIM", "TRIMOUT"},
    {"TRIMOUT", "TRIMOUT"},
    {"TRIMS", "TRIMSPOOL"},
    {"TRIMSPOOL", "TRIMSPOOL"},
    {"UND", "UNDERLINE"},
    {"UNDERLINE", "UNDERLINE"},
    {"VER", "VERIFY"},
    {"VERIFY", "VERIFY"},
    {"WRA", "WRAP"},
    {"WRAP", "WRAP"},
    {"XMLOPT", "XMLOPTIMIZATIONCHECK"},
    {"XMLOPTIMIZATIONCHECK", "XMLOPTIMIZATIONCHECK"},
    {"XQUERY", "XQUERY"}
  };
  private static final Map<String, String> SYSTEM_VARIABLE_NAME_MAP = new TreeMap();
  private Map<String, String> systemVariables = new TreeMap();
  private Map<String, String> substitutionVariables = new TreeMap();
  private Map<String, String> bindVariables = new TreeMap();
  private ArrayList<Command> commands = new ArrayList<Command>();
  private Connection connection;
  private String outputFileName;
  private OutputStream outputStream;
  private boolean outputStreamCloseable;
  private boolean scannable = true;

  static {
    for (String[] pair : SYSTEM_VARIABLE_PAIRS) {
      SYSTEM_VARIABLE_NAME_MAP.put(pair[0], pair[1]);
    }
  }

  public void setArguements(String[] args) {
    if (args != null) {
      for (int i = 0, len = args.length; i < len; ++i) {
        this.setSubstitutionVariable(Integer.toString(i+1), args[i]);
      }
    }
  }

  public void connect(String driverName, String connectionString) throws SQLException, ClassNotFoundException {
    Class.forName(driverName);
    connection = DriverManager.getConnection(connectionString);
  }

  public void connect(String driverName, String connectionString, String userName, String password) throws SQLException, ClassNotFoundException {
    Class.forName(driverName);
    connection = DriverManager.getConnection(connectionString, userName, password);
  }

  public void disconnect() throws SQLException {
    if (connection != null && !connection.isClosed()) {
      connection.close();
    }
  }

  public void closeOutputStream() throws IOException {
    if (outputStreamCloseable && outputStream != null) {
      outputStream.close();
      outputStream = null;
    }
  }

  public String getSubstitutedText(String text) {
    if (scannable && text != null) {
      int pos;
      int subsVarNameStartIndex;
      int subsVarNameEndIndex;
      int subsEndIndex;
      StringBuilder buf = new StringBuilder();
      int len = text.length();
      int maxEndIndex = len - 1;

      String resultText;
      if ((pos = text.indexOf('&', 0)) >= 0) {
        int subsStartIndex = pos;
        buf.append(text, 0, pos);
        while ((subsStartIndex = text.indexOf('&', pos)) >= 0) {
          subsVarNameStartIndex = subsStartIndex + (subsStartIndex < maxEndIndex && text.charAt(subsStartIndex + 1) == '&' ? 2 : 1);
          if (subsVarNameStartIndex <= maxEndIndex) {
            char c = ' ';
            OUTTER_FOR_LOOP:
            for (subsVarNameEndIndex = subsVarNameStartIndex; subsVarNameEndIndex <= maxEndIndex; ++subsVarNameEndIndex) {
              c = text.charAt(subsVarNameEndIndex);
              switch (c) {
                case '~':
                case '!':
                case '@':
                case '#':
                case '$':
                case '%':
                case '^':
                case '&':
                case '*':
                case '(':
                case ')':
                //case '_':
                case '+':
                case '`':
                case '-':
                case '=':
                case '{':
                case '}':
                case '|':
                case '[':
                case ']':
                case '\\':
                case ':':
                case '"':
                case ';':
                case '\'':
                case '<':
                case '>':
                case '?':
                case ',':
                case '.':
                case '/':
                case '\t':
                case '\r':
                case '\n':
                case ' ':
                  break OUTTER_FOR_LOOP;
              }
            }

            subsEndIndex = c == '.' ? subsVarNameEndIndex + 1 : subsVarNameEndIndex;
          } else {
            subsVarNameEndIndex = subsVarNameStartIndex;
            subsEndIndex = subsVarNameEndIndex;
          }

          String variableName = text.substring(subsVarNameStartIndex, subsVarNameEndIndex);
          String value = getSubstitutionVariable(variableName);
          if (value == null) {
            value = "";
          }
          buf.append(text, pos, subsStartIndex).append(value);
          pos = subsEndIndex;
        }
        if (pos <= maxEndIndex) {
          buf.append(text, pos, len);
        }

        resultText = buf.toString();
      } else {
        resultText = text;
      }

      return resultText;
    } else {
      return text;
    }
  }

  public void runStatement(String sql, boolean queryable) throws SQLException, IOException {
    System.out.println("------------------");
    System.out.println(sql);
    System.out.println("------------------");
    Statement s = connection.createStatement();
    if (queryable) {
      ResultSet rs = s.executeQuery(sql);

      Exporter.export(getSystemVariable("OUTPUTFORMAT"), outputStream, rs, outputStreamCloseable);
    } else {
      s.executeUpdate(sql);
    }
    s.close();
  }

  public void rollback() throws SQLException {
    connection.rollback();
  }

  public void commit() throws SQLException {
    connection.commit();
  }

  public void runScript(Reader r) throws ParseException {
    SqlParser parser = new SqlParser(new StreamProvider(r));
    parser.parse(this);
  }

  public void rerunCommands() {
    systemVariables.clear();
    substitutionVariables.clear();
    bindVariables.clear();

    for (Command cmd : commands) {
      cmd.execute(this);
    }
  }

  public ResultSet executeQuery(String sql) throws SQLException {
    Statement s = connection.createStatement();
    return s.executeQuery(sql);
  }

  public void addCommand(Command cmd) {
    commands.add(cmd);
    cmd.execute(this);
  }

  public ArrayList<Command> getCommands() {
    return commands;
  }

  private static String getFullSystemVariableName(String name) {
    String fullName = SYSTEM_VARIABLE_NAME_MAP.get(name.toUpperCase());
    return fullName != null ? fullName : name;
  }

  public void setSystemVariable(String name, String value) {
    systemVariables.put(getFullSystemVariableName(name), value);
  }

  public String getSystemVariable(String name) {
    return systemVariables.get(getFullSystemVariableName(name));
  }

  public String unsetSystemVariable(String name) {
    return systemVariables.remove(getFullSystemVariableName(name));
  }

  public void setSubstitutionVariable(String name, String value) {
    substitutionVariables.put(name, value);
  }

  public String getSubstitutionVariable(String name) {
    return substitutionVariables.get(name);
  }

  public String unsetSubstitutionVariable(String name) {
    return substitutionVariables.remove(name);
  }

  public void setBindVariable(String name, String value) {
    bindVariables.put(name, value);
  }

  public String getBindVariable(String name) {
    return bindVariables.get(name);
  }

  public String unsetBindVariable(String name) {
    return bindVariables.remove(name);
  }

  /**
   * @return the connection
   */
  public Connection getConnection() {
    return connection;
  }

  /**
   * @param connection the connection to set
   */
  public void setConnection(Connection connection) {
    this.connection = connection;
  }

  /**
   * @return the outputFileName
   */
  public String getOutputFileName() {
    return outputFileName;
  }

  /**
   * @param outputFileName the outputFileName to set
   */
  public void setOutputFileName(String outputFileName) {
    this.outputFileName = getSubstitutedText(outputFileName);
  }

  /**
   * @return the outputStream
   */
  public OutputStream getOutputStream() {
    return outputStream;
  }

  /**
   * @param outputStream the outputStream to set
   */
  public void setOutputStream(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  /**
   * @return the outputStreamCloseable
   */
  public boolean isOutputStreamCloseable() {
    return outputStreamCloseable;
  }

  /**
   * @param outputStreamCloseable the outputStreamCloseable to set
   */
  public void setOutputStreamCloseable(boolean outputStreamCloseable) {
    this.outputStreamCloseable = outputStreamCloseable;
  }

  /**
   * @return the scannable
   */
  public boolean isScannable() {
    return scannable;
  }

  /**
   * @param scannable the scannable to set
   */
  public void setScannable(boolean scannable) {
    this.scannable = scannable;
  }
}
