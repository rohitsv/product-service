package com.rsv.learn.productservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsv.learn.productservice.dto.ProductRequest;
import com.rsv.learn.productservice.repository.ProductRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductServiceApplicationTests {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private ProductRepository productRepository;

	@Container
	final static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0.3"));

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
		dynamicPropertyRegistry.add("spring.data.mongodb.uri",mongoDBContainer::getReplicaSetUrl);
	}

	@BeforeAll
	public static void init() {
		mongoDBContainer.start();
	}

	@AfterAll
	public static void destroy() {
		mongoDBContainer.stop();
	}

	@Test
	@Order(1)
	void shouldCreateProduct() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
						.contentType(MediaType.APPLICATION_JSON)
						.content(productRequestString))
				.andExpect(status().isCreated());

		Assert.assertEquals(1, productRepository.findAll().size());
	}

	@Test
	@Order(2)
	void shouldGetAllProducts() throws Exception {
		ProductRequest productRequest = getProductRequest();
		String productRequestString = objectMapper.writeValueAsString(productRequest);

		mockMvc.perform(MockMvcRequestBuilders.get("/api/product"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.*").isArray())
				.andExpect(jsonPath("$[0].id").exists())
				.andExpect(jsonPath("$[0].name").value("IPhone 13"))
				.andExpect(jsonPath("$[0].description").value("IPhone 13"))
				.andExpect(jsonPath("$[0].price").value("50000"));
	}

	private ProductRequest getProductRequest() {
		return ProductRequest.builder()
				.name("IPhone 13")
				.description("IPhone 13")
				.price(BigDecimal.valueOf(50000))
				.build();
	}

}
