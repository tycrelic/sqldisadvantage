# sqldisadvantage (Alpha)
A command line SQL script runner written in Java for exporting Excel files currently.
It is still under development.
It will probably support a subset of Oracle SQL*Plus commands PL/SQL with some extensions.

# Depending libraries
1. POI and its depending :
   https://poi.apache.org/download.html

2. JDBC drivers of the target databases

3. JavaCC (optional, for compiling com/github/tycrelic/sqldisadvantage/parser/SqlParser.jj only):
   https://java.net/projects/javacc/downloads/directory/releases/Release%206.1.2

## Usage
The currently only useful usage is to generate simple Excel reports, e.g., in a Windows command prompt,
```
set sqldis_lib=_the_path_of_the_project_folder_\lib
set CLASSPATH=_the_path_of_the_compiled_classes_;%sqldis_lib%\commons-codec-1.5.jar;%sqldis_lib%\commons-logging-1.1.jar;%sqldis_lib%\junit-4.11.jar;%sqldis_lib%\log4j-1.2.13.jar;%sqldis_lib%\ojdbc6.jar;%sqldis_lib%\poi-3.12-20150511.jar;%sqldis_lib%\poi-ooxml-3.12-20150511.jar;%sqldis_lib%\poi-ooxml-schemas-3.12-20150511.jar;%sqldis_lib%\xmlbeans-2.6.0.jar
java com.github.tycrelic.sqldisadvantage.SqlDisadvantage oracle.jdbc.OracleDriver "jdbc:oracle:thin:user/password@db" test.sql 
```

And the test.sql looks like:
```
set outputformat excel2007
spool 'a.xlsx'
select *
from abc
where a = b;
spool off

set outputformat excel
spool 'b.xlsx'
select *
from abc
where a = b;
spool off
```

## Compiling 
For instance, in a Windows command prompt,
```
set CLASSPATH=_somewhere_\javacc-6.1.2.jar;%CLASSPATH%
d:
cd _the_path_of_the_project_folder_\src\com\github\tycrelic\sqldisadvantage\parser
del TokenMgrError.java
del Token.java
del ParseException.java
del SimpleCharStream.java
java javacc SqlParser.jj
```
