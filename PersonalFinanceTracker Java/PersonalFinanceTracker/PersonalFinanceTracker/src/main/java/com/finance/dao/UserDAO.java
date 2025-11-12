package com.finance.dao;

import com.finance.models.User;
import com.finance.utils.XMLUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UserDAO {
    private static final String USERS_XML_PATH = System.getProperty("user.home") + 
                                                  "/finance_data/users.xml";

    public UserDAO() {
        initializeUsersFile();
    }

    private void initializeUsersFile() {
        File file = new File(USERS_XML_PATH);
        file.getParentFile().mkdirs();
        
        if (!file.exists()) {
            try {
                Element root = new Element("users");
                Document doc = new Document(root);
                XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileOutputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public User registerUser(String username, String email, String password) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(USERS_XML_PATH));
            Element rootElement = document.getRootElement();

            // Check if user already exists
            List<Element> users = rootElement.getChildren("user");
            for (Element userElement : users) {
                if (userElement.getChildText("username").equals(username) ||
                    userElement.getChildText("email").equals(email)) {
                    return null; // User already exists
                }
            }

            // Create new user
            String userId = UUID.randomUUID().toString();
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            Element userElement = new Element("user");
            userElement.addContent(new Element("userId").setText(userId));
            userElement.addContent(new Element("username").setText(username));
            userElement.addContent(new Element("email").setText(email));
            userElement.addContent(new Element("password").setText(hashedPassword));
            userElement.addContent(new Element("createdDate").setText(createdDate));

            rootElement.addContent(userElement);

            // Save to file
            XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
            xmlOutput.output(document, new FileOutputStream(USERS_XML_PATH));

            return new User(userId, username, email, hashedPassword, createdDate);
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User authenticateUser(String username, String password) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(USERS_XML_PATH));
            Element rootElement = document.getRootElement();

            List<Element> users = rootElement.getChildren("user");
            for (Element userElement : users) {
                String storedUsername = userElement.getChildText("username");
                String storedPassword = userElement.getChildText("password");

                if (storedUsername.equals(username) && 
                    BCrypt.checkpw(password, storedPassword)) {
                    return new User(
                        userElement.getChildText("userId"),
                        storedUsername,
                        userElement.getChildText("email"),
                        storedPassword,
                        userElement.getChildText("createdDate")
                    );
                }
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(String userId) {
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(new File(USERS_XML_PATH));
            Element rootElement = document.getRootElement();

            List<Element> users = rootElement.getChildren("user");
            for (Element userElement : users) {
                if (userElement.getChildText("userId").equals(userId)) {
                    return new User(
                        userId,
                        userElement.getChildText("username"),
                        userElement.getChildText("email"),
                        userElement.getChildText("password"),
                        userElement.getChildText("createdDate")
                    );
                }
            }
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
