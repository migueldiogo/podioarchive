package com.podioarchive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import com.podio.APIApplicationException;
import com.podio.APIFactory;
import com.podio.RateLimits;
import com.podio.ResourceFactory;
import com.podio.contact.Profile;
import com.podio.oauth.OAuthClientCredentials;
import com.podio.oauth.OAuthUsernameCredentials;
import com.podio.user.UserAPI;


public class Main {

    public static void main(String[] args) {

        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream("resources/config.properties"));

            Date currentTime = new Date();
            SimpleDateFormat parserDate = new SimpleDateFormat("yyyyMMddHHmmss");
            String currentTimeFormatted = parserDate.format(currentTime);
            Session.setArchiveURL(properties.getProperty("archiveURL") + File.separator + currentTimeFormatted + "_PodioArchive");
            Session.setClientId(properties.getProperty("clientID"));
            Session.setClientSecret(properties.getProperty("clientSecret"));

            File archiveFolder = new File(Session.getArchiveURL());

            if (archiveFolder.exists()) {
                System.out.println("An error occur... please try again.");
                System.exit(1);
            }

            archiveFolder.mkdir();

        } catch (IOException e) {
            if (args.length != 3) {
                System.out.println("No properties file found. Please rerun the program with the following arguments: \n" +
                        "[Archive Destination Folder URL] [Client ID] [Client Secret];\n");
                System.exit(1);
            } else {
                Session.setArchiveURL(args[0]);
                Session.setClientId(args[1]);
                Session.setClientSecret(args[2]);
            }
        }

        boolean success = false;

        while (!success) {
            // authentication input
            Scanner scanner = new Scanner(System.in);
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            // start tracking rate limits
            RateLimits.resetTracking();

            // authentication process
            ResourceFactory resourceFactory = new ResourceFactory(
                    new OAuthClientCredentials(Session.getClientId(), Session.getClientSecret()),
                    new OAuthUsernameCredentials(username, password));

            // update session with api factory
            APIFactory apiFactory = new APIFactory(resourceFactory);
            Session.setApiFactory(apiFactory);

            // instantiation of User Api
            UserAPI userAPI = apiFactory.getAPI(UserAPI.class);
            try {
                Profile profile = userAPI.getProfile();
                // welcome message
                System.out.println("Logged as " + profile.getName() + ".");
                success = true;
            } catch (APIApplicationException e) {
                System.out.println(e.getDescription());
            }
        }

        System.out.println("Press any key to start archiving...");
        try {
            System.in.read();
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Starting process...");

        Archive archive = new Archive();
        archive.start();    // archiving main routine

        System.out.println("Done!");
    }


}
