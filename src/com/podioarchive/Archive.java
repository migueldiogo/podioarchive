package com.podioarchive;

import com.podio.APIFactory;
import com.podio.app.AppAPI;
import com.podio.app.Application;
import com.podio.comment.Comment;
import com.podio.comment.CommentAPI;
import com.podio.common.Reference;
import com.podio.common.ReferenceType;
import com.podio.file.FileAPI;
import com.podio.hook.Hook;
import com.podio.hook.HookAPI;
import com.podio.item.*;
import com.podio.org.OrgAPI;
import com.podio.org.OrganizationWithSpaces;
import com.podio.space.SpaceMini;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;

/**
 * Created by Miguel Prata Leal on 20/07/16.
 * This class is responsible for all the archiving process.
 */
public class Archive {
    private APIFactory apiFactory;

    public Archive() {
        apiFactory = Session.getApiFactory();
    }

    /**
     * Starting point
     */
    public void start() {
        archiveOrganizations(Session.getArchiveURL());
    }

    /**
     * get all Organizations
     * @return
     */
    private List<OrganizationWithSpaces> getAllOrganizations() {
        OrgAPI orgAPI = apiFactory.getAPI(OrgAPI.class);
        return orgAPI.getOrganizations();
    }

    /**
     * archive all Organizations' domains
     * @param currentURL where to store the organizations' folders
     */
    public void archiveOrganizations(String currentURL) {
        List<OrganizationWithSpaces> organizations = getAllOrganizations();
        Scanner scanner = new Scanner(System.in);

        for(OrganizationWithSpaces org : organizations) {
            System.out.println("Do you want to archive the organization \"" + org.getName() + "\"? (Y/N)");
            String response = scanner.next();
            if (!response.equalsIgnoreCase("Y"))
                continue;

            String orgURL = currentURL + "/" + org.getName().replace(File.separator, "_");
            File folder = new File(orgURL);
            System.out.println("[ORG] creating org folder: " + org.getName().replace(File.separator, "_") + "...");
            boolean result = false;

            try{
                folder.mkdir();
                result = true;
            }
            catch(SecurityException se){
                System.out.println("You don't have permission to perform this operation.");

            }
            if(result) {
                System.out.println("[ORG] " + org.getName() + " created.");

                // metadata
                MetadataFile meta = new MetadataFile(orgURL);
                meta.generate(org);

                archiveSpaces(org, orgURL);
            }

        }
    }

    /**
     * archive all domain's spaces
     * @param organization
     * @param currentURL organization's folder
     */
    public void archiveSpaces(OrganizationWithSpaces organization, String currentURL) {
        for(SpaceMini space : organization.getSpaces()) {
            String spaceURL = currentURL + "/" + space.getName().replace(File.separator, "_");
            File folder = new File(spaceURL);
            System.out.println("[SPACE] creating space folder: " + space.getName());
            boolean result = false;

            try{
                folder.mkdir();
                result = true;
            }
            catch(SecurityException se){
                System.out.println("You don't have permission to perform this operation.");

            }
            if(result) {
                System.out.println("[SPACE] " + space.getName() + " created.");

                // metadata
                MetadataFile meta = new MetadataFile(spaceURL);
                meta.generate(space);

                archiveApps(space, spaceURL);
            }
        }
    }

    /**
     * archive apps
     * @param spaceMini apps' space
     * @param currentURL space's folder
     */
    public void archiveApps(SpaceMini spaceMini, String currentURL) {
        AppAPI appAPI = apiFactory.getAPI((AppAPI.class));
        List<Application> apps = appAPI.getAppsOnSpace(spaceMini.getId());

        for (Application app : apps) {
            String appURL = currentURL + "/" + app.getConfiguration().getName().replace(File.separator, "_");
            File folder = new File(appURL);
            System.out.println("[APP] creating app folder: " + app.getConfiguration().getName());
            boolean result = false;

            try {
                folder.mkdir();
                result = true;
            } catch (SecurityException se) {
                System.out.println("You don't have permission to perform this operation.");

            }
            if (result) {
                System.out.println("[APP] " + app.getConfiguration().getName() + " created.");
                MetadataFile metadataFile = new MetadataFile(appURL);
                metadataFile.generate(app);
                archiveItems(app.getId(), appURL);
                archiveAppHooks(app.getId(), appURL);
                archiveTasks(app, appURL);

                new File(appURL + "/files").mkdir();
                archiveAppFiles(app.getId(), appURL);
            }
        }
    }

    /**
     * archive app hooks
     * @param appId
     * @param appURL app's folder
     */
    public void archiveAppHooks(int appId, String appURL) {
        HookAPI hookAPI = apiFactory.getAPI(HookAPI.class);
        List<Hook> hooks = hookAPI.get(new Reference(ReferenceType.APP, appId));

        MetadataFile metadataFile = new MetadataFile(appURL);
        metadataFile.generate(hooks);
    }

    // TODO API does not fully support tasks
    public void archiveTasks(Application app, String currentURL) {
    }

    /**
     * archive items
     * @param appId
     * @param currentURL app's folder
     */
    public void archiveItems(int appId, String currentURL) {
        ItemAPI itemAPI = apiFactory.getAPI(ItemAPI.class);

        ItemsResponse response = itemAPI.getItems(appId, null, null, null, null);
        for (ItemBadge item : response.getItems()) {
            String itemURL = currentURL + "/" + "[" + item.getId() + "] " + item.getTitle().replace(File.separator, "_");
            File folder = new File(itemURL);
            System.out.println("[ITEM] creating item folder: " + item.getId());
            boolean result = false;

            try{
                folder.mkdir();
                result = true;
            }
            catch(SecurityException se){
                System.out.println("You don't have permission to perform this operation.");
            }
            if(result) {
                System.out.println("[ITEM] " + item.getId() + " " + item.getTitle() + " created.");
                MetadataFile metadataFile = new MetadataFile(itemURL);
                metadataFile.generate(item);

                archiveItemComments(item.getId(), itemURL);
                archiveItemValues(item, itemURL);
            }
        }
    }

    /** archive files by app. its doesn't let me archive files by item...
     * files inside an item are downloaded to that item folder
     * others files are downloaded to the app folder
     */
    public void archiveAppFiles(int appId, String appURL) {
        FileAPI filesAPI = apiFactory.getAPI(FileAPI.class);
        List<com.podio.file.File> files = filesAPI.getOnApp(appId, 5, 0);

        for (com.podio.file.File file : files) {
            File fileURL;
            if (file.getContext().getType() == ReferenceType.ACTION.ITEM)
                fileURL = new File(appURL + "/" + "[" + file.getContext().getId() + "] " + file.getContext().getTitle() + "/" + "/files/" + file.getName());
            else
                fileURL = new File(appURL + "/files/" + file.getName());

            try {
                System.out.println("[FILE] downloading " + file.getName() + " ...");
                filesAPI.downloadFile(file.getId(), fileURL, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * archive comments
     * @param itemId
     * @param currentURL
     */
    public void archiveItemComments(int itemId, String currentURL) {
        CommentAPI commentAPI = apiFactory.getAPI(CommentAPI.class);

        List<Comment> comments = commentAPI.getComments(new Reference(ReferenceType.ITEM, itemId));

        currentURL += "/comments";
        String commentsMetaURL = currentURL + "/meta";
        File commentsFolder = new File(currentURL);
        commentsFolder.mkdir();
        File commentsMetaFolder = new File(currentURL + "/meta");
        commentsMetaFolder.mkdir();

        SimpleDateFormat parserTime = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat parserHumanTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");



        for (Comment comment : comments) {
            // human-readable comment file
            String commentURL = currentURL + "/" + "[" + parserTime.format(comment.getCreatedOn().toDate()) + "] " + comment.getCreatedBy().getName() + ".txt";

            PrintWriter writer = null;
            try {
                writer = new PrintWriter(commentURL, "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            writer.println("id: " + comment.getId());
            writer.println("external_id: " + comment.getExternalId());
            writer.println("createdOn: " + parserHumanTime.format(comment.getCreatedOn().toDate()));
            writer.println("createdVia: " + comment.getCreatedVia().getName());
            writer.println("value: " + comment.getValue());
            writer.close();

            // comment meta file
            MetadataFile metadataFile = new MetadataFile(commentsMetaURL);
            metadataFile.generate(comment);
        }
    }

    /**
     * archive item's values
     * @param item
     * @param itemURL
     */
    public void archiveItemValues(ItemBadge item, String itemURL) {
        ItemAPI itemAPI = apiFactory.getAPI(ItemAPI.class);
        List<FieldValuesView> fieldvalues = itemAPI.getItemValues(item.getId());
        HumanReadableItemValues file = new HumanReadableItemValues(this, itemURL, fieldvalues);
        file.setDocTitle(item.getTitle());
        file.generateHTML();
        
        String itemValuesURL = itemURL + "/row_values.txt";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(itemValuesURL, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        for (FieldValuesView fieldValue : fieldvalues) {
            writer.println(fieldValue.getLabel() + "(" + fieldValue.getType() + "): ");
            writer.println(fieldValue.getValues().toString());
        }

        writer.close();
    }

    public APIFactory getApiFactory() {
        return apiFactory;
    }

}