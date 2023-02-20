package com.daou;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.daou.config.ServletContainerConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=ServletContainerConfig.class)
//@SpringApplicationConfiguration(classes = JMakeStampApplication.class)
@WebAppConfiguration
public class JMakeStampApplicationTests {

	@Test
	public void contextLoads() {
	}

}
