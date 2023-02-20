package com.daou;

import javax.annotation.PreDestroy;

import org.joda.time.DateTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.daou.config.StampConfigFactory;
import com.daou.config.StampConfigFactory.StampConfig;
import com.daou.config.StampUtil;
import com.daou.log.StampLogFactory;


@SpringBootApplication
@EnableScheduling
public class JMakeStampApplication {

    public static void main(String[] args) {
    	if (1 > args.length)
		{
    		args = new String[1];
    		args[0] = "C:\\STS_Workspace\\j_make_stamp\\output\\config\\stamp";
    		
			//System.out.println("Usage : java -jar j_make_stamp-1.0.0.1.war config/stamp");
			//System.exit(0);
		}
    	
    	StampUtil.config_file_name = args[0];
		
		try {
			initService(StampUtil.config_file_name );
		} catch (Exception e) {
			if (StampLogFactory.getInitLogBuff().length() > 0) {
					System.out.println(StampLogFactory.getInitLogBuff().toString());
			}
			System.out.println("[ERROR]Failed to Start Service ERR_MSG=" + e.getMessage());
		}
		
		/*SpringApplication에 전달되는 argument가 Listener로 등록 되기에 설정파일 Argument를 삭제한다.*/
		args[0] = "";
		
    	SpringApplication myApp = new SpringApplication(JMakeStampApplication.class);
    	
    	myApp.setShowBanner(false);
    	
    	myApp.run(args);
    }
            
    private static void initService(String bizConfigFileName) throws Exception {
    	StampConfig config;
    	try {
    		config = StampConfigFactory.getInstance(bizConfigFileName, StampLogFactory.getInitLogBuff());
    	} catch (Exception e) {
    		throw new Exception ("Failed to Create BizConfig Exception, ErrMsg = " + e.getMessage());
    	}

    	/* SpringApplicaion이 실행된 후에는 Context가 변경되기 때문에 시작시점에는 Log Buffer를 사용
    	 * SpringApplication 구동 시점에서 Context에 대한 Logger 설정을 한다.*/
    	StampLogFactory.getInitLogBuff().append("[OK] Stamp Image Creator START").append(StampUtil.NEW_LINE);
    	StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] ConfigFileName = ").append(bizConfigFileName).append(StampUtil.NEW_LINE);
    	StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[SYSTEM TIME]:").append(StampUtil.getDateForLog()).append(StampUtil.NEW_LINE);
				
		
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t========= Service Start =========").append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] [OK]createStartLogger").append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append(StampUtil.NEW_LINE);
		/*Set Log For BizConfiguration*/
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t========= Application Configuration LOG =========").append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] Log File Path = ").append(config.getLOG_PATH()).append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] Stamp Image File Path = ").append(config.getFILE_PATH()).append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] Service Port = ").append(config.getSERVICE_PORT()).append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] Log Level = ").append(config.get_LOG_LEVEL()).append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] File Delete Option = ").append(config.getFILE_DELETE_OPTION()).append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] Alert Base File Path = ").append(config.getALERT_BASE_FILE_PATH()).append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] Alert enfax Msg File Path = ").append(config.getALERT_ENFAX_FILE_PATH()).append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] Alert ppurio Msg File Path = ").append(config.getALERT_PPURIO_FILE_PATH()).append(StampUtil.NEW_LINE);
		StampLogFactory.getInitLogBuff().append((new DateTime()).toString("MM/dd HH:mm:ss") + "\t\t[INFO] TEST IP List = ").append(config.getTEST_IP_LIST()).append(StampUtil.NEW_LINE);
		
    }
    
    @PreDestroy
    public synchronized void preDestroyJob() throws InterruptedException{
    	//System.out.println("종료");
    	notifyAll();
    }
}
