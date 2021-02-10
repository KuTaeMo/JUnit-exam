package com.cos.book.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.cos.book.domain.Book;
import com.cos.book.domain.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
public class BookControllerIntegreTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	private EntityManager entityManager;
	
	@BeforeEach
	public void init() {
		entityManager.createNativeQuery("ALTER TABLE Book AUTO_INCREMENT =1").executeUpdate();
	}
	
	@Test
	public void save_테스트() throws Exception {
		// given
		String content = new ObjectMapper().writeValueAsString(new Book(null, "springboot", 4.5,15000.0));

		// when
		ResultActions resultAction = mockMvc.perform(post("/book").contentType(MediaType.APPLICATION_JSON_UTF8)
				.content(content).accept(MediaType.APPLICATION_JSON_UTF8));

		// then
		resultAction.andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.rating").value(4.5)).andDo(MockMvcResultHandlers.print());
	} 
	
	@Test
	public void findAll_테스트() throws Exception {
		// given
		bookRepository.saveAll(Arrays.asList(new Book(null, "springboot", 4.5,15000.0), new Book(null, "android", 4.3,25000.0)));

		// when
		ResultActions resultAction = mockMvc.perform(get("/book").accept(MediaType.APPLICATION_JSON_UTF8));

		// then
		resultAction.andExpect(status().isOk()).andExpect(jsonPath("$.*", Matchers.hasSize(2))) // import static org.hamcrest.Matchers.*;																								
				.andExpect(jsonPath("$.[0].title").value("springboot")).andExpect(jsonPath("$.[0].price").value(15000.0))
				.andDo(MockMvcResultHandlers.print());
	}
	
	@Test
	public void findById_테스트() throws Exception {
		// given
		bookRepository.saveAll(Arrays.asList(new Book(null, "springboot", 4.5,15000.0), new Book(null, "android", 4.3,25000.0)));
		Long id = 2L;

		// when
		ResultActions resultAction = mockMvc.perform(get("/book/{id}", id).accept(MediaType.APPLICATION_JSON_UTF8));

		// then
		resultAction.andExpect(status().isOk()).andExpect(jsonPath("$.title").value("android"))
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void update_테스트() throws Exception {
		// given
		bookRepository.saveAll(Arrays.asList(new Book(null, "springboot", 4.5,15000.0), new Book(null, "android", 4.3,25000.0)));
		
		Long id = 1L;
		String content = new ObjectMapper().writeValueAsString(new Book(null, "JPA", 4.2,18000.0));

		// when
		ResultActions resultAction = mockMvc.perform(put("/book/{id}", id).content(content)
				.contentType(MediaType.APPLICATION_JSON_UTF8).accept(MediaType.APPLICATION_JSON_UTF8));

		// then
		resultAction.andExpect(status().isOk()).andExpect(jsonPath("$.title").value("JPA"))
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	public void delete_테스트() throws Exception {
		// given
		bookRepository.saveAll(Arrays.asList(new Book(null, "springboot", 4.5,15000.0), new Book(null, "android", 4.3,25000.0)));
		Long id = 1L;

		// when
		ResultActions resultAction = mockMvc.perform(delete("/book/{id}", id));

		// then
		resultAction.andExpect(status().isOk()).andDo(MockMvcResultHandlers.print());

		MvcResult requestResult = resultAction.andReturn();
		String result = requestResult.getResponse().getContentAsString();
		assertEquals("ok", result);
	}
}
