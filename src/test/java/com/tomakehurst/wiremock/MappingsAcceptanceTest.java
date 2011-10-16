package com.tomakehurst.wiremock;

import static com.tomakehurst.wiremock.testsupport.HttpHeader.withHeader;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_OK;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tomakehurst.wiremock.testsupport.MappingJsonSamples;
import com.tomakehurst.wiremock.testsupport.WireMockClient;
import com.tomakehurst.wiremock.testsupport.WireMockResponse;

public class MappingsAcceptanceTest {
	
	private WireMock wireMock;
	private WireMockClient wireMockClient;
	
	
	@Before
	public void init() {
		wireMock = new WireMock();
		wireMock.start();
		wireMockClient = new WireMockClient();
	}
	
	@After
	public void stopWireMock() {
		wireMock.stop();
	}

	@Test
	public void cannedResponseIsReturnedForPreciseUrl() {
		WireMockResponse response = wireMockClient.get("/canned/resource");
		assertThat(response.statusCode(), is(HTTP_OK));
		assertThat(response.content(), is("{ \"somekey\": \"My value\" }"));
	}
	
	@Test
	public void basicMappingWithExactUriAndMethodMatchIsCreatedAndReturned() {
		wireMockClient.addResponse(MappingJsonSamples.BASIC_MAPPING_REQUEST);
		
		WireMockResponse response = wireMockClient.get("/a/registered/resource");
		
		assertThat(response.statusCode(), is(401));
		assertThat(response.content(), is("Not allowed!"));
		assertThat(response.header("Content-Type"), is("text/plain"));
	}
	
	@Test
	public void mappingWithStatusOnlyResponseIsCreatedAndReturned() {
		wireMockClient.addResponse(MappingJsonSamples.STATUS_ONLY_MAPPING_REQUEST);
		
		WireMockResponse response = wireMockClient.put("/status/only");
		
		assertThat(response.statusCode(), is(204));
		assertNull(response.content());
	}
	
	@Test
	public void mappingWithExactUriMethodAndHeaderMatchingIsCreatedAndReturned() {
		wireMockClient.addResponse(MappingJsonSamples.MAPPING_REQUEST_WITH_EXACT_HEADERS);
		
		WireMockResponse response = wireMockClient.get("/header/dependent",
				withHeader("Accept", "text/xml"),
				withHeader("If-None-Match", "abcd1234"));
		
		assertThat(response.statusCode(), is(304));
	}

	@Test
	public void notFoundResponseIsReturnedForUnregisteredUrl() {
		WireMockResponse response = wireMockClient.get("/non-existent/resource");
		assertThat(response.statusCode(), is(HTTP_NOT_FOUND));
	}
	
	@Test
	public void multipleMappingsSupported() {
		add200ResponseFor("/resource/1");
		add200ResponseFor("/resource/2");
		add200ResponseFor("/resource/3");
		
		getResponseAndAssert200Status("/resource/1");
		getResponseAndAssert200Status("/resource/2");
		getResponseAndAssert200Status("/resource/3");
	}

	@Test
	public void multipleInvocationsSupported() {
		add200ResponseFor("/resource/100");
		getResponseAndAssert200Status("/resource/100");
		getResponseAndAssert200Status("/resource/100");
		getResponseAndAssert200Status("/resource/100");
	}

	private void getResponseAndAssert200Status(String uri) {
		WireMockResponse response = wireMockClient.get(uri);
		assertThat(response.statusCode(), is(200));
	}
	
	private void add200ResponseFor(String uri) {
		wireMockClient.addResponse(String.format(MappingJsonSamples.STATUS_ONLY_GET_MAPPING_TEMPLATE, uri));
	}
}