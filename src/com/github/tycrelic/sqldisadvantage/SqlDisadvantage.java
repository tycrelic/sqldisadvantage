package com.github.tycrelic.sqldisadvantage;

import com.github.tycrelic.sqldisadvantage.io.Exporter;
import com.github.tycrelic.sqldisadvantage.parser.ClientEnvironment;
import com.github.tycrelic.sqldisadvantage.parser.ParseException;
import java.io.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SqlDisadvantage {

  static {
    try {
      Class.forName("com.github.tycrelic.sqldisadvantage.io.ExcelExporter");
      Class.forName("com.github.tycrelic.sqldisadvantage.io.DataStreamExporter");
    }
    // List of registered Exporters
    catch (ClassNotFoundException ex) {
      Logger.getLogger(Exporter.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
 
  public static void main(String[] args) throws SQLException, IOException, ParseException, ClassNotFoundException {
    if (args.length < 3) {
      System.err.println("Not enough parameters");
      System.err.println("usage: " + SqlDisadvantage.class.getName() + " driverName connectionString sqlFile [sqlArgs ...]");
      System.exit(-1);
    }
    String driverName = args[0];
    String connectionString = args[1];
    String sqlFile = args[2];
    String[] sqlArgs = new String[args.length - 3];
    System.arraycopy(args, 3, sqlArgs, 0, sqlArgs.length);

    process(driverName, connectionString, sqlFile, sqlArgs);
  }

  static void process(String driverName, String connectionString, String sqlFile, String[] sqlArgs) throws SQLException, IOException, ParseException, ClassNotFoundException {
    BufferedReader br = new BufferedReader(new FileReader(sqlFile));

    ClientEnvironment env = new ClientEnvironment();
    env.setArguements(sqlArgs);
    env.connect(driverName, connectionString);
    env.runScript(br);
    env.disconnect();
  }
}
