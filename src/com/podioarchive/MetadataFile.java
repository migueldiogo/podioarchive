package com.podioarchive;

import com.podio.app.Application;
import com.podio.app.ApplicationField;
import com.podio.comment.Comment;
import com.podio.common.AuthorizationEntity;
import com.podio.hook.Hook;
import com.podio.item.FieldValuesView;
import com.podio.item.ItemAPI;
import com.podio.item.ItemBadge;
import com.podio.org.OrganizationWithSpaces;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.podio.space.Space;
import com.podio.space.SpaceMini;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.io.File;
import java.util.List;

/**
 * This class is responsible for building metadata files depending on the
 * type of the content
 * Created by Miguel Prata Leal on 20/07/16.
 */
public class MetadataFile {
    private Document doc;
    private String fileDestinationURL;

    public MetadataFile(String fileDestinationURL) {
        this.fileDestinationURL = fileDestinationURL;
        init();
    }

    /**
     * starting point
     */
    private void init() {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.newDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * create metafile with custom name
     * @param fileName
     */
    private void createFile(String fileName) {
        try {
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(fileDestinationURL + "/" + fileName + ".xml"));
            transformer.transform(source, result);
            // Output to console for testing
            //StreamResult consoleResult = new StreamResult(System.out);
            //transformer.transform(source, consoleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createFile() {
        createFile("meta");
    }

    public void generate(OrganizationWithSpaces org) {
        // root element
        Element rootElement = doc.createElement("meta");
        doc.appendChild(rootElement);

        // setting name
        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(org.getName() != null ? org.getName() : "null"));
        rootElement.appendChild(name);

        // setting createRight
        Element createRight = doc.createElement("createRight");
        createRight.appendChild(doc.createTextNode(org.getCreateRight() != null ? "" + org.getCreateRight() : "null"));
        rootElement.appendChild(createRight);

        // setting id
        Element id = doc.createElement("id");
        id.appendChild(doc.createTextNode("" + org.getId()));
        rootElement.appendChild(id);

        // setting logo
        Element logo = doc.createElement("logo");
        logo.appendChild(doc.createTextNode(org.getLogo() != null ? "" + org.getLogo() : "null"));
        rootElement.appendChild(logo);

        // setting url
        Element url = doc.createElement("url");
        url.appendChild(doc.createTextNode(org.getUrl() != null ? org.getUrl() : "null"));
        rootElement.appendChild(url);

        createFile();
    }

    public void generate(SpaceMini space) {
        // root element
        Element rootElement = doc.createElement("meta");
        doc.appendChild(rootElement);

        // setting name
        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(space.getName() != null ? space.getName() : "null"));
        rootElement.appendChild(name);


        // setting id
        Element id = doc.createElement("id");
        id.appendChild(doc.createTextNode("" + space.getId()));
        rootElement.appendChild(id);

        // setting url
        Element url = doc.createElement("url");
        url.appendChild(doc.createTextNode(space.getUrl() != null ? space.getUrl() : "null"));
        rootElement.appendChild(url);

        createFile();

    }

    public void generate(Application app) {

        // root element
        Element rootElement = doc.createElement("meta");
        doc.appendChild(rootElement);

        // setting name
        Element name = doc.createElement("name");
        name.appendChild(doc.createTextNode(app.getConfiguration().getName() != null ? app.getConfiguration().getName()  : "null"));
        rootElement.appendChild(name);


        // setting id
        Element id = doc.createElement("id");
        id.appendChild(doc.createTextNode("" + app.getId()));
        rootElement.appendChild(id);

        // setting ext_id
        Element extId = doc.createElement("external_id");
        extId.appendChild(doc.createTextNode("" + app.getConfiguration().getExternalId()));
        rootElement.appendChild(extId);

        // setting space_id
        Element spaceId = doc.createElement("space_id");
        spaceId.appendChild(doc.createTextNode("" + app.getSpaceId()));
        rootElement.appendChild(spaceId);

        // setting description
        Element description = doc.createElement("description");
        description.appendChild(doc.createTextNode(app.getConfiguration().getDescription() != null ? app.getConfiguration().getDescription() : "null"));
        rootElement.appendChild(description);

        // setting ownerId
        Element ownerId = doc.createElement("ownerId");
        ownerId.appendChild(doc.createTextNode("" + app.getOwnerId()));
        rootElement.appendChild(ownerId);

        /*
        // setting fields description
        Element fields = doc.createElement("fields");
        List<ApplicationField> fieldsList = app.get
        for (ApplicationField fieldValue : fieldsList) {
            Element field_element = doc.createElement("field");
            field_element.setAttribute("id", "" + fieldValue.getId());
            field_element.setAttribute("external_id", fieldValue.getExternalId());
            field_element.setAttribute("label", fieldValue.getConfiguration().getLabel() != null ? fieldValue.getConfiguration().getLabel() : "null");
            field_element.setAttribute("type", fieldValue.getType().toString() != null ? fieldValue.getType().toString() : "null");
            //field_element.setAttribute("values", fieldValue.getStatus()..toString() != null ? fieldValue.getLabel() : "null");

            fields.appendChild(field_element);
        }



        rootElement.appendChild(fields);
        */

        createFile();
    }

    public void generate(ItemBadge item) {
        // root element
        Element rootElement = doc.createElement("meta");
        doc.appendChild(rootElement);

        // setting name
        Element title = doc.createElement("title");
        title.appendChild(doc.createTextNode(item.getTitle() != null ? item.getTitle() : "null"));
        rootElement.appendChild(title);


        // setting id
        Element id = doc.createElement("id");
        id.appendChild(doc.createTextNode("" + item.getId()));
        rootElement.appendChild(id);

        // setting ext_id
        Element extId = doc.createElement("external_id");
        extId.appendChild(doc.createTextNode("" + item.getExternalId()));
        rootElement.appendChild(extId);

        // setting ext_id
        Element link = doc.createElement("link");
        link.appendChild(doc.createTextNode(item.getLink() != null ? item.getLink() : "null"));
        rootElement.appendChild(link);

        // setting ext_id
        Element createdOn = doc.createElement("createdOn");
        createdOn.appendChild(doc.createTextNode(item.getCreatedOn().toString() != null ? item.getCreatedOn().toString() : "null"));
        rootElement.appendChild(createdOn);

        // setting comments
        Element comments = doc.createElement("comments");
        comments.appendChild(doc.createTextNode("" + item.getComments()));
        rootElement.appendChild(comments);


        createFile();
    }

    public void generate(Comment comment) {
        // root element
        Element rootElement = doc.createElement("meta");
        doc.appendChild(rootElement);

        // setting id
        Element id = doc.createElement("id");
        id.appendChild(doc.createTextNode("" + comment.getId()));
        rootElement.appendChild(id);

        // setting ext_id
        Element extId = doc.createElement("external_id");
        extId.appendChild(doc.createTextNode("" + comment.getExternalId()));
        rootElement.appendChild(extId);

        // setting createdOn
        Element createdOn = doc.createElement("createdOn");
        createdOn.appendChild(doc.createTextNode(comment.getCreatedOn().toString()));
        rootElement.appendChild(createdOn);


        // setting createdBy
        Element createdBy = doc.createElement("createdBy");
        Element createdByName = doc.createElement(("name"));
        createdByName.appendChild(doc.createTextNode(comment.getCreatedBy().getName() != null ? comment.getCreatedBy().getName(): "null"));
        createdBy.appendChild(createdByName);
        Element createdById = doc.createElement(("id"));
        createdById.appendChild(doc.createTextNode("" + comment.getCreatedBy().getId()));
        createdBy.appendChild(createdById);
        rootElement.appendChild(createdBy);

        // setting createdVia
        Element createdVia = doc.createElement("createdVia");
        Element createdViaName = doc.createElement(("name"));
        createdViaName.appendChild(doc.createTextNode(comment.getCreatedVia().getName() != null ? comment.getCreatedVia().getName(): "null"));
        createdVia.appendChild(createdViaName);
        Element createdViaId = doc.createElement(("id"));
        createdViaId.appendChild(doc.createTextNode("" + comment.getCreatedVia().getId()));
        createdVia.appendChild(createdViaId);
        rootElement.appendChild(createdVia);

        // setting value (comment's body)
        Element value = doc.createElement("value");
        value.appendChild(doc.createTextNode(comment.getValue() != null ? comment.getValue(): "null"));
        rootElement.appendChild(value);

        createFile("" + comment.getId());
    }

    public void generate(List<Hook> hooks) {
        Element rootElement = doc.createElement("meta");
        doc.appendChild(rootElement);

        Element hooksElement = doc.createElement("hooks");
        rootElement.appendChild(hooksElement);

        for(Hook hook : hooks) {
            // root element
            Element hookElement = doc.createElement("hook");
            hooksElement.appendChild(hookElement);

            // setting id
            Element id = doc.createElement("id");
            id.appendChild(doc.createTextNode("" + hook.getId()));
            hookElement.appendChild(id);

            // setting type
            Element type = doc.createElement("type");
            type.appendChild(doc.createTextNode(hook.getType().toString()));
            hookElement.appendChild(type);

            // setting url
            Element url = doc.createElement("url");
            url.appendChild(doc.createTextNode(hook.getUrl()));
            hookElement.appendChild(url);

            // setting status
            Element status = doc.createElement("status");
            status.appendChild(doc.createTextNode(hook.getStatus().toString()));
            hookElement.appendChild(status);

            // setting createdOn
            Element createdOn = doc.createElement("createdOn");
            createdOn.appendChild(doc.createTextNode(hook.getCreatedOn().toString()));
            hookElement.appendChild(createdOn);

            // setting createdBy
            Element createdBy = doc.createElement("createdBy");
            Element createdByName = doc.createElement(("name"));
            createdByName.appendChild(doc.createTextNode(hook.getCreatedBy().getName() != null ? hook.getCreatedBy().getName() : "null"));
            createdBy.appendChild(createdByName);
            Element createdById = doc.createElement(("id"));
            createdById.appendChild(doc.createTextNode("" + hook.getCreatedBy().getId()));
            createdBy.appendChild(createdById);
            hookElement.appendChild(createdBy);

            // setting createdVia
            Element createdVia = doc.createElement("createdVia");
            Element createdViaName = doc.createElement(("name"));
            createdViaName.appendChild(doc.createTextNode(hook.getCreatedVia().getName() != null ? hook.getCreatedVia().getName() : "null"));
            createdVia.appendChild(createdViaName);
            Element createdViaId = doc.createElement(("id"));
            createdViaId.appendChild(doc.createTextNode("" + hook.getCreatedVia().getId()));
            createdVia.appendChild(createdViaId);
            hookElement.appendChild(createdVia);
        }

        createFile("hooks");
    }
}
