package com.daou.log;

import java.io.File;
import java.util.zip.Deflater;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import com.daou.config.StampConfigFactory.StampConfig;


public class StampLogFactory {
	private static StampLogFactory STAMP_LOG_INSTANCE = null;
	private final static StringBuffer INIT_LOG_BUFF = new StringBuffer();
	private String logRoot;
	private LoggerContext ctx;
	private Configuration logConfig;
	
	private final String stamp_log_name = "stamp_log";
	private final String stamp_test_log_name = "stamp_test_log";
	private Logger stamp_logger;
	private Logger stamp_test_logger;
	
	private StampLogFactory(StampConfig conf) throws Exception {
		setLogProp(conf);
	}
	
	public synchronized static StampLogFactory getInstance(StampConfig conf) throws Exception {
		
		if (STAMP_LOG_INSTANCE == null) {
			if (conf == null) {
				throw new Exception("Failed to Create StampLogFactory Instance, StampConfig Parameter is null");
			}
			STAMP_LOG_INSTANCE = new StampLogFactory(conf);
		}
		
		return STAMP_LOG_INSTANCE;
	}
	
	public static StringBuffer getInitLogBuff() {
		return INIT_LOG_BUFF;
	}
	
	public Logger getStampLogger() {
		return stamp_logger;
	}
	
	public Logger getStampTestLogger() {
		return stamp_test_logger;
	}
	
	public void writeInitLog() {
		stamp_logger.info(INIT_LOG_BUFF.toString());
		INIT_LOG_BUFF.delete(0, INIT_LOG_BUFF.length());
	}
	
	private void setLogProp(StampConfig conf) throws Exception {
		
		/*SetContext*/
		ctx = (LoggerContext) LogManager.getContext(false);
		
		/*Get BaseConfiguration*/
		logConfig = ctx.getConfiguration();
				
		/*SetLogRoot PATH*/
		logRoot = conf.getLOG_PATH();
		
		/*Add StartUp Logger*/
		addLogger(stamp_log_name, logConfig, conf.get_LOG_LEVEL());
		stamp_logger = LogManager.getLogger(stamp_log_name);
		
		addLogger(stamp_test_log_name, logConfig, conf.get_LOG_LEVEL());
		stamp_test_logger = LogManager.getLogger(stamp_test_log_name);
		
		ctx.updateLoggers();
	}
	
	private void addLogger(String logFileName, Configuration logConfig, Level logLevel) throws Exception {
		if (!isValidLogLevel(logLevel)) {
			throw new Exception ("Failed to setLogProp, is Not Valid Log LEVEL");
		}
		
		String logFilePath = logRoot + File.separator + logFileName;
		
		/*Create PatternLayout*/
		PatternLayout pl = PatternLayout.newBuilder()
				.withPattern(getLayout(logLevel))
				.build();

		/*Create Policy*/		
		TimeBasedTriggeringPolicy timeBasePolicy = TimeBasedTriggeringPolicy.newBuilder().withInterval(1).withModulate(true).build();
		
		
		/*Create Strategy*/
		DefaultRolloverStrategy strategy = DefaultRolloverStrategy.newBuilder()
											.withMax("7")
											.withMin("1")
											.withCompressionLevelStr(String.valueOf(Deflater.DEFAULT_COMPRESSION))
											.withConfig(logConfig)
											.build();
				
		/*Create Appender*/
		Appender apd = RollingFileAppender.createAppender(logFilePath,
														getDatePattern(logFilePath),
														"true",
														logFileName,
														"true",
														"8192",
														"true", 
														timeBasePolicy, 
														strategy, 
														pl,
														null,
														"true",
														"false",
														null,
														logConfig);
		apd.start();
		logConfig.addAppender(apd);
		
		/*Create AppenderRef*/
		AppenderRef fileRef = AppenderRef.createAppenderRef(logFileName, logLevel, null);
		AppenderRef[] refs = new AppenderRef[] {fileRef};
				
		LoggerConfig loggerConfig = LoggerConfig.createLogger("false", logLevel, logFileName, "true", refs, null, logConfig, null);
		loggerConfig.addAppender(apd, logLevel, null);
		logConfig.addLogger(logFileName, loggerConfig);
	}
	
	private boolean isValidLogLevel(Level logLevel) {
		boolean isValid = false;
		
		if (logLevel == Level.INFO) {
			isValid = true;
		} else if (logLevel == Level.DEBUG) {
			isValid = true;
		} else if (logLevel == Level.WARN) {
			isValid = true;
		} else if (logLevel == Level.ERROR) {
			isValid = true;
		} else if (logLevel == Level.FATAL) {
			isValid = true;
		}
		
		return isValid;
	}
	
	private String getLayout(Level lev) {
		if (lev == Level.DEBUG) {
			return "%d{MM/dd HH:mm:ss} [%t] %p : %C. %M(%F:%L) - %m%n";
		} else {
			return "%d{MM/dd HH:mm:ss} [%t] : %m%n";
		}
	}
	
	private String getDatePattern(String fileName) {
		return fileName + ".%d{yyyyMMdd}";
	}
}
