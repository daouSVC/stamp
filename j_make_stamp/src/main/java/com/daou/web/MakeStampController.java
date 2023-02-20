package com.daou.web;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.daou.alert.MakeAlert;
import com.daou.common.StampLib;
import com.daou.common.StampResult;
import com.daou.config.StampConfigFactory;
import com.daou.config.StampUtil;
import com.daou.config.StampConfigFactory.StampConfig;

@Controller
public class MakeStampController {
	private final String SESSION_NAME_FOR_ERR_MSG = "err_msg";
	private final String SESSION_NAME_FOR_MSG = "msg";
	private final String SESSION_NAME_FOR_REDIRECT_URL = "re_url";
	private final String REDIRECT_TYPE = "history_back";

	@RequestMapping(value = "/MakeStamp/MakeStamp.do", method = { RequestMethod.POST, RequestMethod.GET })
	public String MakeStampView(Model model, HttpServletRequest request, HttpSession session
															, @RequestParam("sname") String sname
															, @RequestParam("stype") String stype
															, @RequestParam("id") String id
															, @RequestParam("domain") String domain
															, @RequestParam("homepath") String homepath
															, @RequestParam("re_url") String re_url) throws Exception {

		return doMakeStamp(request, session, sname, stype, id, domain, homepath, re_url, "http");
	}
	
	@RequestMapping(value = "/MakeStamp/MakeStamp2.do", method = { RequestMethod.POST, RequestMethod.GET })
	public String MakeStamp2View(Model model, HttpServletRequest request, HttpSession session
															, @RequestParam("sname") String sname
															, @RequestParam("stype") String stype
															, @RequestParam("id") String id
															, @RequestParam("domain") String domain
															, @RequestParam("homepath") String homepath
															, @RequestParam("re_url") String re_url) throws Exception {

		return doMakeStamp(request, session, sname, stype, id, domain, homepath, re_url, "https");
	}

	private String doMakeStamp(HttpServletRequest request, HttpSession session, String sname, String stype, String id, String domain,
			String homepath, String re_url, String protocol) throws Exception, UnsupportedEncodingException {
		Logger log = StampUtil.appLogger();
		String upload_result = "";

		String redirect_url = "/error";
		String referer_info = request.getHeader("referer");
		
		sname = new String(request.getParameter("sname").getBytes("8859_1"), "euc-kr");
		stype = new String(request.getParameter("stype").getBytes("8859_1"), "euc-kr");
		
		if(!re_url.equals("")){
			redirect_url = ( (domain.toLowerCase().indexOf(protocol + "://") > -1) ? "" : protocol + "://" ) + domain + "/" + re_url;
		}
		
		session.setAttribute(SESSION_NAME_FOR_REDIRECT_URL, redirect_url);
		
		String key = id + "_" + StampUtil.getDateString("yyyyMMddHHmmss");
		String converted_sname = StringEscapeUtils.unescapeHtml(sname);
		
		log.info("-----------------------------------------------------------------------------------------------------------------------------------");
		
		log.info("[INFO] Request Info, Key = " + key
				+ " | Remote Host = " + request.getRemoteHost()
				+ " | sname_org = " + sname
				//+ " | sname_escape = " + StringEscapeUtils.escapeHtml(converted_sname)
				+ " | sname_converted = " + converted_sname
				+ " | stype = " + stype
				+ " | id = " + id
				+ " | domain = " + domain
				+ " | homepath = " + homepath
				+ " | re_url = " + re_url
				+ " | Referer = " + referer_info
				+ " | RequestPath = " + request.getRequestURI()
				);
		
		log.info("[INFO] Stamp Image Create Start [" + key + ".jpg]");
		
		try {
			StampConfig config = StampConfigFactory.getInstance();				
			
			BufferedImage image = new BufferedImage(152, 152, BufferedImage.TYPE_INT_RGB);
			Graphics2D stamp_img = image.createGraphics(); 
			
			// 도장 이미지 생성
			StampResult createStampImg = (new StampLib(stamp_img, stype, converted_sname)).drawStampGraphics();
			
			if(createStampImg.isErr()){
				log.info("[ERROR] Create Stamp Image Error, Message: " + createStampImg.getMsg());
				return GoAlertPage(log, session, createStampImg.getMsg(), false, domain, "");
			} else {
				// 이미지 파일 저장
				String img_file_name = config.getFILE_PATH() + File.separator + key + ".jpg";
				ImageIO.write(image, "jpg", new File(img_file_name));
				log.info("[INFO] Stamp Image Create Complete");
				
				// 도장 프레임 새로고치는 페이지로 넘김						
				log.info("[INFO] Return Url: " + redirect_url);

				// 이미지 업로드
				if(protocol.equalsIgnoreCase("HTTP")) {
					upload_result = HttpUploadStamp(log, key, domain, re_url, homepath, img_file_name, protocol);
				} else if(protocol.equalsIgnoreCase("HTTPS")) {
					upload_result = HttpsUploadStamp(log, key, domain, re_url, homepath, img_file_name, protocol);
				}
				
				// 생성 이미지 삭제 또는 보관
				if(config.getFILE_DELETE_OPTION().equalsIgnoreCase("Y")) {
					DeleteStampImgFile(log, img_file_name);
				} else {
					MoveStampImgFile(log, img_file_name, config.getFILE_PATH() + File.separator + "BAK", key + ".jpg");
				}				
				
				if(upload_result.equalsIgnoreCase("ERROR")) {
					log.info("[ERROR] Stamp Image Upload Fail");						
					return GoAlertPage(log, session,"이미지 업로드에 실패하였습니다. 관리자에게 문의 해 주시기 바랍니다.", redirect_url, true, domain, "ImageUpload Fail !! Check Stamp Service");
				}
			}
			
		} catch (IOException ioe){
			log.info("[ERROR] Stamp Image Create Fail \r\n" + ioe.getMessage());						
			return GoAlertPage(log, session,"이미지 생성에 실패하였습니다. 관리자에게 문의 해 주시기 바랍니다.", redirect_url, true, domain, "Image Create Fail !! Check Stamp Service");
		} catch (Exception e) {
			log.info("[ERROR] Etc Error, Message = " + e.getMessage());
			return GoAlertPage(log, session,"기타 에러가 발생하였습니다. 관리자에게 문의 해 주시기 바랍니다.[1]", redirect_url, true, domain, "Etc Fail !! Check Stamp Service");
		} 

		return "redirect:" + redirect_url;
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
	
	/**
	 * @param log
	 * @param orgin_img_file_name
	 * @param file_path
	 * @param file_name
	 */
	private void MoveStampImgFile(Logger log, String orgin_img_file_name, String file_path, String file_name) {
		File sImg = new File(orgin_img_file_name);
		
		if(sImg.exists() && sImg.isFile()){
			
			String new_img_file_path_name = file_path + File.separator + file_name;
			
			File new_file_path = new File(file_path);
			
			if(!new_file_path.exists()){
				if(!new_file_path.mkdir()) {
					log.info("[INFO] Stamp Image File Move Fail, Error on Make Directory = " + file_path);
					return;
				}
			}
			
			if(sImg.renameTo(new File(new_img_file_path_name))) {
				log.info("[INFO] Stamp Image File Moved, Moved File = " + orgin_img_file_name);
			} else {
				log.info("[INFO] Stamp Image File Move Fail, File =" + orgin_img_file_name);
			}
		} else {
			log.info("[INFO] Stamp Image File is not Exist, File =" + orgin_img_file_name);
		}
	}
	
	@RequestMapping(value = "/error", method = { RequestMethod.GET })
	public String ErrorView(Model model, HttpServletRequest request, HttpSession session) throws Exception {
				
		if (session.getAttribute(SESSION_NAME_FOR_ERR_MSG) != null) {
			Logger log = StampUtil.appLogger();
			log.info("[ERROR] Message : " + (String) session.getAttribute(SESSION_NAME_FOR_ERR_MSG));
		}
		
		return "error";
	}
	
	@RequestMapping(value = "/msg", method = { RequestMethod.GET })
	public String MsgView(Model model, HttpServletRequest request, HttpSession session) throws Exception {
		
		if (session.getAttribute(SESSION_NAME_FOR_MSG) != null && session.getAttribute(SESSION_NAME_FOR_REDIRECT_URL) != null) {
			String msg = (String) session.getAttribute(SESSION_NAME_FOR_MSG);
			String re_url = (String) session.getAttribute(SESSION_NAME_FOR_REDIRECT_URL);
				
			StampUtil.RemoveSession(session);
			
			Logger log = StampUtil.appLogger();
			
			model.addAttribute("msg", msg);
			model.addAttribute("re_url", re_url);
			model.addAttribute("is_redirect_url", re_url.equalsIgnoreCase(REDIRECT_TYPE) ? false : true);

			log.info("[INFO] Message : " + msg);
			log.info("[INFO] Redirect URL : " + (re_url.equalsIgnoreCase(REDIRECT_TYPE) ? "history" : re_url));
			
			return "messages"; 
		}
		
		return "error";
	}	
	
	@RequestMapping(value = "/Default.html", method = {RequestMethod.GET})
	public String DefaultView(Model model, HttpServletRequest request, HttpSession session) throws Exception {

		Logger log;
		
		log = StampUtil.appLogger();
		log.info("[ALIVE CHECK REQUEST] Remote Host = " + request.getRemoteHost() + " | Request URL = " + request.getRequestURL().toString());
			
		return "default";
	}
	
	private String GoAlertPage(Logger log, HttpSession session, String AlertUserMsg, String RedirectUrl, boolean bMakeAlert, String domain, String AlertAdminMsg){
		session.setAttribute(SESSION_NAME_FOR_MSG, AlertUserMsg);
		session.setAttribute(SESSION_NAME_FOR_REDIRECT_URL, RedirectUrl);
		
		if(bMakeAlert) {
			try {
				MakeAlert.WriteMsg(domain, AlertAdminMsg);
			} catch (Exception e) {
				log.info(e.getMessage(), e);
			}
		}
		return "redirect:/msg";
	}
	
	private String GoAlertPage(Logger log, HttpSession session, String AlertUserMsg, boolean bMakeAlert, String domain, String AlertAdminMsg){
		return GoAlertPage(log, session, AlertUserMsg, REDIRECT_TYPE, bMakeAlert, domain, AlertAdminMsg);
	}
	
	// HTTP POST request
	private String HttpUploadStamp(Logger log, String key, String domain, String re_url, String homepath, String img_file_name, String protocol) throws Exception {
		
		File stamp_file = new File(img_file_name);
		String result_description = "[ERROR] Fail";	
		String result = "ERROR";
		
		if(stamp_file.exists()){			
			String strHttp = (domain.toLowerCase().indexOf(protocol + "://") > -1) ? "" : protocol + "://";
			String url = strHttp + domain + "/mgr/PPFaxMgr.qri?act=transfer_stamp";
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			
			
			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "multipart/form-data; boundary=---------------------------7da35b910818");
			
			String filename = key + ".jpg\r\n";
			String info;
			info = "-----------------------------7da35b910818\r\nContent-Disposition: form-data; name=\"act\"\r\n\r\n";
			info += "transfer_stamp";
			info += "\r\n-----------------------------7da35b910818\r\nContent-Disposition: form-data; name=\"re_url\"\r\n\r\n";
			info += re_url;
			info += "\r\n-----------------------------7da35b910818\r\nContent-Disposition: form-data; name=\"homepath\"\r\n\r\n";
//			info += URLEncoder.encode(homepath, "UTF-8");
			info += homepath;
			info += "\r\n-----------------------------7da35b910818\r\nContent-Disposition: form-data; name=\"s_key\"\r\n\r\n";
			info += "CFF2F3740F2D93B3A4040186F9C5FC8C94073921DD014930EA300D1119DCC6A8";// s_key 고정
			info += "\r\n-----------------------------7da35b910818\r\nContent-Disposition: form-data; name=\"userfile\"; filename=\"";
			info += filename;
			info += "Content-Type: image/jpg\r\n\r\n";
			
			byte[] info_data = info.getBytes();
			
			FileInputStream fi = new FileInputStream(new File(img_file_name));
			
			byte [] file_data = new byte[fi.available()];
			fi.read(file_data);
			fi.close();
			
			String footer = "\r\n-----------------------------7da35b910818--\r\n";
			byte[] cont_footer = footer.getBytes();
			
			log.info("[INFO] Image File Size=" + file_data.length + " bytes, Total Request Size=" + String.valueOf(info_data.length + file_data.length + cont_footer.length) + " bytes");
			
			//con.setFixedLengthStreamingMode(post_data.length + file_data.length + cont_footer.length);
			
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(info);
			wr.write(file_data);
			wr.writeBytes(footer);
			wr.flush();
			wr.close();
			
			int responseCode = con.getResponseCode();
			String responseMsg = con.getResponseMessage();
			
			log.info("[INFO] Sending 'POST' request to URL = " + url);
			log.debug("[INFO] Post Data = " + info + file_data.toString() + footer);
			
			if(HttpStatus.valueOf(responseCode).equals(HttpStatus.OK)){	
				result_description = "[INFO] Success Receive a Response Data";
				result_description += " / Status Code = " + HttpStatus.valueOf(responseCode).toString();
				result_description += " / Status Description = " + responseMsg;

				log.info(result_description);

				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				// 업로드 성공 시 리턴 값으로 1000 코드를 받음
				if (response.indexOf("1000") > -1) {
					result = "SUCCESS";
					result_description = "[SUCCESS] Upload Image File";
				} else {
					// 업로드 실패 시 7xx 코드를 받음
					// 710: IP거부 또는 비밀키 오류
					// 720: 디렉토리 생성 오류
					// 730: 허용되지 않은 파일 확장자
					// 740: 파일업로드 실패
					result = "ERROR";
                	result_description = "[ERROR] Upload Image File";
				}
				result_description += " / Response = " + response.toString();	
				
				log.info(result_description);
			} else {
				result = "ERROR";
				result_description = "[ERROR] Fail";	
				result_description += " / Response Status Code = " + HttpStatus.valueOf(responseCode).toString();
				result_description += " / StatusDescription = " + responseMsg;
				
				log.info(result_description);
			}
		} else {
			result = "ERROR";
			log.info("[ERROR] Stamp Image File is not Exist, File = " + img_file_name);
		}
		
		return result;
	}
	
	// HTTPS POST request
	private String HttpsUploadStamp(Logger log, String key, String domain, String re_url, String homepath, String img_file_name, String protocol) throws Exception {
		
		File stamp_file = new File(img_file_name);
		String result_description = "[ERROR] Fail";	
		String result = "ERROR";
		
		if(stamp_file.exists()){			
			String strHttp = (domain.toLowerCase().indexOf(protocol + "://") > -1) ? "" : protocol + "://";
			String url = strHttp + domain + "/mgr/PPFaxMgr.qri?act=transfer_stamp";
			
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@SuppressWarnings("unused")
				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				@SuppressWarnings("unused")
				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
						throws CertificateException {
				}
			} };
			
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			
			
			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "multipart/form-data; boundary=---------------------------7da35b910818");
			
			String filename = key + ".jpg\r\n";
			String info;
			info = "-----------------------------7da35b910818\r\nContent-Disposition: form-data; name=\"act\"\r\n\r\n";
			info += "transfer_stamp";
			info += "\r\n-----------------------------7da35b910818\r\nContent-Disposition: form-data; name=\"re_url\"\r\n\r\n";
			info += re_url;
			info += "\r\n-----------------------------7da35b910818\r\nContent-Disposition: form-data; name=\"homepath\"\r\n\r\n";
//				info += URLEncoder.encode(homepath, "UTF-8");
			info += homepath;
			info += "\r\n-----------------------------7da35b910818\r\nContent-Disposition: form-data; name=\"s_key\"\r\n\r\n";
			info += "CFF2F3740F2D93B3A4040186F9C5FC8C94073921DD014930EA300D1119DCC6A8";// s_key 고정
			info += "\r\n-----------------------------7da35b910818\r\nContent-Disposition: form-data; name=\"userfile\"; filename=\"";
			info += filename;
			info += "Content-Type: image/jpg\r\n\r\n";
			
			byte[] info_data = info.getBytes();
			
			FileInputStream fi = new FileInputStream(new File(img_file_name));
			
			byte [] file_data = new byte[fi.available()];
			fi.read(file_data);
			fi.close();
			
			String footer = "\r\n-----------------------------7da35b910818--\r\n";
			byte[] cont_footer = footer.getBytes();
			
			log.info("[INFO] Image File Size=" + file_data.length + " bytes, Total Request Size=" + String.valueOf(info_data.length + file_data.length + cont_footer.length) + " bytes");
			
			//con.setFixedLengthStreamingMode(post_data.length + file_data.length + cont_footer.length);
			
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(info);
			wr.write(file_data);
			wr.writeBytes(footer);
			wr.flush();
			wr.close();
			
			int responseCode = con.getResponseCode();
			String responseMsg = con.getResponseMessage();
			
			log.info("[INFO] Sending 'POST' request to URL = " + url);
			log.debug("[INFO] Post Data = " + info + file_data.toString() + footer);
			
			if(HttpStatus.valueOf(responseCode).equals(HttpStatus.OK)){	
				result_description = "[INFO] Success Receive a Response Data";
				result_description += " / Status Code = " + HttpStatus.valueOf(responseCode).toString();
				result_description += " / Status Description = " + responseMsg;
				
				log.info(result_description);
				
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				// 업로드 성공 시 리턴 값으로 1000 코드를 받음
				if (response.indexOf("1000") > -1) {
					result = "SUCCESS";
					result_description = "[SUCCESS] Upload Image File";
				} else {
					// 업로드 실패 시 7xx 코드를 받음
					// 710: IP거부 또는 비밀키 오류
					// 720: 디렉토리 생성 오류
					// 730: 허용되지 않은 파일 확장자
					// 740: 파일업로드 실패
					result = "ERROR";
					result_description = "[ERROR] Upload Image File";
				}
				result_description += " / Response = " + response.toString();	
				
				log.info(result_description);
			} else {
				result = "ERROR";
				result_description = "[ERROR] Fail";	
				result_description += " / Response Status Code = " + HttpStatus.valueOf(responseCode).toString();
				result_description += " / StatusDescription = " + responseMsg;
				
				log.info(result_description);
			}
		} else {
			result = "ERROR";
			log.info("[ERROR] Stamp Image File is not Exist, File = " + img_file_name);
		}
		
		return result;
	}
}
