package org.opensrp.web.rest.it;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensrp.domain.Address;
import org.opensrp.domain.Client;
import org.opensrp.domain.Event;
import org.opensrp.repository.AllClients;
import org.opensrp.repository.AllEvents;
import org.opensrp.service.EventService;
import org.opensrp.web.rest.EventResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.server.MockMvc;
import org.springframework.test.web.server.MvcResult;
import org.springframework.test.web.server.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.opensrp.common.AllConstants.BaseEntity.BASE_ENTITY_ID;
import static org.opensrp.common.AllConstants.Event.EVENT_TYPE;
import static org.opensrp.common.AllConstants.Event.PROVIDER_ID;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.server.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.server.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = TestWebContextLoader.class, locations = {
		"classpath:spring/applicationContext-opensrp-web.xml" })
public class EventResourceTest {

	String baseEntityId = "1";

	String eventType = "eventType";

	DateTime eventDate = new DateTime(0l, DateTimeZone.UTC);

	String entityType = "entityType";

	String providerId = "providerId";

	String locationId = "locationId";

	String formSubmissionId = "formSubmissionId";


	String addressType = "addressType";

	String country = "country";

	String stateProvince = "stateProvince";

	String cityVillage = "cityVillage";

	String countryDistrict = "countryDistrict";

	String subDistrict = "subDistrict";

	String town = "town";

	private final static String BASE_URL = "/rest/event/";

	@Autowired
	private WebApplicationContext wac;

	MockMvc mockMvc;

	@Autowired
	private AllEvents allEvents;

	@Autowired
	private AllClients allClients;

	@Autowired
	private EventService eventService;

	@Autowired
	private EventResource eventResource;

	ObjectMapper mapper = new ObjectMapper();

	@Before
	public void setUp() {
		allEvents.removeAll();
		allClients.removeAll();
	}

	@After
	public void tearDown() {
		//allEvents.removeAll();
		//allClients.removeAll();
	}

	@Test
	public void testRequiredProperties() {
		List<String> requiredProperties = eventResource.requiredProperties();
		assertTrue(requiredProperties.contains(PROVIDER_ID));
		assertTrue(requiredProperties.contains(EVENT_TYPE));
		assertTrue(requiredProperties.contains(BASE_ENTITY_ID));
	}

	@Test
	public void shouldFindEventById() throws Exception {
		Event expectedEvent = new Event("1", "eventType", new DateTime(0l, DateTimeZone.UTC), "entityType", "providerId",
				"locationId", "formSubmissionId");
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		createEvent(asList(expectedEvent));

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		MvcResult mvcResult = this.mockMvc.perform(get(BASE_URL + "value").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(print()).andReturn();

		String responseString = mvcResult.getResponse().getContentAsString();
		JsonNode actualObj = mapper.readTree(responseString);
		Event actualEvent = mapper.treeToValue(actualObj, Event.class);

		assertEquals(expectedEvent, actualEvent);

	}

	@Test
	public void shouldNotFindEvent() throws Exception {
		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		MvcResult mvcResult = this.mockMvc.perform(get(BASE_URL + "value").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andDo(print()).andReturn();
		String responseString = mvcResult.getResponse().getContentAsString();
		assertTrue(responseString.isEmpty());
	}

	@Test
	public void shouldCreateEventWithAllRequiredProperties() throws Exception {
		Event expectedEvent = new Event("1", "eventType", new DateTime(0l, DateTimeZone.UTC), "entityType", "providerId",
				"locationId", "formSubmissionId");
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		this.mockMvc.perform(
				post(BASE_URL).contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsBytes(expectedEvent))
						.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
		List<Event> allEventsInDB = allEvents.getAll();
		Event actualEvent = allEventsInDB.get(0);
		actualEvent.setDateCreated(null); //So We don't need to mock DateTimeUtil.now()
		assertEquals(1, allEventsInDB.size());
		assertEquals(expectedEvent, actualEvent);
	}

	@Test(expected = NestedServletException.class)
	public void shouldNotCreateEventWithOutProviderId() throws Exception {
		Event expectedEvent = new Event("1", "eventType", new DateTime(0l, DateTimeZone.UTC), "entityType", "providerId",
				"locationId", "formSubmissionId");
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		expectedEvent.setProviderId(null);

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		this.mockMvc.perform(
				post(BASE_URL).contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsBytes(expectedEvent))
						.accept(MediaType.APPLICATION_JSON));

		List<Event> allEventsInDb = allEvents.getAll();
		assertEquals(0, allEventsInDb.size());
	}

	@Test(expected = NestedServletException.class)
	public void shouldNotCreateEventWithOutEventType() throws Exception {
		Event expectedEvent = new Event("1", "eventType", new DateTime(0l, DateTimeZone.UTC), "entityType", "providerId",
				"locationId", "formSubmissionId");
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		expectedEvent.setEventType(null);

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		this.mockMvc.perform(
				post(BASE_URL).contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsBytes(expectedEvent))
						.accept(MediaType.APPLICATION_JSON));

		List<Event> allEventsInDb = allEvents.getAll();
		assertEquals(0, allEventsInDb.size());
	}

	@Test(expected = NestedServletException.class)
	public void shouldNotCreateEventWithOutBaseEntityId() throws Exception {
		Event expectedEvent = new Event("1", "eventType", new DateTime(0l, DateTimeZone.UTC), "entityType", "providerId",
				"locationId", "formSubmissionId");
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		expectedEvent.setBaseEntityId(null);

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		this.mockMvc.perform(
				post(BASE_URL).contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsBytes(expectedEvent))
						.accept(MediaType.APPLICATION_JSON));

		List<Event> allEventsInDb = allEvents.getAll();
		assertEquals(0, allEventsInDb.size());
	}

	@Test
	public void shouldUpdateExistingClient() throws Exception {
		Event expectedEvent = new Event("1", "eventType", new DateTime(0l, DateTimeZone.UTC), "entityType", "providerId",
				"locationId", "formSubmissionId");
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		createEvent(asList(expectedEvent));

		expectedEvent.addDetails("detail", "value");
		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		this.mockMvc.perform(post(BASE_URL + "value").contentType(MediaType.APPLICATION_JSON)
				.body(mapper.writeValueAsBytes(expectedEvent)).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		List<Event> allEventsInDb = allEvents.getAll();
		Event actualEvent = allEventsInDb.get(0);
		actualEvent.setDateEdited(null); //So We don't need to mock DateTimeUtil.now()
		assertEquals(1, allEventsInDb.size());
		assertEquals(expectedEvent, actualEvent);
	}

	@Test(expected = NestedServletException.class)
	public void shouldThrowExceptionWhileUpdateIfClientNotFound() throws Exception {
		Event expectedEvent = new Event("1", "eventType", new DateTime(0l, DateTimeZone.UTC), "entityType", "providerId",
				"locationId", "formSubmissionId");
		expectedEvent.addDetails("detail", "value");

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		this.mockMvc.perform(
				post(BASE_URL + "1").contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsBytes(expectedEvent))
						.accept(MediaType.APPLICATION_JSON));

		List<Event> allEventsInDb = allEvents.getAll();
		assertEquals(0, allEventsInDb.size());

	}

	@Test(expected = NestedServletException.class)
	public void shouldThrowExceptionWhileUpdateIfBaseEntityIdNotPresent() throws Exception {
		Event expectedEvent = new Event("1", "eventType", new DateTime(0l, DateTimeZone.UTC), "entityType", "providerId",
				"locationId", "formSubmissionId");
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		expectedEvent.setBaseEntityId(null);
		createEvent(asList(expectedEvent));
		Event updatedEvent = expectedEvent;
		updatedEvent.addDetails("key", "value");

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		this.mockMvc.perform(
				post(BASE_URL + "value").contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsBytes(updatedEvent))
						.accept(MediaType.APPLICATION_JSON));

		List<Event> allEventsInDb = allEvents.getAll();
		Event actualEvent = allEventsInDb.get(0);
		assertEquals(1, allEventsInDb.size());
		assertEquals(expectedEvent, actualEvent);
		assertNotSame(updatedEvent, actualEvent);
	}

	@Test(expected = NestedServletException.class)
	public void shouldThrowExceptionWhileUpdateIfEventTypeNotPresent() throws Exception {
		Event expectedEvent = new Event("1", "eventType", new DateTime(0l, DateTimeZone.UTC), "entityType", "providerId",
				"locationId", "formSubmissionId");
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		expectedEvent.setEventType(null);
		createEvent(asList(expectedEvent));
		Event updatedEvent = expectedEvent;
		updatedEvent.addDetails("key", "value");

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		this.mockMvc.perform(
				post(BASE_URL + "value").contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsBytes(updatedEvent))
						.accept(MediaType.APPLICATION_JSON));

		List<Event> allEventsInDb = allEvents.getAll();
		Event actualEvent = allEventsInDb.get(0);
		assertEquals(1, allEventsInDb.size());
		assertEquals(expectedEvent, actualEvent);
		assertNotSame(updatedEvent, actualEvent);
	}

	@Test(expected = NestedServletException.class)
	public void shouldThrowExceptionWhileUpdateIfProviderIdNotPresent() throws Exception {
		Event expectedEvent = new Event("1", "eventType", new DateTime(0l, DateTimeZone.UTC), "entityType", "providerId",
				"locationId", "formSubmissionId");
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		expectedEvent.setProviderId(null);
		createEvent(asList(expectedEvent));
		Event updatedEvent = expectedEvent;
		updatedEvent.addDetails("key", "value");

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		this.mockMvc.perform(
				post(BASE_URL + "value").contentType(MediaType.APPLICATION_JSON).body(mapper.writeValueAsBytes(updatedEvent))
						.accept(MediaType.APPLICATION_JSON));

		List<Event> allEventsInDb = allEvents.getAll();
		Event actualEvent = allEventsInDb.get(0);
		assertEquals(1, allEventsInDb.size());
		assertEquals(expectedEvent, actualEvent);
		assertNotSame(updatedEvent, actualEvent);
	}

	@Test
	@Ignore
	public void shouldSearchClientWithValidQuery() throws Exception {
		Event expectedEvent = new Event(baseEntityId, eventType, eventDate, entityType, providerId, locationId,
				formSubmissionId);
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		DateTime dateEdited = new DateTime(3l, DateTimeZone.UTC);
		expectedEvent.setDateEdited(dateEdited);

		Event otherEvent = new Event("2", eventType, eventDate, entityType, providerId, locationId, formSubmissionId);
		Event otherEvent2 = new Event("3", eventType, eventDate, entityType, providerId, locationId, formSubmissionId);
		createEvent(asList(expectedEvent, otherEvent, otherEvent2));

		String searchQuery =
				"search?" + "eventType=" + eventType + "&locationId=" + locationId + "&providerId=" + providerId;

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		MvcResult mvcResult = this.mockMvc.perform(get(BASE_URL + searchQuery)).andDo(print()).andReturn();

		String responseString = mvcResult.getResponse().getContentAsString();
		JsonNode actualObj = mapper.readTree(responseString);
		Event actualEvent = mapper.treeToValue(actualObj.get(0), Event.class);
		assertEquals(expectedEvent, actualEvent);
	}

	@Test
	public void shouldSearchClientWithInvalidQuery() throws Exception {
		Event expectedEvent = new Event(baseEntityId, eventType, eventDate, entityType, providerId, locationId,
				formSubmissionId);
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		DateTime dateEdited = new DateTime(3l, DateTimeZone.UTC);
		expectedEvent.setDateEdited(dateEdited);

		Event otherEvent = new Event("2", eventType, eventDate, entityType, providerId, locationId, formSubmissionId);
		Event otherEvent2 = new Event("3", eventType, eventDate, entityType, providerId, locationId, formSubmissionId);
		createEvent(asList(expectedEvent, otherEvent, otherEvent2));

		String searchQuery =
				"search?identifier= invalid" + "eventType=" + eventType + "&locationId=" + locationId + "&providerId="
						+ providerId;

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		MvcResult mvcResult = this.mockMvc.perform(get(BASE_URL + searchQuery)).andDo(print()).andReturn();

		String responseString = mvcResult.getResponse().getContentAsString();
		assertTrue(responseString.equals("[]"));
		JsonNode actualObj = mapper.readTree(responseString);
		assertNull(actualObj.get(0));
	}
	//TODO couch-lucene related test

	@Test
	public void shouldReturnAllEvents() throws Exception {
		Event expectedEvent = new Event(baseEntityId, eventType, eventDate, entityType, providerId, locationId,
				formSubmissionId);
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		DateTime dateEdited = new DateTime(3l, DateTimeZone.UTC);
		expectedEvent.setDateEdited(dateEdited);

		Event otherEvent = new Event("2", eventType, eventDate, entityType, providerId, locationId, formSubmissionId);
		Event otherEvent2 = new Event("3", eventType, eventDate, entityType, providerId, locationId, formSubmissionId);
		createEvent(asList(expectedEvent, otherEvent, otherEvent2));

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();
		MvcResult mvcResult = this.mockMvc.perform(get(BASE_URL + "getall")).andExpect(status().isOk()).andDo(print())
				.andReturn();

		String responseString = mvcResult.getResponse().getContentAsString();
		JsonNode actualObj = mapper.readTree(responseString);
		assertEquals(3, actualObj.size());
		List<Event> actualEventList = new ArrayList<>();
		for (int i = 0; i < actualObj.size(); i++) {
			Event actualEvent = mapper.treeToValue(actualObj.get(i), Event.class);
			actualEvent.setDateCreated(null);
			actualEventList.add(actualEvent);
		}

		assertTrue(asList(expectedEvent, otherEvent, otherEvent2).containsAll(actualEventList) && actualEventList
				.containsAll(asList(expectedEvent, otherEvent, otherEvent2)));
	}

	@Test
	public void shouldSyncEventAndRelatedClient() throws Exception {
		Address address = new Address().withAddressType(addressType).withCountry(country).withStateProvince(stateProvince)
				.withCityVillage(cityVillage).withCountyDistrict(countryDistrict).withSubDistrict(subDistrict).withTown(town);

		Client expectedClient = (Client) new Client("1").withFirstName("name").withGender("male")
				.withBirthdate(new DateTime(0l, DateTimeZone.UTC), false).withAddress(address);
		expectedClient.setDateCreated(new DateTime(0l, DateTimeZone.UTC));

		Client otherClient = (Client) new Client("2").withFirstName("name").withGender("male")
				.withBirthdate(new DateTime(0l, DateTimeZone.UTC), false).withAddress(address);
		otherClient.setDateCreated(new DateTime(0l, DateTimeZone.UTC));
		Client otherClient2 = (Client) new Client("3").withFirstName("name").withGender("male")
				.withBirthdate(new DateTime(0l, DateTimeZone.UTC), false).withAddress(address);
		otherClient2.setDateCreated(new DateTime(0l, DateTimeZone.UTC));

		createClient(asList(expectedClient, otherClient, otherClient2));

		Event expectedEvent = new Event(baseEntityId, eventType, eventDate, entityType, providerId, locationId,
				formSubmissionId);
		Map<String, String> identifiers = new HashMap<>();
		identifiers.put("key", "value");
		expectedEvent.setIdentifiers(identifiers);
		DateTime dateEdited = new DateTime(3l, DateTimeZone.UTC);
		expectedEvent.setDateCreated(dateEdited);

		Event oE = new Event("1", eventType, eventDate, entityType, providerId, locationId, formSubmissionId);
		oE.setDateCreated(dateEdited);
		Event otherEvent = new Event("2", eventType, eventDate, entityType, providerId, locationId, formSubmissionId);
		otherEvent.setDateCreated(dateEdited);
		Event otherEvent2 = new Event("3", eventType, eventDate, entityType, providerId, locationId, formSubmissionId);
		otherEvent2.setDateCreated(dateEdited);
		createEvent(asList(expectedEvent, otherEvent, otherEvent2, oE));

		this.mockMvc = MockMvcBuilders.webApplicationContextSetup(this.wac).build();

		MvcResult mvcResult = this.mockMvc.perform(get(BASE_URL + "sync?serverVersion=0&providerId=providerId")).andExpect(status().isOk()).andDo(print())
				.andReturn();

		String responseString = mvcResult.getResponse().getContentAsString();
		JsonNode actualObj = mapper.readTree(responseString);
		JsonNode eventObj = actualObj.get("events");
		JsonNode clientObj = actualObj.get("clients");
		int eventSize = actualObj.get("no_of_events").asInt();
		assertEquals(4, eventSize);
		List<Event> actualEventList = new ArrayList<>();
		for (int i = 0; i < eventObj.size(); i++) {
			Event actualEvent = mapper.treeToValue(eventObj.get(i), Event.class);
			actualEventList.add(actualEvent);
		}
		assertTrue(asList(oE,expectedEvent, otherEvent, otherEvent2).containsAll(actualEventList) && actualEventList
				.containsAll(asList(expectedEvent, otherEvent, otherEvent2, oE)));

		List<Client> actualClientList = new ArrayList<>();
		for (int i = 0; i < clientObj.size(); i++) {
			Client actualClient = mapper.treeToValue(clientObj.get(i), Client.class);
			actualClientList.add(actualClient);
		}
		assertEquals(actualClientList, asList(expectedClient, otherClient, otherClient2));

		assertTrue(asList(expectedClient, otherClient, otherClient2).containsAll(actualClientList) && actualClientList
				.containsAll(asList(expectedClient, otherClient, otherClient2)));
	}

	private void createEvent(List<Event> events) {
		for (Event event : events) {
			allEvents.add(event);
		}
	}

	private void createClient(List<Client> allClient) {
		for (Client client : allClient) {
			allClients.add(client);
		}
	}

}
