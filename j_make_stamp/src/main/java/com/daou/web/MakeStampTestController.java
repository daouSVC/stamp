package com.daou.web;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.daou.alert.MakeAlert;
import com.daou.common.StampLib;
import com.daou.common.StampResult;
import com.daou.config.StampConfigFactory;
import com.daou.config.StampUtil;
import com.daou.config.StampConfigFactory.StampConfig;
import com.daou.entity.TestMakeStampResult;

@RestController
public class MakeStampTestController {
	
	@RequestMapping(value = "/MakeStampTest", method = { RequestMethod.POST, RequestMethod.GET })
	public TestMakeStampResult MakeStampTestView(Model model, HttpServletRequest request, HttpSession session
			, @RequestParam("sname") String sname
			, @RequestParam("stype") String stype
			, @RequestParam("id") String id) throws Exception {
		
		Logger log = StampUtil.testLogger();

		TestMakeStampResult result = new TestMakeStampResult();
		String log_msg="";
		String referer_info = request.getHeader("referer");
		
		if(!StampUtil.isTestableIP(request.getRemoteHost())){
			log_msg = "[ERROR] [TEST] Host IP is not allowed Test IP";
			log.info(log_msg);
			
			result.setResult("ERROR");
			result.setMsg(log_msg);
			
			return result;
		}
		
		String key = id + StampUtil.getDateString("yyyyMMddHHmmss");
		String converted_sname = StringEscapeUtils.unescapeHtml(sname);
		
		log.info("-----------------------------------------------------------------------------------------------------------------------------------");
		log.info("[INFO] [TEST] Stamp Image Create Start [" + key + ".jpg]");
		
		log.info("[INFO] [TEST] Parameter Info, Key = " + key
				+ " | Request IP = " + request.getRemoteHost()
				+ " | sname_org = " + sname
				//+ " | sname_escape = " + StringEscapeUtils.escapeHtml(converted_sname)
				+ " | sname_converted = " + converted_sname
				+ " | stype = " + stype
				+ " | id = " + id
				+ " | referer = " + referer_info
				+ " | referer_valid_chk_result = " + StampUtil.isValidRefer(referer_info)
				);
		
		try {
			StampConfig config = StampConfigFactory.getInstance();				
			
			BufferedImage image = new BufferedImage(152, 152, BufferedImage.TYPE_INT_RGB);
			Graphics2D stamp_img = image.createGraphics(); 
			
			// 도장 이미지 생성
			StampResult createStampImg = (new StampLib(stamp_img, stype, converted_sname)).drawStampGraphics();
			
			if(createStampImg.isErr()){
				log_msg = "[ERROR] [TEST] Create Stamp Image Error, Message: " + createStampImg.getMsg();
				log.info(log_msg);
				
				result.setResult("ERROR");
				result.setMsg(log_msg);
				
				return result;
			} else {
				// 이미지 파일 저장
				String img_file_name = config.getFILE_PATH() + File.separator + key + ".jpg";
				ImageIO.write(image, "jpg", new File(img_file_name));
				log.info("[INFO] [TEST] Stamp Image Create Complete !!!");
				
				FileInputStream fi = new FileInputStream(new File(img_file_name));
				
				byte [] file_data = new byte[fi.available()];
				fi.read(file_data);
				fi.close();
				
				result.setResult("SUCCESS");
				result.setMsg("성공");
				result.setFile_data(file_data);
				
				DeleteStampImgFile(log, img_file_name);
			}
			
		} catch (IOException ioe){
			log_msg = "[ERROR] [TEST] Stamp Image Create Fail \r\n" + ioe.getMessage();
			log.info(log_msg);
			
			result.setResult("ERROR");
			result.setMsg(log_msg);
			
			return result;
		} catch (Exception e) {
			log_msg = "[ERROR] [TEST] Etc Error, Message = " + e.getMessage();
			log.info(log_msg);
			
			result.setResult("ERROR");
			result.setMsg(log_msg);
			
			return result;
		} 

		return result;
	}
	
	@RequestMapping(value = "/MakeStamp/MakeAlertMsgTest", method = { RequestMethod.POST, RequestMethod.GET })
	public void MakeAlertMsgTestView(Model model, HttpServletRequest request, HttpSession session
			, @RequestParam("domain") String domain
			, @RequestParam("AlertMsg") String AlertMsg) throws Exception {
		
		Logger log = StampUtil.testLogger();
		
		String log_msg="";
		
		if(!StampUtil.isTestableIP(request.getRemoteHost())){
			log_msg = "[ERROR] [TEST] Host IP is not allowed Test IP";
			log.info(log_msg);
		}
		
		MakeAlert.WriteMsg(domain, AlertMsg);
	}
	
	/**
	 * @param log
	 * @param img_file_path_name
	 */
	private synchronized void DeleteStampImgFile(Logger log, String img_file_path_name) {
		File sImg = new File(img_file_path_name);
		
		if(sImg.exists() && sImg.isFile()){
			if(sImg.delete()) {
				log.info("[INFO] Stamp Image File Deleted, Deleted File = " + img_file_path_name);
			} else {
				log.info("[INFO] Stamp Image File Delete Fail, File =" + img_file_path_name);
			}
		} else {
			log.info("[INFO] Stamp Image File is not Exist, File =" + img_file_path_name);
		}
	}
}
