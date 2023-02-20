package com.daou.common;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;

import com.daou.config.StampUtil;


public class StampLib {
	
	private enum StampInOutType {YANGGAK, UMGAK, UNDEFINED}	
	private enum StampLanguage {HANGUL, HANJA, UNDEFINED}	
	private enum StampShape {CIRCLE, RECTANGLE, UNDEFINED}
	
	private Graphics2D stamp_graphics;
	
	// 도장 유형 ( 한글원형음각, 한글원형양각, 한글사각음각, 한글사각양각, 한자원형음각, 한자원형양각, 한자사각음각, 한자사각양각)
	private String stamp_type;
	private StampLanguage stamp_lang;
	private StampShape stamp_shape;
	private StampInOutType stamp_inout_type;	
	
	// 도장 글씨
	private String stamp_text;
	private String stamp_text_line1_1;
	private String stamp_text_line1_2;
	private String stamp_text_line2_1;
	private String stamp_text_line2_2;
	
	// 폰트 정보
	private final int FONT_SIZE = 39;
	private final String HANGUL_FONT = "(한)한전서";
	private final String HANJA_FONT = "(한)인장체";
	//private final String HANJA_FONT = "(한)고인서A";
	private String font_name;
	
	// 생성자
	public StampLib(Graphics2D stamp_graphics, String stamp_type, String stamp_text) {
		super();
		
		this.stamp_graphics = stamp_graphics;
		this.stamp_type = stamp_type;
		this.stamp_text = stamp_text;
		
		if(this.stamp_type.indexOf("한글") > -1){
			this.stamp_lang = StampLanguage.HANGUL;
			this.font_name = HANGUL_FONT;
		} else 	if(this.stamp_type.indexOf("한자") > -1){
			this.stamp_lang = StampLanguage.HANJA;
			this.font_name = HANJA_FONT;
		} else {
			this.stamp_lang = StampLanguage.UNDEFINED;
		}
		
		if(this.stamp_type.indexOf("양각") > -1){
			this.stamp_inout_type = StampInOutType.YANGGAK;
		} else if(this.stamp_type.indexOf("음각") > -1){
			this.stamp_inout_type = StampInOutType.UMGAK;
		} else {
			this.stamp_inout_type = StampInOutType.UNDEFINED;
		}
		
		if(this.stamp_type.indexOf("원형") > -1){
			this.stamp_shape = StampShape.CIRCLE;
		} else if(this.stamp_type.indexOf("사각") > -1){
			this.stamp_shape = StampShape.RECTANGLE;
		} else {
			this.stamp_shape = StampShape.UNDEFINED;
		}
	}
	
	// 지정 유형에 따라 도장 이미지를 Graphics2D 타입으로 생성
	public StampResult drawStampGraphics() throws Exception{
		
		// 바탕 색 지정
		stamp_graphics.setColor(Color.WHITE);
		stamp_graphics.fillRect(0, 0, 152, 152);
		
		StampResult chk_rslt = ValidCheck();

		if(!chk_rslt.isErr()){
			switch(this.stamp_shape){
				case CIRCLE:
					drawStampBgCircle(stamp_graphics);
					break;
				case RECTANGLE:
					drawStampBgRectangle(stamp_graphics);
					break;
				default:
					break;
			}
			
			//stamp_graphics.setFont(new Font(font_name, Font.BOLD, FONT_SIZE));
			//stamp_graphics.drawString(stamp_text_line1, 33, 69);
			//stamp_graphics.drawString(stamp_text_line2, 33, 108);

			drawString(stamp_graphics, stamp_text_line1_1, 35, 69);
			drawString(stamp_graphics, stamp_text_line1_2, 36 + FONT_SIZE, 69);
			drawString(stamp_graphics, stamp_text_line2_1, 35, 108);
			drawString(stamp_graphics, stamp_text_line2_2, 36 + FONT_SIZE, 108);	
		}
		
		return chk_rslt;
	}
	
	// 유효성 체크
	private StampResult ValidCheck(){
		StampResult chk_rslt = new StampResult();
		
		if(this.stamp_lang.equals(StampLanguage.UNDEFINED) || this.stamp_inout_type.equals(StampInOutType.UNDEFINED) || this.stamp_shape.equals(StampShape.UNDEFINED)) {
			chk_rslt.setMsg("도장유형을 인식 할 수 없습니다.");
			return chk_rslt;
		}
		
		if(this.stamp_text.length() < 2 || this.stamp_text.length() > 4){
			chk_rslt.setMsg("2~4자리로 입력 해 주시기 바랍니다.");
			return chk_rslt;
		}

		switch(this.stamp_lang){
			case HANGUL:
				for(char ch : this.stamp_text.toCharArray())
				{
					//유니코드 한글범위 체크
					if (!(ch >= 0xAC00 && ch <= 0xD7AF)){
						chk_rslt.setMsg("한글만 입력하세요");
						return chk_rslt;
					}
				}
				
				if (this.stamp_text.length() == 2)
					this.stamp_text += "의인";
				else if (this.stamp_text.length() == 3)
					this.stamp_text += "인";
				
				
				break;
			case HANJA:
				for(char ch : this.stamp_text.toCharArray())
				{
					//유니코드 한자 범위(??) 체크
					if (!((ch >= 0x3400 && ch <= 0x4DBF) || (ch >= 0x4E00 && ch <= 0x9FBF) || (ch >= 0xF900 && ch <= 0xFAFF))){
						chk_rslt.setMsg("한자만 입력하세요");
						return chk_rslt;
					}
				}
				
				if (this.stamp_text.length() == 2)
					this.stamp_text += "之印";
				else if (this.stamp_text.length() == 3)
					this.stamp_text += "印";	
	
				break;
			case UNDEFINED:
				chk_rslt.setMsg("한글/한자만 가능합니다.");
				break;
			default:
				chk_rslt.setMsg("미지정(한글/한자) 오류");
				break;
		}
		
		if(!chk_rslt.isErr()){
			//this.stamp_text_line1 = stamp_text.substring(2,3) + stamp_text.substring(0,1);
			//this.stamp_text_line2 = stamp_text.substring(3) + stamp_text.substring(1,2);
			this.stamp_text_line1_1 = stamp_text.substring(2,3);
			this.stamp_text_line1_2 = stamp_text.substring(0,1);
			this.stamp_text_line2_1 = stamp_text.substring(3);
			this.stamp_text_line2_2 = stamp_text.substring(1,2);
		}
		
		return chk_rslt;
	}
	
	private void drawStampBgCircle(Graphics2D g){
		switch(this.stamp_inout_type){
			case UMGAK:
				// 둥근도장(음각)
				drawfillOval(g, Color.RED, 19, 19, 112, 112);
				
				g.setColor(Color.WHITE);
				break;
			case YANGGAK:
				// 둥근도장(양각)			
				drawfillOval(g, Color.RED, 19, 19, 112, 112);
				drawfillOval(g, Color.WHITE, 23, 23, 104, 104);
				
				g.setColor(Color.RED);
				break;
			default:
				break;		
		}
	}
	
	private void drawStampBgRectangle(Graphics2D g){
		switch(this.stamp_inout_type){
		case UMGAK:
			// 사각 도장 (음각)
			drawfillRoundRect(g, Color.RED, 32, 33, 86, 84, 8, 8);
			
			g.setColor(Color.WHITE);
			break;
		case YANGGAK:
			// 사각 도장 (양각)
			drawfillRoundRect(g, Color.RED, 32, 33, 86, 84, 8, 8);
			drawfillRoundRect(g, Color.WHITE, 35, 36, 80, 78, 4, 4);
						
			g.setColor(Color.RED);
			break;
		default:
			break;		
		}		
	}
	
	private void drawfillOval(Graphics2D g, Color c, int x, int y, int width, int height){
		g.setColor(c);
		g.fillOval(x, y, width, height);
	}
	
	private void drawfillRoundRect(Graphics2D g, Color c, int x, int y, int width, int height, int arcWidth, int arcHeight){
		g.setColor(c);
		g.fillRoundRect(x, y, width, height, arcWidth, arcHeight);
	}
	
	private void drawString(Graphics2D g, String sub_text, int x, int y) throws Exception{
		Font stamp_font = getAvailableFont(font_name, sub_text.charAt(0), FONT_SIZE);
		if(stamp_font != null){
			g.setFont(stamp_font);		
			g.drawString(sub_text, x, y);
		} else {
			StampUtil.appLogger().info("Can not Draw String, String = " + sub_text);
		}
	}
	
	private Font getAvailableFont(String default_font_name, char chk_char, int font_size) throws Exception{
		Font ret_font = null;
		String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		
		Font default_font = new Font(default_font_name, Font.PLAIN, font_size);
		
		if(default_font.canDisplay(chk_char)){
			ret_font = default_font;
		} else {		
		    for ( int i = 0; i < fonts.length; i++ )
		    {
		    	Font test_font = new Font(fonts[i], Font.PLAIN, font_size);
		    	if(test_font.canDisplay(chk_char)){
		    		StampUtil.appLogger().info("'" + chk_char + "' can not display in font(" + default_font_name + "), This Character Display in Other Font, Font Name = " + test_font.getFontName());
		    		ret_font = test_font;
		    		break;
		    	}
		    }
		}
		
		return ret_font;
	}
}


