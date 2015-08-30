package com.github.tycrelic.sqldisadvantage.io;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class DataStreamImporter extends Importer {

  private boolean gzipped;

  static {
    try {
      Importer.registerImporter("datastream", new DataStreamImporter(false));
      Importer.registerImporter("datastream_gz", new DataStreamImporter(true));
    } catch (SQLException ex) {
      Logger.getLogger(ExcelExporter.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private DataStreamImporter(boolean gzipped) {
    this.gzipped = gzipped;
  }

  public void importData(InputStream in, PreparedStatement ps, boolean closesInputStream) throws IOException, SQLException {
    if (gzipped) {
      in = new GZIPInputStream(in);
    }
    DataInputStream dis = new DataInputStream(in);

    ParameterMetaData pmd = ps.getParameterMetaData();
    int numColumns = pmd.getParameterCount();
    String[] columLabels = new String[numColumns];
    int[] columCellStyles = new int[numColumns];
    int[] columNullabilities = new int[numColumns];
    String[] columnClassNames = new String[numColumns];

    final int integralCellStyle = 1;
    final int decimalCellStyle = 2;
    final int textCellStyle = 3;
    final int dateCellStyle = 4;

    for (int i = 1; i <= numColumns; ++i) {
      columnClassNames[i - 1] = pmd.getParameterClassName(i);
    }

    try {
      while (true) {
        // export records
        for (int i = 1, cn = 0; i <= numColumns; ++i, ++cn) {
          String className = columnClassNames[i - 1];

          if (className != null) {
            if (className.equals("java.lang.Integer")) {
              ps.setInt(i, dis.readInt());
            } else if (className.equals("java.lang.Long")) {
              ps.setLong(i, dis.readLong());
            } else if (className.equals("java.lang.Short")) {
              ps.setShort(i, dis.readShort());
            } else if (className.equals("java.lang.Byte")) {
              ps.setByte(i, dis.readByte());
            } else if (className.equals("java.lang.BigDecimal")) {
              ps.setBigDecimal(i, BigDecimal.valueOf(dis.readDouble()));
            } else if (className.equals("java.lang.Double")) {
              ps.setDouble(i, dis.readDouble());
            } else if (className.equals("java.lang.Float")) {
              ps.setFloat(i, dis.readFloat());
            } else if (className.equals("java.lang.java.sql.Date")) {
              ps.setDate(i, new java.sql.Date(dis.readLong()));
            } else if (className.equals("java.lang.java.sql.Timestamp")) {
              ps.setTimestamp(i, new java.sql.Timestamp(dis.readLong()));
            } else if (className.equals("java.lang.Calendar")) {
              ps.setTimestamp(i, new java.sql.Timestamp(dis.readLong()));
            } else if (className.equals("java.lang.String")) {
              ps.setString(i, dis.readUTF());
            } else if (className.equals("java.lang.Boolean")) {
              ps.setBoolean(i, dis.readBoolean());
            } else {
              throw new IOException("No read method for " + className + " " + i);
            }
          }
        }
      }
    } catch (EOFException e) {
      //ignored
    }

    if (closesInputStream) {
      dis.close();
    }
  }

}
