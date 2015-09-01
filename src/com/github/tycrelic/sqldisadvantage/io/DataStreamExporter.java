package com.github.tycrelic.sqldisadvantage.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class DataStreamExporter extends Exporter {

  private boolean gzipped;

  static {
    try {
      Exporter.registerExporter("datastream", new DataStreamExporter(false));
      Exporter.registerExporter("datastream_gz", new DataStreamExporter(true));
    } catch (SQLException ex) {
      Logger.getLogger(ExcelExporter.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private DataStreamExporter(boolean gzipped) {
    this.gzipped = gzipped;
  }

  public void exportData(OutputStream out, ResultSet rs, boolean closesOutputStream) throws IOException, SQLException {
    if (gzipped) {
      out = new GZIPOutputStream(out);
    }
    DataOutputStream dos = new DataOutputStream(out);

    ResultSetMetaData rsmd = rs.getMetaData();
    int numColumns = rsmd.getColumnCount();
    String[] columLabels = new String[numColumns];
    int[] columNullabilities = new int[numColumns];

    dos.writeInt(numColumns);
    for (int i = 1; i <= numColumns; ++i) {
      columLabels[i - 1] = rsmd.getColumnLabel(i);
      columNullabilities[i - 1] = rsmd.isNullable(i);

      dos.writeUTF(columLabels[i - 1]);
      dos.writeUTF(rsmd.getColumnClassName(i));
      dos.writeInt(rsmd.getColumnType(i));
      dos.writeUTF(rsmd.getColumnTypeName(i));
      dos.writeInt(rsmd.getColumnDisplaySize(i));
      dos.writeInt(rsmd.getPrecision(i));
      dos.writeInt(rsmd.getScale(i));
      dos.writeInt(columNullabilities[i - 1]);
    }

    while (rs.next()) {
      // export records
      for (int i = 1, cn = 0; i <= numColumns; ++i, ++cn) {
        Object obj = rs.getObject(i);

        int nullability = columNullabilities[i - 1];
        if (obj == null && nullability == ResultSetMetaData.columnNoNulls) {
          throw new IOException("Not expected null " + columLabels[i - 1] + " " + i);
        } else {
          boolean expectedNullValue = false;

          if(nullability != ResultSetMetaData.columnNoNulls) {
            expectedNullValue = obj == null;
            //dos.writeInt(expectedNullValue ? 0 : 1);
            //dos.writeByte(expectedNullValue ? 0 : 1);
            dos.writeBoolean(expectedNullValue);
          }
          
          if(expectedNullValue) {
            // do nothing
          }
          else if (obj instanceof Integer) {
            dos.writeInt(((Integer) obj).intValue());
          } else if (obj instanceof Long) {
            dos.writeLong(((Long) obj).longValue());
          } else if (obj instanceof Short) {
            dos.writeShort(((Long) obj).shortValue());
          } else if (obj instanceof Byte) {
            dos.writeByte(((Byte) obj).byteValue());
          } else if (obj instanceof BigDecimal) {
            dos.writeDouble(((BigDecimal) obj).doubleValue());
          } else if (obj instanceof Double) {
            dos.writeDouble(((Double) obj).doubleValue());
          } else if (obj instanceof Float) {
            dos.writeFloat(((Float) obj).floatValue());
          } else if (obj instanceof java.sql.Date) {
            dos.writeLong(((java.sql.Date) obj).getTime());
          } else if (obj instanceof java.sql.Timestamp) {
            dos.writeLong(((java.sql.Timestamp) obj).getTime());
          } else if (obj instanceof Calendar) {
            dos.writeLong(((Calendar) obj).getTimeInMillis());
          } else if (obj instanceof String) {
            dos.writeUTF(((String) obj));
          } else if (obj instanceof Boolean) {
            dos.writeBoolean(((Boolean) obj).booleanValue());
          } else {
            throw new IOException("No write method for " + obj.getClass().getName() + " " + obj.toString());
          }
        }
      }
    }

    dos.flush();
    if (gzipped) {
      ((GZIPOutputStream) out).finish();
    }
    if (closesOutputStream) {
      dos.close();
    }
  }

}
