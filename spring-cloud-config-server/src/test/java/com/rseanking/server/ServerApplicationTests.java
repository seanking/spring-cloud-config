package com.rseanking.server;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ServerApplication.class })
public class ServerApplicationTests {
	
	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;

	@Before
	public void setup() {
		mvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	public void shouldProvideDefaultConfigurationForClient() throws Exception {
		mvc.perform(get("/greeting-service/default"))
			.andExpect(jsonPath("propertySources[0].source.['hello.greeting']", equalTo("Hello!")))
			.andExpect(status().isOk());
	}
	
	@Test
	public void shouldProvideDevConfigurationForClient() throws Exception {
		mvc.perform(get("/greeting-service/dev"))
			.andExpect(jsonPath("propertySources[0].source.['hello.greeting']", equalTo("Hello Dev!")))
			.andExpect(status().isOk());
	}
	
	@Test
	public void shouldProvideProdConfigurationForClient() throws Exception {
		mvc.perform(get("/greeting-service/prod"))
			.andExpect(jsonPath("propertySources[0].source.['hello.greeting']", equalTo("Hello World!")))
			.andExpect(status().isOk());
	}
}
