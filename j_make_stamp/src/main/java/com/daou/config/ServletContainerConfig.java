package com.daou.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.daou.config.StampConfigFactory.StampConfig;
import com.daou.log.StampLogFactory;

@Component
public class ServletContainerConfig implements
		EmbeddedServletContainerCustomizer {

	private int serverPortHttps;
	
	@Value("${tomcat.ajp.protocol}")
	String ajpProtocol;
	 
	@Value("${tomcat.ajp.port}")
	int ajpPort;
	 
	@Value("${tomcat.ajp.enabled}")
	boolean tomcatAjpEnabled;

	/*ServerConfiguration*/
	@Override
	public void customize(ConfigurableEmbeddedServletContainer container) {
		StampConfig conf = null;
		Logger serviceStartLog = null;
		
		try {
			conf = StampConfigFactory.getInstance();
			
			/*Module Start Log*/
			StampLogFactory.getInstance(conf).writeInitLog();
			serviceStartLog = StampLogFactory.getInstance(conf).getStampLogger();

			if(conf != null && serviceStartLog != null) {
				/*Service PORT를 설정값을 반영하여 수정*/
				int servicePort = Integer.parseInt(conf.getSERVICE_PORT());
			
				serverPortHttps = servicePort;
				
				serviceStartLog.info("[INFO] SET SERVICE PORT = " + conf.getSERVICE_PORT());
				serviceStartLog.info("=================================================================");

				container.setPort(servicePort);				
			}			
		} catch (Exception e) {
			
		}
		
	}
	
    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        characterEncodingFilter.setForceEncoding(true);
        return characterEncodingFilter;
    }
    
    @Bean
    public TomcatEmbeddedServletContainerFactory servletContainer() {
    	TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/MakeStamp/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        
        tomcat.addAdditionalTomcatConnectors(createHttpConnector());
        
        if (tomcatAjpEnabled)
        {
        	Connector ajpConnector = new Connector(ajpProtocol);
        	ajpConnector.setProtocol(ajpProtocol);
        	ajpConnector.setPort(ajpPort);
        	ajpConnector.setSecure(false);
        	ajpConnector.setAllowTrace(false);
        	ajpConnector.setScheme("http");
        	tomcat.addAdditionalTomcatConnectors(ajpConnector);
        }
        
        return tomcat;
    }
    
    // IMON용 http 포트 추가
	private Connector createHttpConnector() {
        Connector connector =
                new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setSecure(false);
        connector.setRedirectPort(serverPortHttps);
        connector.setPort(StampDefine.DEFAULT_IMON_PORT);
        
        return connector;
    }
}
