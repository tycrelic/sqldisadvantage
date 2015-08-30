package com.github.tycrelic.sqldisadvantage.io;

import java.io.*;
import java.sql.*;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Exporter {

  private final static TreeMap<String, Exporter> registeredExporters = new TreeMap<String, Exporter>();

  public static synchronized void registerExporter(String outputFileFormat, Exporter exporter) throws SQLException {
    registeredExporters.put(outputFileFormat.toLowerCase(), exporter);
  }

  public static void export(String outputFileFormat, OutputStream out, ResultSet rs, boolean closesOutputStream) throws IOException, SQLException {
    Exporter exporter = registeredExporters.get(outputFileFormat.toLowerCase());
    if (exporter != null) {
      exporter.exportData(out, rs, closesOutputStream);
    } else {
      throw new IOException("No exporter registered for output file format " + outputFileFormat);
    }
  }

  protected abstract void exportData(OutputStream out, ResultSet rs, boolean closesOutputStream) throws IOException, SQLException;
}
