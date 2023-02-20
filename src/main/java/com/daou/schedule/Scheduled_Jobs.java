package com.daou.schedule;

import java.io.File;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.daou.config.StampConfigFactory;
import com.daou.config.StampConfigFactory.StampConfig;
import com.daou.config.StampUtil;

@Component
public class Scheduled_Jobs {
		
	@Scheduled(cron = "*/30 * * * * *")
	public void ConfigFileChkAndReset() throws Exception {
		StampConfig conf = StampConfigFactory.getInstance(StampUtil.config_file_name, null);
		
		String ConfFileName = StampUtil.config_file_name + ".conf";
		String lastModified = StampUtil.getDateStringFromDate(new Date(new File(ConfFileName).lastModified()), "yyyyMMddHHmmss");
		
		if(!conf.getCONF_FILE_DATE().equals(lastModified)){
			Logger log = StampUtil.appLogger();
			log.info("Config file is modified !!!");
			StringBuffer resetMsg = new StringBuffer();
			log.info("Config Reset !!");
			StampConfigFactory.reset(StampUtil.config_file_name, resetMsg);
			
			StampConfig config = StampConfigFactory.getInstance();	
			log.info("[INFO] Log File Path = " + config.getLOG_PATH());
			log.info("[INFO] Stamp Image File Path = " + config.getFILE_PATH());
			log.info("[INFO] Service Port = " + config.getSERVICE_PORT());
			log.info("[INFO] Log Level = " + config.get_LOG_LEVEL());
			log.info("[INFO] File Delete Option = " + config.getFILE_DELETE_OPTION());
			log.info("[INFO] Referer List = " + config.getREFERER_LIST());
			log.info("[INFO] Test IP List = " + config.getTEST_IP_LIST());
		}	
	}
}
