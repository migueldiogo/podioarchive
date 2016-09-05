package com.podioarchive;

import com.podio.file.*;
import com.podio.item.FieldValuesView;

import java.io.*;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Miguel Prata Leal on 23/07/16.
 */
public class HumanReadableItemValues {

    private Archive archive;
    private String docTitle;
    private PrintWriter document;
    private String itemURL;
    private List<FieldValuesView> fieldValues;

    /**
     *
     * @param archive archive instance
     * @param itemURL item URL
     * @param fieldValues fieldValues instance
     */
    public HumanReadableItemValues(Archive archive, String itemURL, List<FieldValuesView> fieldValues) {
        this.archive = archive;
        this.itemURL = itemURL;
        this.fieldValues = fieldValues;
    }

    /**
     * set the title of the html page
     * @param docTitle
     */
    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    /**
     * initialize HTML file
     */
    private void initializeHTMLFile() {
        try {
            document = new PrintWriter(itemURL + "/values.html", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String title = docTitle != null ? docTitle : "null";
        document.println("<html>");
        document.println("<head>" +
                            "<meta charset=\"utf-8\">" +
                            "<title>" + title + "</title>" +
                        "</head>");

    }

    /**
     * close HTML file
     */
    private void closeHTMLFile() {
        document.println("</html>");
        document.close();
    }

    /**
     * main routine - generates the html file
     */
    public void generateHTML() {
        initializeHTMLFile();

        for (FieldValuesView fieldValue : fieldValues) {
            document.println(addItemValueToHTML(fieldValue));
        }

        closeHTMLFile();
    }

    /**
     * add item value to the html page
     * @param fieldValue field value instance
     * @return item information converted to html
     */
    private String addItemValueToHTML(FieldValuesView fieldValue) {
        String result = "";

        result += "<p><b>" + fieldValue.getLabel() + ": </b>";

        switch (fieldValue.getType()) {
            case APP:
                result += "<ul style=\"list-style-type:disc\">";
                for (Map<String, ?> referencesMap : fieldValue.getValues()) {
                    Map<String, ?> reference = (Map<String, ?>)referencesMap.get("value");
                    Map<String, ?> app = (Map<String, ?>)reference.get("app");

                    result += "<li>" +
                            "<a href=\"" +
                            "../.." + File.separator + ((String)app.get("name")).replace(File.separator, "_") + File.separator +
                            "[" + reference.get("item_id") + "] " +
                            reference.get("title") + File.separator + "values.html" +
                            "\">" +
                            reference.get("title") +
                            "</a>" +
                            " (" +
                            app.get("name") +
                            ")" +
                            "</li>";
                }
                result += "</ul>";

                break;
            case CALCULATION:
                result += (String)fieldValue.getValues().get(0).get("value");
                break;
            case CATEGORY:
                Map<String, ?> valuesMap = (Map<String, ?>)fieldValue.getValues().get(0).get("value");
                result += "<font color=" + valuesMap.get("color") + ">" +
                        valuesMap.get("text") +
                        "</font>";
                break;
            case CONTACT:
                result += "<ul style=\"list-style-type:disc\">";
                for (Map<String, ?> map : fieldValue.getValues()) {
                    Map<String, ?> contactMap = (Map<String, ?>)map.get("value");
                    result += "<li>" +
                            contactMap.get("name") +
                            "</li>";
                }
                result += "</ul>";
                break;
            case DATE:
                String start = (String) fieldValue.getValues().get(0).get("start");
                String end = (String) fieldValue.getValues().get(0).get("end");

                SimpleDateFormat parserDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                try {
                    result += "<ul style=\"list-style-type:disc\">";
                    if (start != null) {
                        Date startDate = parserDate.parse(start);
                        result += "<li>start: " +
                                parserDate.format(startDate) +
                                "</li>";
                    }

                    if (end != null) {
                        Date endDate = parserDate.parse(end);
                        result += "<li>end: " +
                                parserDate.format(endDate) +
                                "</li>";
                    }

                    result += "</ul>";

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
            case DURATION:
                int totalSeconds = (Integer)fieldValue.getValues().get(0).get("value");
                int[] hms = convertSecondsToHMS(totalSeconds);
                result += hms[0] + " hours " + hms[1] + " minutes " + hms[2] + " seconds";
                break;
            case EMAIL:
                result += "<ul style=\"list-style-type:disc\">";
                for (Map<String, ?> map : fieldValue.getValues()) {
                    result += "<li>" +
                            map.get("type") + ": " +
                            "<a href=\"" +
                            "mailto:" +
                            map.get("value") +
                            "\">" +
                            map.get("value") +
                            "</a>" +
                            "</li>";
                }
                result += "</ul>";
                break;
            case EMBED:
                result += "<ul style=\"list-style-type:disc\">";
                for (Map<String, ?> map : fieldValue.getValues()) {
                    Map<String, ?> embedMap = (Map<String, ?>)map.get("embed");
                    result += "<li>" +
                            "<a href=\"" +
                            embedMap.get("original_url") +
                            "\">" +
                            embedMap.get("original_url") +
                            "</a>" +
                            "</li>";
                }
                result += "</ul>";
                break;
            case FILE:
                result += "FIELD TYPE NOT SUPPORTED!";
                break;
            case IMAGE:
                result += archiveFieldImages(fieldValue);
                break;
            case LOCATION:
                result += "<a href=\"" +
                        "http://maps.google.com/maps?&z=10&q=" +
                        fieldValue.getValues().get(0).get("lat") + "+" +
                        fieldValue.getValues().get(0).get("lng") +
                        "&ll=" +
                        fieldValue.getValues().get(0).get("lat") + "+" +
                        fieldValue.getValues().get(0).get("lng") +
                        "\">" +
                        fieldValue.getValues().get(0).get("formatted") +
                        "</a>";
                break;
            case MEMBER:
                result += "FIELD TYPE NOT SUPPORTED!";
                break;
            case MONEY:
                result += (String)fieldValue.getValues().get(0).get("value") +
                        fieldValue.getValues().get(0).get("currency");
                break;
            case NUMBER:
                result += (String)fieldValue.getValues().get(0).get("value");
                break;
            case PHONE:
                result += "<ul style=\"list-style-type:disc\">";
                for (Map<String, ?> map : fieldValue.getValues()) {
                    result += "<li>" +
                            map.get("type") + ": " +
                            map.get("value") +
                            "</li>";
                }
                result += "</ul>";
                break;
            case PROGRESS:
                result += fieldValue.getValues().get(0).get("value") + "%";
                break;
            case TEL:
                result += "FIELD TYPE NOT SUPPORTED!";
                break;
            case TEXT:
                result += fieldValue.getValues().get(0).get("value");
                break;
            case VIDEO:
                result += "FIELD TYPE NOT SUPPORTED!";
                break;
            default:
                result += "FIELD TYPE NOT SUPPORTED!";
                break;
        }

        result += "</p>";

        return result;
    }

    /**
     * tool method - converts seconds in [hours, minutes, seconds]
     * @param totalSeconds seconds to convert
     * @return array [hours, minutes, seconds]
     */
    private int[] convertSecondsToHMS(int totalSeconds) {
        int[] hms = new int[3];
        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        hms[2] = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        hms[1] = totalMinutes % MINUTES_IN_AN_HOUR;
        hms[0] = totalMinutes / MINUTES_IN_AN_HOUR;

        return hms;
    }

    /**
     * method responsible for archiving images in a file
     * @param fieldValue
     * @return
     */
    private String archiveFieldImages(FieldValuesView fieldValue) {
        FileAPI filesAPI = archive.getApiFactory().getAPI(FileAPI.class);
        File folder = new File(itemURL + "/" + fieldValue.getLabel());
        folder.mkdir();

        String result = "";

        for (Map<String, ?> map : fieldValue.getValues()) {
            Map<String, ?> valueMap = (Map<String, ?>)map.get("value");
            File imageTargetURL = new File(itemURL + File.separator + fieldValue.getLabel() + File.separator + valueMap.get("name"));

            try {
                // download images to a specific folder
                System.out.println("[IMAGE] downloading " + valueMap.get("name") + " ...");
                filesAPI.downloadFile((Integer)valueMap.get("file_id"), imageTargetURL, null);
                // embed images on the html files
                result += "<br><img src=\"" +
                        fieldValue.getLabel() + File.separator + valueMap.get("name") +
                        "\"><br>";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }
}
