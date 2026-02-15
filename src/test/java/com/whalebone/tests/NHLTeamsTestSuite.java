package com.whalebone.tests;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.*;

import java.util.*;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.*;
import static org.testng.Assert.*;

public class NHLTeamsTestSuite {
    private static final String API_BASE_URI = "https://qa-assignment.dev1.whalebone.io";
    private static final String API_ENDPOINT = "/api/teams";

    private static final int EXPECTED_TEAMS_COUNT = 32;
    private static final String EXPECTED_OLDEST_TEAM = "Montreal Canadiens";
    private static final int EXPECTED_CITY_WITH_MORE_THAN_ONE_TEAM_COUNT = 1;
    private static final List<String> EXPECTED_TEAMS_IN_SAME_CITY = Arrays.asList(
            "New York Islanders",
            "New York Rangers"
        );
    private static final int EXPECTED_TEAMS_IN_METROPOLITAN_DIVISION_COUNT = 8;
    private static final List<String> EXPECTED_TEAMS_IN_METROPOLITAN_DIVISION = Arrays.asList(
            "Carolina Hurricanes",
            "Columbus Blue Jackets",
            "New Jersey Devils",
            "New York Islanders",
            "New York Rangers",
            "Philadelphia Flyers",
            "Pittsburgh Penguins",
            "Washington Capitals"
        );
    
    private JsonNode teamsData;
    private ObjectMapper objectMapper;

    private Playwright playwright;
    private Browser browser;
    
    @BeforeClass
    public void setup() {
        RestAssured.baseURI = API_BASE_URI;
        objectMapper = new ObjectMapper();

        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterClass
    public void teardown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeMethod
    public void fetchTeamsData() throws Exception {
        Response apiResponse = given()
            .when()
            .get(API_ENDPOINT)
            .then()
            .statusCode(200)
            .extract().response();
        
        teamsData = objectMapper.readTree(apiResponse.asString());
    }

    @Test
    public void testTotalTeamsCount() {
        int teamsCount = getTeams().size();
        assertEquals(teamsCount, EXPECTED_TEAMS_COUNT);
    }

    @Test
    public void testOldestTeam() {
        String oldestTeam = findOldestTeam(getTeams());
        assertEquals(oldestTeam, EXPECTED_OLDEST_TEAM);
    }

    @Test
    public void testCityWithMultipleTeams() {
        Map<String, List<String>> teamsByCity = arrangeTeamsByCity(getTeams());
        List<String> citiesWithMultipleTeams = findCitiesWithMultipleTeams(teamsByCity);

        assertEquals(citiesWithMultipleTeams.size(), EXPECTED_CITY_WITH_MORE_THAN_ONE_TEAM_COUNT);
        
        String cityWithMultipleTeams = citiesWithMultipleTeams.getFirst();
        List<String> sameCityTeams = teamsByCity.get(cityWithMultipleTeams);

        assertEqualsNoOrder(sameCityTeams, EXPECTED_TEAMS_IN_SAME_CITY);
    }

    @Test
    public void testMetropolitanDivisionTeams() {
        List<String> metropolitanTeams = listMetropolitanTeams(getTeams());

        assertEquals(metropolitanTeams.size(), EXPECTED_TEAMS_IN_METROPOLITAN_DIVISION_COUNT);
        assertEqualsNoOrder(metropolitanTeams, EXPECTED_TEAMS_IN_METROPOLITAN_DIVISION);
    }

    @Test
    public void testOldestTeamRosterNationality() {
        BrowserContext context = null;
        Page page = null;
        
        try {
            String rosterUrl = findRosterUrlOfTeam(getTeams(), EXPECTED_OLDEST_TEAM);

            context = browser.newContext();
            page = context.newPage();
            
            page.navigate(rosterUrl);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            
            int canadianPlayers = page.locator("td:has-text('CAN')").count();
            int usaPlayers = page.locator("td:has-text('USA')").count();

            assertTrue(canadianPlayers > usaPlayers);

        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    // Helpers

    private JsonNode getTeams() {
        return teamsData.get("teams");
    }

    private String findOldestTeam(JsonNode teams) {
        String oldestTeamName = null;
        int oldestYear = Integer.MAX_VALUE;
        for (JsonNode team : teams) {
            int founded = team.get("founded").asInt();
            if (founded < oldestYear) {
                oldestYear = founded;
                oldestTeamName = team.get("name").asText();
            }
        }
        return oldestTeamName;
    }

    private Map<String, List<String>> arrangeTeamsByCity(JsonNode teams) {
        Map<String, List<String>> teamsByCity = new HashMap<>();        

        for (JsonNode team : teams) {
            String location = team.get("location").asText();
            String teamName = team.get("name").asText();
            teamsByCity.computeIfAbsent(location, k -> new ArrayList<>()).add(teamName);
        }
        return teamsByCity;
    }

    private List<String> findCitiesWithMultipleTeams(Map<String, List<String>> teamsByCity) {
        return teamsByCity.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private List<String> listMetropolitanTeams(JsonNode teams) {
        List<String> metropolitanTeams = new ArrayList<>();

        for (JsonNode team : teams) {
            String divisionName = team.get("division").get("name").asText();
            if (divisionName.equals("Metropolitan")) {
                metropolitanTeams.add(team.get("name").asText());
            }
        }
        return metropolitanTeams;
    }

    private String findRosterUrlOfTeam(JsonNode teams, String teamName) {
        for (JsonNode team : teams) {
            if (team.get("name").asText().equals(teamName)) {
                return team.get("officialSiteUrl").asText() + "roster";
            }
        }
        throw new IllegalArgumentException("Team not found: " + teamName);
    }
}
