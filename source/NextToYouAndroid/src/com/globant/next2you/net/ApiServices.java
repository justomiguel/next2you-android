package com.globant.next2you.net;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.globant.next2you.objects.CreateCommunityRequest;
import com.globant.next2you.objects.CreateTravelRequest;
import com.globant.next2you.objects.CreateTravelResponse;
import com.globant.next2you.objects.CreateUserTokenRequest;
import com.globant.next2you.objects.CreateUserTokenResponse;
import com.globant.next2you.objects.GetCummunitiesResponse;
import com.globant.next2you.objects.ListDestinationsResponse;
import com.globant.next2you.objects.ListPeopleResponse;
import com.globant.next2you.objects.PasswordResetRequest;
import com.globant.next2you.objects.Person;
import com.globant.next2you.objects.RegisterUserRequest;
import com.globant.next2you.objects.ResetCurrentUserPasswordRequest;
import com.globant.next2you.objects.RetrieveApplicationVersionResponse;
import com.globant.next2you.objects.RetrievePendingTravelsResponse;
import com.globant.next2you.objects.Travel;
import com.globant.next2you.objects.TravelPersonResponse;
import com.globant.next2you.objects.UpdateUserTokenRequest;

public class ApiServices {
	private static final String TAG = "ApiServices";
	@SuppressWarnings("unused")
	private static final String API_URL_MOCK = "http://next2you.apiary-mock.com/api/";
	/*
	 * user:jack.next2you@gmail.com pass:Next2you
	 */
	private static final String API_URL_REAL = "https://next2you.globant.com:8443/api/";
	public static final String API_URL = API_URL_REAL;

	public static RetrieveApplicationVersionResponse retrieveApplicationVersion()
			throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "applicationVersions");
		String output = query.send("GET");
		if (query.getResponseStatusCode() == 200) {
			return new ObjectMapper().readValue(output,
					RetrieveApplicationVersionResponse.class);
		}
		return null;
	}

	public static String selectCommunity(String token, int communityId)
			throws Exception {
		HTTPQuery query = new HTTPQuery(API_URL + "userTokens?set=true");
		query.setContentType("application/json");
		JSONObject json = new JSONObject();
		json.put("token", token);
		json.put("communityId", communityId);
		String jsonRequest = json.toString();
		query.setEntity(new StringEntity(jsonRequest));
		query.setAuthorizationToken(token);

		String output = query.send("POST");
		return output;
	}

	public static CreateUserTokenResponse createOrUpdateUserToken(
			CreateUserTokenRequest request) throws IOException {
		String jsonRequest = new ObjectMapper().writeValueAsString(request);
		HTTPQuery query = new HTTPQuery(API_URL + "userTokens");
		query.setContentType("application/json");
		query.setEntity(new StringEntity(jsonRequest));

		String output = query.send("PUT");

		Log.d(TAG, "createOrUpdateUserToken result=" + output);
		if (query.getResponseStatusCode() == 201) {
			return new ObjectMapper().readValue(output,
					CreateUserTokenResponse.class);
		}
		return null;
	}

	public static boolean updateUserToken(String currentToken,
			UpdateUserTokenRequest request) throws IOException {
		String jsonRequest = new ObjectMapper().writeValueAsString(request);

		HTTPQuery query = new HTTPQuery(API_URL + "userTokens");
		query.setContentType("application/json");
		query.setEntity(new StringEntity(jsonRequest));
		query.setAuthorizationToken(currentToken);

		query.send("POST");

		return query.getResponseStatusCode() == 200;
	}

	public static boolean passwordReset(PasswordResetRequest request)
			throws IOException {
		String jsonRequest = new ObjectMapper().writeValueAsString(request);

		HTTPQuery query = new HTTPQuery(API_URL + "passwordReset");
		query.setContentType("application/json");
		query.setEntity(new StringEntity(jsonRequest));
		query.send("PUT");

		int code = query.getResponseStatusCode();
		Log.d(TAG, "passwordReset resultcode=" + code + ";jsonRequest="
				+ jsonRequest);
		return code == 200;
	}

	public static boolean resetCurrentUserPassword(String currentToken,
			ResetCurrentUserPasswordRequest request) throws IOException {
		String jsonRequest = new ObjectMapper().writeValueAsString(request);

		HTTPQuery query = new HTTPQuery(API_URL + "passwordReset");
		query.setContentType("application/json");
		query.setEntity(new StringEntity(jsonRequest));
		query.setAuthorizationToken(currentToken);
		query.send("POST");

		return query.getResponseStatusCode() == 200;
	}

	/* Currently the returned JSON is not well formated, so it cannot be parsed */
	public static GetCummunitiesResponse getCommunities() throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "communities");
		String output = query.send("GET");

		if (query.getResponseStatusCode() == 200) {
			return new ObjectMapper().readValue(output,
					GetCummunitiesResponse.class);
		}
		return null;
	}

	public static Person getPersonInfo(String token, long travelPersonId)
			throws Exception {
		HTTPQuery query = new HTTPQuery(API_URL + "people/" + travelPersonId);
		query.setAuthorizationToken(token);
		query.setContentType("text/plain");

		String output = query.send("GET");
		if (query.getResponseStatusCode() == 200) {
			return new ObjectMapper().readValue(output, Person.class);
		}
		return null;
	}

	public static List<Person> getPeople(String token, double nELat,
			double nELon, double sWLat, double swLon, boolean withTravelFlag)
			throws Exception {
		Log.d(TAG, String.format(Locale.US,
				"getPeople nELat=%f nELon=%f sWLat=%f swLon=%f", nELat, nELon,
				sWLat, swLon));
		HTTPQuery query = new HTTPQuery(API_URL + "people");
		query.setAuthorizationToken(token);
		query.addParam("northEastLatitude", String.valueOf(nELat));
		query.addParam("northEastLongitude", String.valueOf(nELon));
		query.addParam("southWestLatitude", String.valueOf(sWLat));
		query.addParam("southWestLongitude", String.valueOf(swLon));
		query.addParam("withTravel", String.valueOf(withTravelFlag));
		String output = query.send("GET");

		if (query.getResponseStatusCode() == 200) {
			ObjectMapper mapper = new ObjectMapper();
			JavaType type = mapper.getTypeFactory().constructCollectionType(
					List.class, Person.class);
			JSONArray people = new JSONObject(output).getJSONArray("people");
			// Log.d(TAG, "people:" + people);
			List<Person> result = mapper.readValue(people.toString(), type);
			return result;
		}
		return null;
	}

	public static Person getPerson(String token) throws Exception {
		HTTPQuery query = new HTTPQuery(API_URL + "people");
		query.setAuthorizationToken(token);
		String output = query.send("GET");
		if (query.getResponseStatusCode() == 200) {
			ObjectMapper mapper = new ObjectMapper();
			Person result = mapper.readValue(new JSONObject(output).toString(),
					Person.class);
			return result;
		}
		return null;
	}

	public static boolean updateLoggedPerson(String token) throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "people");
		query.setAuthorizationToken(token);
		query.send("POST");

		return query.getResponseStatusCode() == 200;
	}

	public static String updatePerson(String token, Person p)
			throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "people");
		query.setAuthorizationToken(token);
		query.addParam("personId", String.valueOf(p.getPersonId()));
		query.addParam("email", p.getEmail());
		query.addParam("nickname", p.getNickname());
		query.addParam("comments", p.getComments());
		query.addParam("address", p.getAddress());
		query.addParam("destinationId", String.valueOf(p.getDestinationId()));
		query.addParam("isAddressVisible", p.getIsAddressVisible() ? "true"
				: "false");
		return query.send("POST");
	}

	/* Currently the returned JSON is not well formated, so it cannot be parsed */
	public static ListPeopleResponse listPeople(String nickname)
			throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "people?nickname=" + nickname);
		String output = query.send("GET");

		if (query.getResponseStatusCode() == 200) {
			return new ObjectMapper().readValue(output,
					ListPeopleResponse.class);
		}
		return null;
	}

	/* Currently the returned JSON is not well formated, so it cannot be parsed */
	public static ListPeopleResponse listPeopleByCoordinates(
			String currentToken, String northEastLatitude,
			String northEastLongitude, String southWestLatitude,
			String southWestLongitude, String withTravel) throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "pople?northEastLatitude="
				+ northEastLatitude + "&northEastLongitude="
				+ northEastLongitude + "&southWestLatitude="
				+ southWestLatitude + "&southWestLongitude="
				+ southWestLongitude + "&withTravel=" + withTravel);
		query.setAuthorizationToken(currentToken);
		String output = query.send("GET");

		if (query.getResponseStatusCode() == 200) {
			return new ObjectMapper().readValue(output,
					ListPeopleResponse.class);
		}
		return null;
	}

	/* Currently the returned JSON is not well formated, so it cannot be parsed */
	public static GetCummunitiesResponse listPersonCommunities(
			String currentToken) throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "personCommunities");
		query.setAuthorizationToken(currentToken);
		String output = query.send("GET");

		if (query.getResponseStatusCode() == 200) {
			return new ObjectMapper().readValue(output,
					GetCummunitiesResponse.class);
		}
		return null;
	}

	public static boolean createCommunity(CreateCommunityRequest request)
			throws IOException {
		String jsonRequest = new ObjectMapper().writeValueAsString(request);

		HTTPQuery query = new HTTPQuery(API_URL + "communityEntries");
		query.setContentType("application/json");
		query.setEntity(new StringEntity(jsonRequest));
		query.send("PUT");

		return query.getResponseStatusCode() == 201;
	}

	public static ListDestinationsResponse listDestinations(String currentToken)
			throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "destinations");
		query.setAuthorizationToken(currentToken);
		String output = query.send("GET");
		if (query.getResponseStatusCode() == 200) {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(
					DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			return objectMapper.readValue(output,
					ListDestinationsResponse.class);
		}
		return null;
	}

	/* No idea what will be the format of the image */
	public static String retrieveImage(String uid) throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "images/" + uid);
		String output = query.send("GET");
		if (query.getResponseStatusCode() == 200) {
			return output;
		}
		return null;
	}

	public static void resetPass(String oldPass, String newPass, String token)
			throws IOException {
		try {
			JSONObject request = new JSONObject();
			request.put("oldPassword", oldPass);
			request.put("newPassword", newPass);

			String jsonRequest = new ObjectMapper().writeValueAsString(request);

			HTTPQuery query = new HTTPQuery(API_URL + "passwordReset");
			query.setAuthorizationToken(token);
			query.setContentType("application/json");
			query.setEntity(new StringEntity(jsonRequest));
			String output = query.send("POST");
			Log.d(TAG, "output=" + output);
			return;
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
	}

	public static TravelPersonResponse listSimpleTravels(String currentToken)
			throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "simpleTravels");
		query.setAuthorizationToken(currentToken);
		query.setContentType("application/json");
		query.setEntity(new StringEntity(""));
		String output = query.send("GET");
		Log.d(TAG, "CODE:" + query.getResponseStatusCode());
		Log.d(TAG, "output:" + output);
		if (query.getResponseStatusCode() == 200) {
			return new ObjectMapper().readValue(output,
					TravelPersonResponse.class);
		}
		return null;
	}

	public static CreateTravelResponse createTravel(String currentToken,
			CreateTravelRequest request) throws IOException {
		String jsonRequest = new ObjectMapper().writeValueAsString(request);

		HTTPQuery query = new HTTPQuery(API_URL + "simpleTravels");
		query.setAuthorizationToken(currentToken);
		query.setContentType("application/json");
		query.setEntity(new StringEntity(jsonRequest));
		String output = query.send("PUT");
		if (query.getResponseStatusCode() == 201) {
			return new ObjectMapper().readValue(output,
					CreateTravelResponse.class);
		}
		return null;
	}

	/* Currently the returned JSON is not well formated, so it cannot be parsed */
	public static RetrievePendingTravelsResponse retrievePendingTravels(
			String currentToken) throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "travelPeople");
		query.setAuthorizationToken(currentToken);
		String output = query.send("GET");

		if (query.getResponseStatusCode() == 200) {
			return new ObjectMapper().readValue(output,
					RetrievePendingTravelsResponse.class);
		}
		return null;
	}

	public static boolean subscribeForTravel(String currentToken,
			String travelId) throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "travelPeople?travelId="
				+ travelId);
		query.setAuthorizationToken(currentToken);
		query.send("PUT");

		return query.getResponseStatusCode() == 201;

	}

	public static boolean approveOrRejectForTravel(String token,
			int travelPersonId, boolean approve) throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "travelPeople?travelPersonId");
		query.addParam("travelPersonId", Integer.valueOf(travelPersonId)
				.toString());
		query.addParam("approve", Boolean.valueOf(approve).toString());

		query.send("POST");
		return query.getResponseStatusCode() == 201;

	}

	public static boolean registerUser(RegisterUserRequest request)
			throws IOException {
		String jsonRequest = new ObjectMapper().writeValueAsString(request);

		HTTPQuery query = new HTTPQuery(API_URL + "userRegistrations");
		query.setContentType("application/json");
		query.setEntity(new StringEntity(jsonRequest));
		query.send("PUT");
		return query.getResponseStatusCode() == 201;
	}
}
