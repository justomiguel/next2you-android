package com.globant.next2you.net;

import java.io.IOException;

import org.apache.http.entity.StringEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.globant.next2you.Objects.CreateCommunityRequest;
import com.globant.next2you.Objects.CreateTravelRequest;
import com.globant.next2you.Objects.CreateTravelResponse;
import com.globant.next2you.Objects.CreateUserTokenRequest;
import com.globant.next2you.Objects.CreateUserTokenResponse;
import com.globant.next2you.Objects.GetCummunitiesResponse;
import com.globant.next2you.Objects.ListDestinationsResponse;
import com.globant.next2you.Objects.ListPeopleResponse;
import com.globant.next2you.Objects.PasswordResetRequest;
import com.globant.next2you.Objects.Person;
import com.globant.next2you.Objects.RegisterUserRequest;
import com.globant.next2you.Objects.ResetCurrentUserPasswordRequest;
import com.globant.next2you.Objects.RetrieveApplicationVersionResponse;
import com.globant.next2you.Objects.RetrievePendingTravelsResponse;
import com.globant.next2you.Objects.UpdateUserTokenRequest;

public class ApiServices {

	private static final String API_URL = "http://next2you.apiary-mock.com/api/";

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

	public static CreateUserTokenResponse createOrUpdateUserToken(
			CreateUserTokenRequest request) throws IOException {
		String jsonRequest = new ObjectMapper().writeValueAsString(request);
		HTTPQuery query = new HTTPQuery(API_URL + "userTokens");
		query.setContentType("application/json");
		query.setEntity(new StringEntity(jsonRequest));

		String output = query.send("PUT");

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

		return query.getResponseStatusCode() == 200;
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

	public static Person getPeople(String token) throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "people");
		query.setAuthorizationToken(token);
		String output = query.send("GET");

		if (query.getResponseStatusCode() == 200) {
			return new ObjectMapper().readValue(output, Person.class);
		}
		return null;
	}

	public static boolean updateLoggedPerson(String token) throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "people");
		query.setAuthorizationToken(token);
		query.send("POST");

		return query.getResponseStatusCode() == 200;
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

	/* Currently the returned JSON is not well formated, so it cannot be parsed */
	public static ListDestinationsResponse listDestinations(String currentToken)
			throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "destinations");
		query.setAuthorizationToken(currentToken);
		String output = query.send("GET");
		if (query.getResponseStatusCode() == 200) {
			return new ObjectMapper().readValue(output,
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
	public static RetrievePendingTravelsResponse retrievePendingTravels() throws IOException {
		HTTPQuery query = new HTTPQuery(API_URL + "travelPeople");
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

	public static boolean registerUser(RegisterUserRequest request) throws IOException {
		String jsonRequest = new ObjectMapper().writeValueAsString(request);
		
		HTTPQuery query = new HTTPQuery(API_URL + "userRegistrations");
		query.setContentType("application/json");
		query.setEntity(new StringEntity(jsonRequest));
		query.send("PUT");
		return query.getResponseStatusCode() == 201;
	}
}
