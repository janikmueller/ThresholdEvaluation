package com.cqse.thresholdevaluation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ThresholdEvaluation {

    private final OkHttpClient httpClient = new OkHttpClient();

    public static final String INPUT_ARGUMENT_PATTERN = "Please use the following input pattern:\n"
            + "thresholdevaluation <base-url> <project> <threshold-configuration>\n"
            + "Can be followed by optionals:\n--branch <branchname>\n--login <username> <password>\n--fail-on-yellow";

    public static void main(String[] args) throws IllegalArgumentException, IOException {

        if (args.length < 3) {
            if (args.length > 0) {
                if (args[0].equals("--help")) {
                    System.out.println(ThresholdEvaluation.INPUT_ARGUMENT_PATTERN);
                    System.out.println("The base-url must begin with 'http://' or 'https://' and end with '/'");
                    System.out.println("The project id usually has small-case letter only.");
                    System.exit(0);
                }
                if (args[0].equals("--version")) {
                    System.out.println("Version: 1.0");
                    System.exit(0);
                }
            }
            throw new IllegalArgumentException(ThresholdEvaluation.INPUT_ARGUMENT_PATTERN);
        }

        ThresholdEvaluation obj = new ThresholdEvaluation();
        String baseUrl = args[0], project = args[1], thresholdConfig = args[2], branch = "", username = "",
                password = "";
        boolean failOnYellow = false, login = false;

        for (int i = 3; i < args.length; i++) {
            if(args[i].equals("--branch")){
                if(args.length < i+2){
                    throw new IllegalArgumentException(ThresholdEvaluation.INPUT_ARGUMENT_PATTERN);
                }
                branch = args[++i];
                continue;
            }
            if (args[i].equals("--login")) {
                if (args.length < i + 3) {
                    throw new IllegalArgumentException(ThresholdEvaluation.INPUT_ARGUMENT_PATTERN);
                }
                username = args[++i];
                password = args[++i];
                login = true;
                continue;
            }
            if (args[i].equals("--fail-on-yellow")) {
                failOnYellow = true;
                continue;
            }
            throw new IllegalArgumentException(ThresholdEvaluation.INPUT_ARGUMENT_PATTERN);
        }

        String cookie = "";
        if (login) {
            cookie = obj.sendLogin(baseUrl, username, password);
        }

        String response = obj.sendGet(cookie, baseUrl, project, branch, thresholdConfig);

        if (obj.evaluateResponse(failOnYellow, response)) {
            System.exit(2);
        } else {
            System.out.println("All metrics passed the evaluation.");
            System.exit(0);
        }

    }

    private boolean evaluateResponse(boolean failOnYellow, String unparsedResponse) throws IOException {
        boolean failed = false;

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode response = objectMapper.readTree(unparsedResponse);

        for(JsonNode group : response){
            String groupRating = group.get("rating").asText();
            if(groupRating.equals("RED") || (failOnYellow && groupRating.equals("YELLOW"))){
                failed = true;
                System.out.println("Violation in group " + group.get("name") + ":");
                JsonNode metrics = group.get("metrics");
                for(JsonNode metric : metrics){
                    String metricRating = metric.get("rating").asText();
                    if (metricRating.equals("RED")) {
                        System.out.println(metricRating + " " + metric.get("displayName").asText() + ": red-threshold-value "
                                + metric.get("metricThresholds").get("thresholdRed").asDouble() + ", current-value "
                                + metric.get("formattedTextValue"));
                    } else if (failOnYellow && metricRating.equals("YELLOW")) {
                        System.out.println(metricRating + " " + metric.get("displayName").asText() + ": yellow-threshold-value "
                                + metric.get("metricThresholds").get("thresholdYellow").asDouble() + ", current-value "
                                + metric.get("formattedTextValue"));
                    }
                }
            }
        }
        return failed;
    }

    private String sendGet(String cookie, String baseUrl, String project, String branch, String thresholdConfig) throws UnsupportedEncodingException {

        String responseBody = "";

        String url = baseUrl + "api/projects/" + URLEncoder.encode(project, StandardCharsets.UTF_8.toString())
                + "/metric-assessments/?uniform-path=&configuration-name="
                + URLEncoder.encode(thresholdConfig, StandardCharsets.UTF_8.toString());

        if(!branch.equals("")) {
            url += "&t=" + URLEncoder.encode(branch, StandardCharsets.UTF_8.toString()) + "%3AHEAD";
        }

        Request request = new Request.Builder().url(url).addHeader("Cookie", cookie).build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            responseBody = response.body().string();
        } catch (Exception e){
            System.out.println(e);
            System.exit(1);
        }
        return responseBody;
    }

    private String sendLogin(String baseUrl, String username, String password) {

        String json = "{\"username\":\"" + username + "\", \"password\":\"" + password + "\", \"stayLoggedIn\":false}";
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);

        Request request = new Request.Builder().url(baseUrl + "login/").post(body).build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            if (!responseBody.equals("success")) {
                throw new IOException(responseBody);
            }

            String cookie = response.headers().get("Set-Cookie");
            return cookie;
        } catch (Exception e ) {
            System.out.println(e);
            System.exit(1);
        }
        return "";
    }

    private static final String command = "native-image -cp /Users/j.muller/.m2/repository/com/squareup/okhttp3/okhttp/3.14.2/okhttp-3.14.2.jar:/Users/j.muller/.m2/repository/com/squareup/okio/okio/1.17.2/okio-1.17.2.jar:/Users/j.muller/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.9.8/jackson-databind-2.9.8.jar:/Users/j.muller/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.9.0/jackson-annotations-2.9.0.jar:/Users/j.muller/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.9.8/jackson-core-2.9.8.jar:/Users/j.muller/Desktop/CQSE.nosync/Work/git_repo/MavenProject/okta-graalvm-example/jdk/target/okta-graal-example-jdk-1.0-SNAPSHOT.jar -H:Class=com.okta.examples.jdk.OkHttpExample -H:Name=thresholdEvaluation -H:+AddAllCharsets --no-fallback --enable-http --enable-https";
    private static final String additionalInfo = "in jdk folder, mvn package + command";

}