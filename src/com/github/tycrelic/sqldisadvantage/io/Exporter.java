package com.github.tycrelic.sqldisadvantage.io;

import java.io.*;
import java.sql.*;
import java.util.TreeMap;

public abstract class Exporter {

  // List of registered Exporters

  private final static TreeMap<String, Exporter> registeredExporters = new TreeMap<String, Exporter>();

  public static synchronized void registerExporter(String outputFileFormat, Exporter exporter) throws SQLException {
    registeredExporters.put(outputFileFormat.toLowerCase(), exporter);
  }

  public static void export(String outputFileFormat, OutputStream out, ResultSet rs, boolean closesOutputStream) throws IOException, SQLException {
    Exporter exporter = registeredExporters.get(outputFileFormat.toLowerCase());
    if (exporter != null) {
      exporter.export(out, rs, closesOutputStream);
    } else {
      throw new IOException("No exporter registered for output file format " + outputFileFormat);
    }
  }

  protected abstract void export(OutputStream out, ResultSet rs, boolean closesOutputStream) throws IOException, SQLException;
}
