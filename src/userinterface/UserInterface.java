package userinterface;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UserInterface {

	private JSONObject user;

	private String doGetRequest(String url) throws ClientProtocolException,
			IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}

	private String doPostRequest(String urlToPost, String postedValue)
			throws ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpPost request = new HttpPost(urlToPost);
		request.setEntity(new StringEntity(postedValue));
		HttpResponse response = client.execute(request);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}

	private JSONArray readAllUsers() throws ClientProtocolException,
			IOException, JSONException {
		String newRequest = "http://10.218.223.84:5702/sdelab/person";

		String result = doGetRequest(newRequest);

		System.out.println("Chose a user to log in with: ");

		JSONArray userArray = new JSONArray(result.toString());
		JSONObject user;
		for (int i = 0; i < userArray.length(); i++) {
			user = new JSONObject(userArray.get(i).toString());
			System.out.println("\t" + user.get("firstname") + "  --  "
					+ (i + 1));
		}

		return userArray;
	}

	private void readUser(JSONArray userArray) throws IOException,
			JSONException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String chosenUser = reader.readLine();
		switch (chosenUser) {
		case "1":
			this.user = new JSONObject(userArray.get(0).toString());
			break;
		case "2":
			this.user = new JSONObject(userArray.get(1).toString());
			break;
		case "3":
			this.user = new JSONObject(userArray.get(2).toString());

			break;
		case "4":
			this.user = new JSONObject(userArray.get(3).toString());
			break;
		default:
			break;
		}

	}

	private void userProfile() throws JSONException {
		System.out.println("Profile ");
		System.out.println("  Firstname: " + user.get("firstname"));
		System.out.println("  Lastname: " + user.get("lastname"));
		System.out.println("  Birthdate: " + user.get("birthdate"));
		System.out.println("  E-mail: " + user.get("email"));
		System.out.println("  User name: " + user.get("username"));
	}

	private void currentLifeStatus() throws JSONException {
		System.out.println("Current Life Status ");
		JSONObject healthP = new JSONObject(user.get("healthProfile")
				.toString());
		if (!healthP.get("lifeStatus").toString().startsWith("[")) {
			System.out.println("No registered life status!");
			return;
		}
		JSONArray lifeArray = new JSONArray(healthP.get("lifeStatus")
				.toString());
		for (int i = 0; i < lifeArray.length(); i++) {
			JSONObject lifeS = new JSONObject(lifeArray.get(i).toString());
			JSONObject measure = new JSONObject(lifeS.get("measureType")
					.toString());
			System.out.println("  " + measure.get("name") + ": "
					+ lifeS.getDouble("value"));
		}
	}

	private void lastAchivements() throws JSONException {
		System.out.println("Last achivements ");

		if (!user.get("achivedGoals").toString().startsWith("[")) {
			System.out.println("No achived goals!");
			return;
		}
		JSONArray goalsArray = new JSONArray(user.get("achivedGoals")
				.toString());
		for (int i = 0; i < goalsArray.length(); i++) {
			JSONObject achived = new JSONObject(goalsArray.get(i).toString());
			JSONObject goal = new JSONObject(achived.get("achivedGoal")
					.toString());
			System.out.println("  " + goal.get("goalValue") + ": "
					+ achived.getString("achivementDate"));
		}
	}

	private void saveNewLifeStatus() throws IllegalStateException, IOException,
			JSONException {
		System.out.println("Which measure? ");
		String allMeasuresUrl = "http://10.218.223.84:5702/sdelab/measureTypes";
		String allMeasures = doGetRequest(allMeasuresUrl);
		JSONArray allMeasuresArray = new JSONArray(allMeasures);

		for (int i = 0; i < allMeasuresArray.length(); i++) {
			System.out.println("  " + allMeasuresArray.get(i).toString()
					+ "  --  " + (i + 1));
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		int option = Integer.parseInt(reader.readLine());
		String measureType = "";
		 if (option == 1) {
			measureType = allMeasuresArray.getString(0);
		}else if(option == 2){
			measureType = allMeasuresArray.getString(1);
		}
		System.out.println(measureType);
		System.out.println(this.user.getString("idPerson"));
		System.out.println("Give the new value:");
		String postedValue = reader.readLine();
		System.out.println(postedValue);
		String urlToPost = "http://10.218.223.84:5703/sdelab/person/"
				+ this.user.getString("idPerson") + "/" + measureType; //to PC

		// post sends the pic
		String picUrl = doPostRequest(urlToPost, postedValue);
		System.out.println(picUrl);

		// re-read the user, so it is with the new values
		String updated = doGetRequest("http://10.218.223.84:5702/sdelab/person/"
				+ this.user.getString("idPerson"));
		this.user = new JSONObject(updated);

	}

	private void personalInfo() throws ClientProtocolException, IOException,
			JSONException {
		boolean loggedIn = true;

		while (loggedIn) {
			System.out.println("\n ***** Logged in *****");
			System.out.println("User: " + user.get("firstname"));
			System.out.println("  See profile --  1");
			System.out.println("  See current life status --  2");
			System.out.println("  See last achivements --  3");
			System.out.println("  Register new life status --  4");
			System.out.println("  Logout --  99");
			System.out.println("Chose an option: ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));
			String option = reader.readLine();
			switch (option) {
			case "1":
				userProfile();
				break;
			case "2":
				currentLifeStatus();
				break;
			case "3":
				lastAchivements();
				break;
			case "4":
				saveNewLifeStatus();
				break;
			case "99":
				this.user = null;
				loggedIn = false;
				break;

			default:
				break;
			}

		}
	}

	public static void main(String[] args) {
		UserInterface ui = new UserInterface();
		boolean run = true;
		while (run) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.println("\tMenu: ");
			System.out.println("Login  -  1");

			System.out.println("Exit -  100");
			System.out.println("Chose an option: ");
			String option;
			try {
				option = reader.readLine();
				System.out.println("Your choice was: " + option);
				switch (option) {
				case "1":
					JSONArray userArray = ui.readAllUsers();
					ui.readUser(userArray);
					ui.personalInfo();
					break;
				case "100":
					run = false;
					break;
				default:
					break;
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
