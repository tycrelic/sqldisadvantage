package com.github.tycrelic.sqldisadvantage.io;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class ExcelExporter extends Exporter {

  private static final int MAX_ROW_NUM = 65536;
  private boolean excel2007;

  static {
    try {
      Exporter.registerExporter("excel2007", new ExcelExporter(true));
    } catch (SQLException ex) {
      Logger.getLogger(ExcelExporter.class.getName()).log(Level.SEVERE, null, ex);
    }
    try {
      Exporter.registerExporter("excel", new ExcelExporter(false));
    } catch (SQLException ex) {
      Logger.getLogger(ExcelExporter.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private ExcelExporter(boolean excel2007) {
    this.excel2007 = excel2007;
  }

  public static String getFilterRangeAddress(int numColumns) {
    int quotient = (numColumns + 1) / 27;
    int remainder = quotient <= 0 ? numColumns % 27 : (numColumns + 1) % 27;

    StringBuilder buf = new StringBuilder();
    buf.append("A1:");
    if (quotient > 0) {
      buf.append((char) (65 + quotient - 1));
    }
    buf.append((char) (65 + remainder - 1));
    buf.append("1");
    return buf.toString();
  }

  public void exportData(OutputStream out, ResultSet rs, boolean closesOutputStream) throws IOException, SQLException {
    Workbook wb;
    Sheet sheet;
    if (excel2007) {
      wb = new SXSSFWorkbook();
      ((SXSSFWorkbook) wb).setCompressTempFiles(true);
      sheet = wb.createSheet();
      ((SXSSFSheet) sheet).setRandomAccessWindowSize(100);
    } else {
      wb = new HSSFWorkbook();
      sheet = wb.createSheet();
    }

    CellStyle textCellStyle = wb.createCellStyle(); // "General", see javadoc of org.apache.poi.ss.usermodel.BuiltinFormats
    textCellStyle.setDataFormat((short) 0x31);
    CellStyle generalCellStyle = wb.createCellStyle(); // "text", see javadoc of org.apache.poi.ss.usermodel.BuiltinFormats
    generalCellStyle.setDataFormat((short) 0);
    CellStyle integralCellStyle = wb.createCellStyle(); // "0", see javadoc of org.apache.poi.ss.usermodel.BuiltinFormats
    integralCellStyle.setDataFormat((short) 1);
    CellStyle decimalCellStyle = wb.createCellStyle(); // "0.00", see javadoc of org.apache.poi.ss.usermodel.BuiltinFormats
    decimalCellStyle.setDataFormat((short) 2);

    sheet.createFreezePane(0, 1);

    ResultSetMetaData rsmd = rs.getMetaData();
    int numColumns = rsmd.getColumnCount();
    int[] columCellTypes = new int[numColumns];
    String[] columLabels = new String[numColumns];
    CellStyle[] columCellStyles = new CellStyle[numColumns];
    for (int i = 1; i <= numColumns; ++i) {
      int columnType = rsmd.getColumnType(i);
      switch (columnType) {
        case Types.BIT:
        case Types.BIGINT:
        case Types.INTEGER:
        case Types.TINYINT:
        case Types.SMALLINT:
          columCellTypes[i - 1] = Cell.CELL_TYPE_NUMERIC;
          columCellStyles[i - 1] = integralCellStyle;
          break;
        case Types.DECIMAL:
        case Types.NUMERIC:
          columCellTypes[i - 1] = Cell.CELL_TYPE_NUMERIC;
          columCellStyles[i - 1] = rsmd.getScale(i) != 0 ? decimalCellStyle : integralCellStyle;
          break;
        case Types.DOUBLE:
        case Types.FLOAT:
        case Types.REAL:
          columCellTypes[i - 1] = Cell.CELL_TYPE_NUMERIC;
          columCellStyles[i - 1] = decimalCellStyle;
          break;
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
          columCellTypes[i - 1] = Cell.CELL_TYPE_STRING;
          columCellStyles[i - 1] = textCellStyle;
          break;
        case Types.ARRAY:
        case Types.BINARY:
        case Types.BLOB:
        case Types.BOOLEAN:
        case Types.CHAR:
        case Types.CLOB:
        case Types.DISTINCT:
        case Types.DATALINK:
        case Types.JAVA_OBJECT:
        //case Types.LONGNVARCHAR:
        case Types.LONGVARBINARY:
        case Types.LONGVARCHAR:
        //case Types.NCHAR:
        //case Types.NCLOB:
        case Types.NULL:
        //case Types.NVARCHAR:
        case Types.REF:
        //case Types.ROWID:
        //case Types.SQLXML:
        case Types.VARCHAR:
        case Types.STRUCT:
        case Types.VARBINARY:
        case Types.OTHER:
        default:
          columCellTypes[i - 1] = Cell.CELL_TYPE_STRING;
          columCellStyles[i - 1] = textCellStyle;
          break;
      }
      columLabels[i - 1] = rsmd.getColumnLabel(i);
    }

    sheet.setAutoFilter(CellRangeAddress.valueOf(getFilterRangeAddress(numColumns)));

    int rowNum = 0;
    Row row = sheet.createRow(rowNum++);
    row.createCell(0).setCellValue(columLabels[0]); // export column header
    for (int cellNum = 0; cellNum < numColumns; ++cellNum) {
      row.createCell(cellNum).setCellValue(columLabels[cellNum]); // export column header
    }

    while (rs.next()) {
      // split result to next sheet
      if (rowNum >= MAX_ROW_NUM) {
        sheet = wb.createSheet();
        if (excel2007) {
          ((SXSSFSheet) sheet).setRandomAccessWindowSize(100);
        }
        sheet.createFreezePane(0, 1);
        rowNum = 0;
        row = sheet.createRow(rowNum++);
        // export column header
        for (int cellNum = 0; cellNum < numColumns; ++cellNum) {
          row.createCell(cellNum).setCellValue(columLabels[cellNum]); // export column header
        }
        sheet.setAutoFilter(CellRangeAddress.valueOf(getFilterRangeAddress(numColumns)));
      }

      // export records
      row = sheet.createRow(rowNum++);
      Cell[] cells = new Cell[numColumns];
      for (int cellNum = 0; cellNum < numColumns; ++cellNum) {
        Cell cell = row.createCell(cellNum);
        cell.setCellType(columCellTypes[cellNum]);
        cell.setCellStyle(columCellStyles[cellNum]);
        cells[cellNum] = cell;
      }

      for (int i = 1, cn = 0; i <= numColumns; ++i, ++cn) {
        Object obj = rs.getObject(i);

        if (obj != null) {
          /*if (obj instanceof BigDecimal) {
           cells[cn].setCellValue(((BigDecimal) obj).doubleValue());
           } else if (obj instanceof Double) {
           cells[cn].setCellValue(((Double) obj).doubleValue());
           } else if (obj instanceof Float) {
           cells[cn].setCellValue(((Float) obj).doubleValue());
           } */
          if (obj instanceof Number) {
            //columCellStyles[i - 1] == integralCellStyle;
            cells[cn].setCellValue(((Number) obj).doubleValue());
          } else if (obj instanceof java.sql.Date) {
            cells[cn].setCellValue(new Date(((java.sql.Date) obj).getTime()));
          } else if (obj instanceof Calendar) {
            cells[cn].setCellValue(((Calendar) obj));
          } else if (obj instanceof String) {
            cells[cn].setCellValue(((String) obj));
          } else if (obj instanceof Boolean) {
            cells[cn].setCellValue(((Boolean) obj).booleanValue());
          } else {
            cells[cn].setCellValue(obj.toString());
          }
        }
      }
    }

    wb.write(out);

    if (closesOutputStream) {
      out.close();
    }
  }

}
