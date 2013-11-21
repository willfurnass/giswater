/*
 * This file is part of Giswater
 * Copyright (C) 2013 Tecnics Associats
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Author:
 *   David Erill <daviderill79@gmail.com>
 */
package org.giswater.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.giswater.util.Encryption;
import org.giswater.util.PropertiesMap;
import org.giswater.util.Utils;


public class MainDao {
	
    public static Connection connectionConfig;   // SQLite
    public static Connection connectionDrivers;	   // SQLite 
	public static Connection connectionPostgis;   // Postgis
    public static String schema;
    public static boolean isConnected = false;
	
	public static String folderConfig;	
	public static String fileHelpPath;
	
    private static PropertiesMap iniProperties = new PropertiesMap();
	private static String appPath;	
	private static String configPath;
	
	private static final String CONFIG_FOLDER = "config";
	private static final String CONFIG_FILE = "inp.properties";
	private static final String CONFIG_DB = "config.sqlite";
	
	
    // Sets initial configuration files
    public static boolean configIni() {

    	Utils.getLogger().info("Application started");
    	
    	if (!enabledPropertiesFile()){
    		return false;
    	}
    	
    	// Log SQL?
    	Utils.setSqlLog(iniProperties.get("SQL_LOG", "false"));
    	
        // Get INP folder
        folderConfig = iniProperties.get("FOLDER_CONFIG");
        folderConfig = appPath + folderConfig + File.separator;

    	// Set Config DB connection
        if (!setConnectionConfig(CONFIG_DB)){
        	return false;
        }
        
        // Set Postgis connection
        Boolean connect = Boolean.parseBoolean(iniProperties.get("AUTOCONNECT_POSTGIS"));
        if (connect){
        	silenceConnection();
        }
        
        // Get PDF help file
        if (fileHelpPath == null) {
            fileHelpPath = folderConfig + File.separator + iniProperties.get("FILE_HELP");
        }
        
        return true;

    }
       
    
	public static void silenceConnection(){
		
		String host, port, db, user, password;
		
		// Get parameteres connection from properties file
		host = iniProperties.get("POSTGIS_HOST", "localhost");		
		port = iniProperties.get("POSTGIS_PORT", "5431");
		db = iniProperties.get("POSTGIS_DATABASE", "giswater");
		user = iniProperties.get("POSTGIS_USER", "postgres");
		password = iniProperties.get("POSTGIS_PASSWORD");		
		password = Encryption.decrypt(password);
		password = (password == null) ? "" : password;
		
		if (host.equals("") || port.equals("") || db.equals("") || user.equals("") || password.equals("")){
			Utils.getLogger().info("Autoconnection not possible");
			return;
		}
		isConnected = setConnectionPostgis(host, port, db, user, password);
		if (isConnected){
	    	String folder = MainDao.getDataDirectory();
	    	iniProperties.put("POSTGIS_DATA", folder);
	    	Utils.getLogger().info("Postgis data directory: " + folder);
			Utils.getLogger().info("Autoconnection successful");
		} 
		
	}	    
    
    
	public static void rollback(){
		try {
			connectionPostgis.rollback();
		} catch (SQLException e) {
            Utils.showError(e);
		}
	}	

	
    public static PropertiesMap getPropertiesFile() {
        return iniProperties;
    }


    public static void savePropertiesFile() {

        File iniFile = new File(configPath);
        try {
        	FileOutputStream fos = new FileOutputStream(iniFile);
            iniProperties.store(fos, true);
        } catch (FileNotFoundException e) {
            Utils.showError("inp_error_notfound", iniFile.getPath());
        } catch (IOException e) {
            Utils.showError("inp_error_io", iniFile.getPath());
        }

    }
    
    
    // Get Properties Files
    public static boolean enabledPropertiesFile() {

    	appPath = Utils.getAppPath();
        configPath = appPath + CONFIG_FOLDER + File.separator + CONFIG_FILE;
        File fileIni = new File(configPath);
        try {
        	iniProperties.load(new FileInputStream(fileIni));      
        } catch (FileNotFoundException e) {
            Utils.showError("inp_error_notfound", configPath);
            return false;
        } catch (IOException e) {
            Utils.showError("inp_error_io", configPath);
            return false;
        }
        return (iniProperties != null);

    }    
	
    
    // Connect to Config sqlite Database
    public static boolean setConnectionConfig(String fileName) {

        try {
            Class.forName("org.sqlite.JDBC");
            String filePath = folderConfig + fileName;
            File file = new File(filePath);
            if (file.exists()) {
            	connectionConfig = DriverManager.getConnection("jdbc:sqlite:" + filePath);
                return true;
            } else {
                Utils.showError("inp_error_notfound", filePath);
                return false;
            }
        } catch (SQLException e) {
            Utils.showError("inp_error_connection", e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            Utils.showError("inp_error_connection", "ClassNotFoundException");
            return false;
        }

    }
    
    
    // Connect to sqlite drivers database
    public static boolean setConnectionDrivers(String fileName) {

        try {
            Class.forName("org.sqlite.JDBC");
            String filePath = folderConfig + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                connectionDrivers = DriverManager.getConnection("jdbc:sqlite:" + filePath);
                return true;
            } else {
                Utils.showError("inp_error_notfound", filePath);
                return false;
            }
        } catch (SQLException e) {
            Utils.showError("inp_error_connection", e.getMessage());
            return false;
        } catch (ClassNotFoundException e) {
            Utils.showError("inp_error_connection", "ClassNotFoundException");
            return false;
        }

    }  
    
    
	public static String getSoftwareVersion(String type, String id) {
		
        String sql = "SELECT software_version FROM "+type+"_software WHERE id = '"+id+"'";
        String result = "";
        try {
            Statement stat = connectionConfig.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            if (rs.next()) {
            	 result = rs.getString(1);
            }
            rs.close();
            stat.close();
        } catch (SQLException e) {
        	Utils.showError(e, sql);
        }
        return result;   
        
	}
	
	
    public static boolean setConnectionPostgis(String host, String port, String db, String user, String password) {
    	
        String connectionString = "jdbc:postgresql://"+host+":"+port+"/"+db+"?user="+user+"&password="+ password;
        try {
            connectionPostgis = DriverManager.getConnection(connectionString);
        } catch (SQLException e) {
            try {
                connectionPostgis = DriverManager.getConnection(connectionString);
            } catch (SQLException e1) {
                Utils.showError(e1);
                return false;
            }   		
        }
        return true;
        
    }	
    
    
    public static void closeConnectionPostgis(){
    	try {
			connectionPostgis.close();
			isConnected = false;
		} catch (SQLException e) {
            Utils.showError(e);
		}
    }

    
	public static boolean executeUpdateSql(String sql) {
		return executeUpdateSql(sql, false);
	}	
	
    
	public static boolean executeUpdateSql(String sql, boolean commit) {
		try {
			Statement ps = connectionPostgis.createStatement();
	        ps.executeUpdate(sql);
	        if (commit && !connectionPostgis.getAutoCommit()){
	        	connectionPostgis.commit();
	        }
			return true;
		} catch (SQLException e) {
			Utils.showError(e, sql);
			return false;
		}
	}	
	
	
	public static boolean executeSql(String sql) {
		return executeSql(sql, false);
	}	
	
	
	public static boolean executeSql(String sql, boolean commit) {
		try {
			Statement ps = connectionPostgis.createStatement();
	        ps.execute(sql);
			if (commit && !connectionPostgis.getAutoCommit()){
	        	connectionPostgis.commit();
	        }			
			return true;
		} catch (SQLException e) {
			Utils.showError(e, sql);
			return false;
		}
	}		
	
	
    // Check if the table exists
	public static boolean checkTable(String tableName) {
        String sql = "SELECT * FROM pg_tables" +
        	" WHERE lower(tablename) = '" + tableName + "'";
        try {
            Statement stat = connectionPostgis.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            return (rs.next());
        } catch (SQLException e) {
        	Utils.showError(e);
            return false;
        }
    }
	
	
    // Check if the table exists
	public static boolean checkTable(String schemaName, String tableName) {
        String sql = "SELECT * FROM pg_tables" +
        	" WHERE lower(schemaname) = '"+schemaName+"' AND lower(tablename) = '"+tableName+"'";
        try {
            Statement stat = connectionPostgis.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            return (rs.next());
        } catch (SQLException e) {
        	Utils.showError(e, sql);
            return false;
        }
    }	
    
    
    // Check if the view exists
    public static boolean checkView(String viewName) {
        String sql = "SELECT * FROM pg_views" +
        	" WHERE lower(viewname) = '"+viewName+"'";
        try {
            Statement stat = connectionPostgis.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            return (rs.next());
        } catch (SQLException e) {
        	Utils.showError(e, sql);
            return false;
        }
    }    
    
    
    // Check if the view exists
    public static boolean checkView(String schemaName, String viewName) {
        String sql = "SELECT * FROM pg_views" +
        	" WHERE lower(schemaname) = '"+schemaName+"' AND lower(viewname) = '"+viewName+"'";
        try {
            Statement stat = connectionPostgis.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            return (rs.next());
        } catch (SQLException e) {
        	Utils.showError(e, sql);
            return false;
        }
    }        
    
    
    // Check if the selected srid exists in spatial_ref_sys
	public static boolean checkSrid(Integer srid) {
        String sql = "SELECT srid FROM spatial_ref_sys WHERE srid = "+srid;
        try {
            Statement stat = connectionPostgis.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            return (rs.next());
        } catch (SQLException e) {
        	Utils.showError(e, sql);
            return false;
        }
    }    
    
    
	public static Vector<String> getSchemas(){

        String sql = "SELECT schema_name FROM information_schema.schemata " +
        	"WHERE schema_name <> 'information_schema' AND schema_name !~ E'^pg_' " +
        	"AND schema_name <> 'drivers' AND schema_name <> 'public' AND schema_name <> 'topology' " +
        	"ORDER BY schema_name";
        Vector<String> vector = new Vector<String>();
        try {
    		connectionPostgis.setAutoCommit(false);        	
            Statement stat = connectionPostgis.createStatement();
            ResultSet rs = stat.executeQuery(sql);
            while (rs.next()) {
            	vector.add(rs.getString(1));
            }
            rs.close();
    		return vector;	            
        } catch (SQLException e) {
            Utils.showError(e, sql);
            return vector;
        }
		
	}
	
	
	private static ResultSet getResultset(Connection connection, String sql){
		
        ResultSet rs = null;        
        try {
        	connection.setAutoCommit(true);
        	Statement stat = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stat.executeQuery(sql);
        } catch (SQLException e) {
        	Utils.showError(e, sql);
        }
        return rs;   
        
	}

	
	private static ResultSet getResultset(String sql){
		return getResultset(connectionPostgis, sql);
	}
	
	
	public static ResultSet getTableResultset(Connection connection, String table) {
		String sql;
		if (schema == null){
			sql = "SELECT * FROM "+table;
		} else{
			sql = "SELECT * FROM "+schema+"."+table;
		}
		//Utils.logSql(sql);
        return getResultset(connection, sql);
	}
	
	public static ResultSet getTableResultset(String table) {
		return getTableResultset(connectionPostgis, table);
	}
	
	
	public static ResultSet getRaingageResultset(String table) {
        String sql = "SELECT rg_id, form_type, intvl, scf, rgage_type, timser_id, fname, sta, units" +
        	" FROM "+schema+"." + table;
        return getResultset(sql);
	}	

	
    public static Vector<String> getAvailableVersions(String type, String software){

        Vector<String> vector = new Vector<String>();
        String sql = "SELECT id" +
        	" FROM "+type+"_software" +  
        	" WHERE available = 1 AND software_name = '"+software+"'" +
        	" ORDER BY id DESC";            
		try {
			Statement stat = connectionConfig.createStatement();
	        ResultSet rs = stat.executeQuery(sql);
	        while (rs.next()) {
	        	vector.add(rs.getString("id"));
	        }
	        rs.close();   
            stat.close();	        
		} catch (SQLException e) {
            Utils.showError(e, sql);
		}            
		return vector;
    	
    }


	public static Vector<String> getTable(String table, String schemaParam, boolean addBlank) {
        
        Vector<String> vector = new Vector<String>();
        
        if (addBlank){
        	vector.add("");
        }
		if (schemaParam == null){
			schemaParam = schema;
		}
		if (!checkTable(schemaParam, table)) {
			return vector;
		}
		String sql = "SELECT * FROM "+schemaParam+"."+table;
		try {
			Statement stat = connectionPostgis.createStatement();
	        ResultSet rs = stat.executeQuery(sql);
	        while (rs.next()) {
	        	vector.add(rs.getString(1));
	        }
	        stat.close();
		} catch (SQLException e) {
            Utils.showError(e, sql);
		}            
		return vector;
		
	}	
	
	
	public static Vector<String> getTable(String table, String schemaParam) {
		return getTable(table, schemaParam, false);
	}
	
	
	public static void setResultSelect(String schema, String table, String result) {
		String sql = "DELETE FROM "+schema+"."+table;
		Utils.logSql(sql);
		executeUpdateSql(sql);
		sql = "INSERT INTO "+schema+"."+table+" VALUES ('"+result+"')";
		executeUpdateSql(sql);
		Utils.logSql(sql);
	}
	
	
	public static String getSchema() {
		return schema;
	}
	
	
	public static void setSchema(String schema) {
		MainDao.schema = schema;
	}
	

	public static void deleteSchema(String schemaName) {
		String sql = "DROP schema IF EXISTS "+schemaName+" CASCADE;";
		executeUpdateSql(sql, true);		
		sql = "DELETE FROM public.geometry_columns WHERE f_table_schema = '"+schemaName+"'";
		executeUpdateSql(sql, true);			
	}

	
	private static String readFile(String filePath) throws IOException{
		
		File fileName = new File(filePath);			
		if (!fileName.exists()){
			Utils.showError("inp_error_notfound", filePath);
			return "";
		}
		RandomAccessFile rat = new RandomAccessFile(fileName, "r");
		String line;
		String content = "";
		while ((line = rat.readLine()) != null){
			content += line + "\n";
		}
		rat.close();		
		
		return content;
		
	}

	
	public static void createSchema(String softwareName, String schemaName, String srid) {
		
		String sql = "CREATE schema "+schemaName;
		if (!executeUpdateSql(sql, true)){
			rollback();
			return;	
		}
		String filePath = "";
		String content = "";
    	
		try {

	    	String folderRoot = new File(".").getCanonicalPath() + File.separator;			
			filePath = folderRoot + "sql/"+softwareName+".sql";
	    	content = readFile(filePath);
			
	    	// Replace SCHEMA_NAME for schemaName parameter. SRID_VALUE for srid parameter
			content = content.replace("SCHEMA_NAME", schemaName);
			content = content.replace("SRID_VALUE", srid);
			Utils.logSql(content);
			
			if (executeSql(content, true)){
			
				filePath = folderRoot + "sql/"+softwareName+"_value_domain.sql";
		    	content = readFile(filePath);
				content = content.replace("SCHEMA_NAME", schemaName);		   
				Utils.logSql(content);
		    	
				if (executeUpdateSql(content, true)){
					filePath = folderRoot + "sql/"+softwareName+"_functrigger.sql";
			    	content = readFile(filePath);
					content = content.replace("SCHEMA_NAME", schemaName);
					Utils.logSql(content);
					if (executeUpdateSql(content, true)){
						Utils.showMessage("schema_creation_completed", "");								
					}
				}
		    	
			}
			
        } catch (FileNotFoundException e) {
            Utils.showError("inp_error_notfound", filePath);
        } catch (IOException e) {
            Utils.showError(e, filePath);
        }
		
	}

	
	// hecRas functions
	public static boolean createSdfFile(String fileName) {
		String sql = "SELECT gisras.gr_export_geo('"+fileName+"');";
		Utils.logSql(sql);
		return executeSql(sql, true);	
	}
	
	
	public static boolean clearData(){
		String sql = "SELECT gisras.gr_clear();";
		Utils.logSql(sql);
		return executeSql(sql, true);	
	}


	public static boolean saveCase(String schemaName) {
		String sql = "SELECT gisras.gr_save_case_as('"+schemaName+"');";
		Utils.logSql(sql);
		return executeSql(sql, true);	
	}


	public static boolean loadCase(String schemaName) {
		String sql = "SELECT gisras.gr_open_case('"+schemaName+"');";
		Utils.logSql(sql);
		return executeSql(sql, true);	
	}


	public static boolean deleteCase(String schemaName) {
		String sql = "SELECT gisras.gr_delete_case('"+schemaName+"');";
		Utils.logSql(sql);
		return executeSql(sql, true);	
	}


	public static String getDataDirectory() {
		
		String sql = "SELECT setting FROM pg_settings WHERE name = 'data_directory'";
		String folder = "";
		try {
			Statement stat = connectionPostgis.createStatement();
	        ResultSet rs = stat.executeQuery(sql);
	        if (rs.next()) {
	        	folder = rs.getString(1);
	        }
	        stat.close();
		} catch (SQLException e) {
            Utils.showError(e, sql);
		}    		
		return folder;
		
	}


	public static boolean loadRaster(String raster) {

		String fileSql, aux, logFolder;
		String bin, host, port, db, user;
		
		fileSql = raster.replace(".asc", ".sql");
		bin = iniProperties.getProperty("FOLDER_BIN", "");
		host = iniProperties.getProperty("POSTGIS_HOST", "localhost");
		port = iniProperties.getProperty("POSTGIS_PORT", "5431");
		db = iniProperties.getProperty("POSTGIS_DATABASE", "giswater");
		user = iniProperties.getProperty("POSTGIS_USER", "postgres");
		logFolder = Utils.getLogFolder();
		
		File file = new File(bin);
		if (!file.exists()){
			Utils.showError("Postgis bin folder not exists:", bin);
			return false;			
		}
		bin+= File.separator;
		
		// Set content of .bat file
		aux =  "\"" + bin + "raster2pgsql\" -d -s 0 -I -C -M " + raster + " -F -t 100x100 gisras.mdt > " + fileSql;
		aux+= "\n";
		aux+= "\"" + bin + "psql\" -U " + user + " -h " + host + " -p " + port + " -d " + db +
				" -c \"drop table if exists gisras.mdt\";";
		aux+= "\n";		
		aux+= "\"" + bin + "psql\" -U " + user + " -h " + host + " -p " + port + " -d " + db + 
			" -f " + fileSql + " > " + logFolder + "raster2pgsql.log";
		aux+= "\ndel " + fileSql;
		aux+= "\nexit";		
		Utils.getLogger().info(aux);

        // Fill and execute .bat File	
		File batFile = new File(logFolder + "raster2pgsql.bat");        
		Utils.fillFile(batFile, aux);    		
		Utils.openFile(batFile.getAbsolutePath());
		
		return true;
			
	}	
	

}