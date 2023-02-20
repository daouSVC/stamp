package com.daou.alert;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;

import com.daou.config.StampConfigFactory;
import com.daou.config.StampUtil;
import com.daou.config.StampConfigFactory.StampConfig;

public class MakeAlert {
	
	public static void WriteMsg(String domain, String msgBody) throws Exception{
		StampConfig config = StampConfigFactory.getInstance();
		String strNow = StampUtil.getDateString("MM-dd HH:mm:ss");
		String strMessage = "[" + InetAddress.getLocalHost().getHostName() + " / " + strNow + "]\n";
		
		//파일 객체 생성
		String filePath = "";
		String fileName = "msg_" + StampUtil.getDateString("yyyyMMddHHmmss");
		
		switch(domain){
			case "enfax.ppurio.com":
				filePath = config.getALERT_ENFAX_FILE_PATH();
				break;
			case "www.ppurio.com":
				filePath = config.getALERT_PPURIO_FILE_PATH();
				break;
			default:
				filePath = config.getALERT_ENFAX_FILE_PATH();
				break;						
		}
		
		File file = new File(config.getALERT_BASE_FILE_PATH() + File.separator + fileName);

		FileWriter fw = new FileWriter(file);
		fw.write(strMessage + msgBody);
		fw.close();
		
		file.renameTo(new File(filePath + File.separator + fileName));
	}	
}
