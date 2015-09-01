package com.github.tycrelic.sqldisadvantage.io;

import java.io.*;
import java.sql.*;
import java.util.TreeMap;

public abstract class Importer {

  // List of registered Exporters

  private final static TreeMap<String, Importer> registeredImporters = new TreeMap<String, Importer>();

  public static synchronized void registerImporter(String inputFileFormat, Importer importer) throws SQLException {
    registeredImporters.put(inputFileFormat.toLowerCase(), importer);
  }

  public static void importData(String inputFileFormat, InputStream in, PreparedStatement ps, boolean closesInputStream) throws IOException, SQLException {
    Importer importer = registeredImporters.get(inputFileFormat.toLowerCase());
    if (importer != null) {
      importer.importData(in, ps, closesInputStream);
    } else {
      throw new IOException("No importer registered for input file format " + inputFileFormat);
    }
  }

  protected abstract void importData(InputStream in, PreparedStatement ps, boolean closesInputStream) throws IOException, SQLException;
}
