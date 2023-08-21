package files;

import static io.restassured.RestAssured.*;

import java.io.File;

import org.testng.Assert;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;

public class JiraTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		RestAssured.baseURI = "http://localhost:5050/";
		
		// Add SessionFilter class and create session object
		SessionFilter session = new SessionFilter();
		
		// Log in to Jira to create session
		given().relaxedHTTPSValidation().header("Content-Type", "application/json").body("{ \"username\": \"{username}\", \"password\": \"{password}\" }")
		.log().all().filter(session).when().post("/rest/auth/1/session").then().log().all().extract().response().asString();
		
		
		// Add comment to existing issue using add comment API
		String expectedMessage = "Hi how are you?";

		String addCommentResponse = given().pathParam("id", "10004").log().all().header("Content-Type", "application/json").body("{\n"
				+ "    \"body\": \""+expectedMessage+"\",\n"
				+ "    \"visibility\": {\n"
				+ "        \"type\": \"role\",\n"
				+ "        \"value\": \"Administrators\"\n"
				+ "    }\n"
				+ "}")
		.filter(session).when().post("/rest/api/2/issue/{id}/comment")
		.then().log().all().assertThat().statusCode(201).extract().response().asString();
		
		JsonPath js = new JsonPath(addCommentResponse);
		String commentId = js.getString("id");
		
		// Add attachment
		given().header("X-Atlassian-Token", "no-check").pathParam("id", "10004")
		.header("Content-Type", "multipart/form-data").multiPart("file", new File("jira.txt"))
		.filter(session).when().post("/rest/api/2/issue/{id}/attachments")
		.then().log().all().assertThat().statusCode(200);
		
		
		// Get issue: Query and Path Params
		String issueDetails = given().log().all().filter(session).pathParam("id", "10004")
				.queryParam("fields", "comment")
		.when().get("/rest/api/2/issue/{id}")
		.then().log().all().extract().response().asString();
		System.out.println(issueDetails);
		
		JsonPath js1 = new JsonPath(issueDetails);
		// store count of comments in a variable
		int commentsCount = js1.getInt("fields.comment.comments.size()");
		
		// iterate through the comments and grab the id
		for (int i=0; i<commentsCount; i++) 
		{
//			System.out.println(js1.getInt("fields.comment.comments["+i+"].id"));
			
			String commentIdIssue = js1.get("fields.comment.comments["+i+"].id").toString();
			if(commentIdIssue.equalsIgnoreCase(commentId))
			{
				String message = js1.get("fields.comment.comments["+i+"].body").toString();
				System.out.println(message);
				
				Assert.assertEquals(message, expectedMessage);
			}
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		

	}

}
