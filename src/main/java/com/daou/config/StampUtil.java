package com.daou.config;

import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.daou.config.StampConfigFactory;
import com.daou.log.StampLogFactory;

public class StampUtil {
	/* 서비스 내에서 공통적으로 사용할 개행 문자 */
	public static final String NEW_LINE = System.getProperty("line.separator");

	public static String config_file_name = "";

	/**
	 * Log출력 포멧을 위한 DATE FORMAT을 반환한다.
	 */
	public static String getDateForLog() {
		return new DateTime().toString("MM/dd HH:mm:ss");
	}
	
	/**
	 * 지정 format (ex: yyyyMMddHHmmss)으로 현재 시간을 String으로 변환하여 반환
	 * @param format
	 * @return
	 */
	public static String getDateString(String format) {
		return new DateTime().toString(format);
	}
	
	/**
	 * date 값을 지정 format (ex: yyyyMMddHHmmss)으로 변환된 String을 반환
	 * @param date
	 * @param format
	 * @return
	 */
	public static String getDateStringFromDate(Date date, String format) {
		return new DateTime(date).toString(format);
	}
	
	
	public static boolean isValidRefer(String referer){
		if(StampDefine.Referer_List.equals("")){
			return true;
		}
		
		if(referer == null){
			referer = "";
		}
		
		String [] accepted_referers = StampDefine.Referer_List.split("\\|");
		
		for(int i=0; i < accepted_referers.length; i++){
			if(referer.indexOf(accepted_referers[i].trim()) > 0){
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isTestableIP(String ipaddr){
		if(StampDefine.Test_ip_List.equals("")){
			return true;
		}
		
		if(ipaddr == null){
			ipaddr = "";
		}
		
		String [] accepted_ipaddr = StampDefine.Test_ip_List.split("\\|");
		
		for(int i=0; i < accepted_ipaddr.length; i++){
			if(ipaddr.equals(accepted_ipaddr[i].trim())){
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 서비스 Logger 반환
	 * @return
	 * @throws Exception
	 */
	public static Logger appLogger() throws Exception{
		return StampLogFactory.getInstance(StampConfigFactory.getInstance()).getStampLogger();
	}
	
	public static Logger testLogger() throws Exception{
		return StampLogFactory.getInstance(StampConfigFactory.getInstance()).getStampTestLogger();
	}

	
	/**
	 * 세션 저장 값 제거
	 * @param session
	 */
	public static void RemoveSession(HttpSession session) {
		Enumeration<String> enum_app = session.getAttributeNames();
		 String ls_name = "";
		 
		 while(enum_app.hasMoreElements()) {
			 ls_name = enum_app.nextElement().toString();
			session.setAttribute(ls_name, null);
		 }
	}

	
	/**
	 * 파일의 존재 여부를 확인한다
	 * 
	 * @param file_path
	 *            파일 경로
	 * @return boolean 파일의 존재 여부
	 */
	/*
	public static boolean makeFile(String file_path) {

		try {
			File f = new File(file_path);

			if( !f.isFile()){
				f = f.getAbsoluteFile();
				File pf = new File(f.getParent());
				if(!pf.exists()){
					pf.mkdir();
				}
				f.createNewFile();
			}
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	 */
	
	
		
	/**
	 * 쓰기 가능한 디렉토리인지 확인한다
	 * @param   dir_path    디렉토리 경로
	 * @param	log			로거
	 * @return  boolean     쓰기 가능한 디렉토리인지 여부
	 * @throws  IOException
	 */
	/*
	public static boolean isDirAndCanWrite(String dir_path, Logger log)
		throws IOException, NullPointerException {

		File f = new File(dir_path);

		if (!f.exists()) {
			if (f.mkdirs()) {
				log.info("[OK] Make a Directory, DIR=" + dir_path);
			} else {
				return false;
			}
		}

		if (f.isDirectory() && f.canWrite()) {
			return true;
		}
		else {
			return false;
		}
	}
	*/
}
