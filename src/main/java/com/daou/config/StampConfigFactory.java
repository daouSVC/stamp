package com.daou.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import org.apache.logging.log4j.Level;

public class StampConfigFactory {
	private static StampConfig STAMP_CONFIG;
	
	private StampConfigFactory() {
	}
	
	public static StampConfig getInstance() throws Exception {
		if (STAMP_CONFIG == null) {
			throw new Exception("Failed to get StampConfig Instance, StampConfigFactory is not Initialized, "
					+ "Must StampConfigFactory Initialized by 'getInstance(String confFileName, StringBuffer initLogMsg)' Method"
					+ " Before call 'getInstance()' Method");
		}

		return getInstance("", null);
	}
	
	public static void reset(String confFileName, StringBuffer LogMsg) throws Exception {
		STAMP_CONFIG = null;
		getInstance(confFileName, LogMsg);
	}
	
	public synchronized static StampConfig getInstance(String confFileName, StringBuffer initLogMsg) throws Exception{

		if (STAMP_CONFIG == null) {
			if (confFileName.equals("")) {
				throw new Exception("Failed to Create StampConfig Instance, ConfigFileName is Not define");
			}
			if (initLogMsg == null) {
				throw new Exception("Failed to Create StampConfig Instance, initLogMsg StringBuffer is null");
			}

			String udsConfName = confFileName + ".conf";

			STAMP_CONFIG = new StampConfigFactory().new StampConfig();
			
			File f = new File(udsConfName);
			
			if(f.isFile()){
				STAMP_CONFIG.getAllProperties(udsConfName);
			}
		}
		
		return STAMP_CONFIG;
	}
	
	public class StampConfig {
		private Hashtable<String, String> config;
					
		private StampConfig(){
			config = new Hashtable<String, String>();
		}
		
		/*
		 *  설정파일에서 환경설정 값을 읽어온다.
		 *  @param getAllProperties 설정파일 이름
		 */
		private void getAllProperties(String fileName) throws Exception {
			if (config == null) {
				
			}
			
			config.put("CONFIGURATION", fileName);
			config.put("CONF_FILE_DATE", StampUtil.getDateStringFromDate(new Date(new File(fileName).lastModified()), "yyyyMMddHHmmss"));			
			
			Properties props = new Properties();
			FileInputStream fileInputStream = new FileInputStream(fileName);
			props.load(fileInputStream);
			fileInputStream.close();
			
			@SuppressWarnings("unchecked")
			Enumeration<String> e = (Enumeration<String>)props.propertyNames();
			String key;
			
			while (e.hasMoreElements()) {
				key = e.nextElement();
				config.put(key, new String(props.getProperty(key).getBytes("ISO-8859-1"), "EUC-KR"));
			}
			
			StampDefine.Referer_List = getREFERER_LIST();
			StampDefine.Test_ip_List = getTEST_IP_LIST();
		}
		
		private Level getLevelFromLevelStr(String levelStr) {
			if ("DEBUG".equalsIgnoreCase(levelStr)) {
				return Level.DEBUG;
			} else if ("INFO".equalsIgnoreCase(levelStr)) {
				return Level.INFO;
			} else if ("WARN".equalsIgnoreCase(levelStr)) {
				return Level.WARN;
			} else if ("ERROR".equalsIgnoreCase(levelStr)) {
				return Level.ERROR;
			} else if ("FATAL".equalsIgnoreCase(levelStr)) {
				return Level.FATAL;
			} else {
				return Level.INFO;
			}
		}
		
		public String getPropertiesValue(String propertiesKey, String defaultValue) {
			String value = config.get(propertiesKey);
			if (value == null) {
				value = (defaultValue != null) ? defaultValue : "";
			}
			value = value.trim();
			
			return value;
		}

		public int getPropertiesValue(String propertiesKey, int defaultValue) {
			String value = config.get(propertiesKey);
			int returnval = 0;
			
			if (value == null) {
				returnval = (defaultValue != 0) ? defaultValue : 0;
			} else {
				value = value.trim();
				returnval = Integer.parseInt(value);
			}
			
			return returnval;
		}
		
		/*FILE_PATH*/
		public String getLOG_PATH() {
			String path = getPropertiesValue("LOG_PATH","./log");
			File pf = new File(path);
			if(!pf.exists()){
				pf.mkdir();
			}
			return path;
		}
		
		public String getFILE_PATH() {
			String path = getPropertiesValue("FILE_PATH","./images");
			File pf = new File(path);
			if(!pf.exists()){
				pf.mkdir();
			}
			return path;
		}
		
		public String getCONF_FILE_DATE() {
			return getPropertiesValue("CONF_FILE_DATE","");
		}

		public String getFILE_DELETE_OPTION() {
			return getPropertiesValue("FILE_DELETE_OPTION","Y");
		}
		
		public String getREFERER_LIST() {
			return getPropertiesValue("REFERER_LIST","");
		}
		
		public String getTEST_IP_LIST() {
			return getPropertiesValue("TEST_IP_LIST","");
		}
		
		public String getALERT_BASE_FILE_PATH() {
			String path = getPropertiesValue("ALERT_BASE_FILE_PATH","");
			File pf = new File(path);
			if(!pf.exists()){
				pf.mkdir();
			}
			return path;
		}
		
		public String getALERT_PPURIO_FILE_PATH() {
			String path = getPropertiesValue("ALERT_PPURIO_FILE_PATH","");
			File pf = new File(path);
			if(!pf.exists()){
				pf.mkdir();
			}
			return path;
		}
		
		public String getALERT_ENFAX_FILE_PATH() {
			String path = getPropertiesValue("ALERT_ENFAX_FILE_PATH","");
			File pf = new File(path);
			if(!pf.exists()){
				pf.mkdir();
			}
			return path;
		}
		
		/*LOG_LEVEL*/
		public Level get_LOG_LEVEL() {
			String value;
			value = getPropertiesValue("LOG_LEVEL",
					StampDefine.DEFAULT_LOG_LEVEL);
			
			return getLevelFromLevelStr(value);
		}
		
		
		/*SERVLET CONTAINER*/
		public String getSERVICE_PORT() {
			return getPropertiesValue("SERVICE_PORT",
					StampDefine.DEFAULT_SERVICE_PORT);
		}
	}
}
