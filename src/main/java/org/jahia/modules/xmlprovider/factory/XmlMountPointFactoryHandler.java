/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.xmlprovider.factory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.VFS;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.i18n.Messages;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.execution.RequestContext;
import org.jahia.modules.external.admin.mount.AbstractMountPointFactoryHandler;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jcr.RepositoryException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Locale;


public class XmlMountPointFactoryHandler extends AbstractMountPointFactoryHandler<XmlMountPointFactory> implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(XmlMountPointFactoryHandler.class);

    private static final long serialVersionUID = 41541258548484556L;

    private static final String BUNDLE = "resources.xml-externalprovider";
    private static final String CONTENTS_NODENAME = "contents";
    private static final String XML_EXTERNALPROVIDER = "xml-externalprovider";
    private static final String XML_SCHEMA_PATH = "META-INF/xml/XMLSchema.xsd";
    private static final String JNT_CONTENT_FOLDER = "jnt:contentFolder";
    private XmlMountPointFactory xmlMountPointFactory;

    private String stateCode;
    private String messageKey;

    @Autowired
    private transient JahiaTemplateManagerService templateManagerService;

    public void init(RequestContext requestContext) {
        xmlMountPointFactory = new XmlMountPointFactory();
        try {
            super.init(requestContext, xmlMountPointFactory);
        } catch (RepositoryException e) {
            logger.error("Error retrieving mount point", e);
        }
        requestContext.getFlowScope().put("xmlFactory", xmlMountPointFactory);
    }

    public String getFolderList() {
        JSONObject result = new JSONObject();
        try {
            JSONArray folders = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<JSONArray>() {
                @Override
                public JSONArray doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return getSiteFolders(session.getWorkspace(),CONTENTS_NODENAME, JNT_CONTENT_FOLDER);
                }
            });

            result.put("folders",folders);
        } catch (RepositoryException e) {
            logger.error("Error trying to retrieve local folders", e);
        } catch (JSONException e) {
            logger.error("Error trying to construct JSON from local folders", e);
        }

        return result.toString();
    }

    public Boolean save(MessageContext messageContext, RequestContext requestContext) {
        stateCode = "SUCCESS";
        Locale locale = LocaleContextHolder.getLocale();
        boolean validXmlPoint = validateXml(xmlMountPointFactory);
        if(!validXmlPoint) {
            logger.error(String.format("Error saving mount point : %swith the root : %s", xmlMountPointFactory.getName(), xmlMountPointFactory.getRoot()));
            MessageBuilder messageBuilder = new MessageBuilder().error().defaultText(Messages.get(BUNDLE,"xmlProvider.error.file",locale));
            messageContext.addMessage(messageBuilder.build());
            requestContext.getConversationScope().put("adminURL", getAdminURL(requestContext));
            return false;
        }
        try {
            boolean available = super.save(xmlMountPointFactory);
            if (available) {
                stateCode = "SUCCESS";
                messageKey = "label.success";
                requestContext.getConversationScope().put("adminURL", getAdminURL(requestContext));
                return true;
            } else {
                logger.warn(String.format("Mount point availability problem : %swith the root : %sthe mount point is created but unmounted", xmlMountPointFactory.getName(), xmlMountPointFactory.getRoot()));
                stateCode = "WARNING";
                messageKey = "label.error";
                requestContext.getConversationScope().put("adminURL", getAdminURL(requestContext));
                return true;
            }
        } catch (RepositoryException e) {
            logger.error("Error saving mount point : " + xmlMountPointFactory.getName(), e);
            MessageBuilder messageBuilder = new MessageBuilder().error().defaultText(Messages.get(BUNDLE,"label.error",locale));
            messageContext.addMessage(messageBuilder.build());
        }
        return false;
    }

    private boolean validateXml(XmlMountPointFactory xmlMountPointFactory) {
        InputStream is = null;
        try {
            VFS.getManager().resolveFile(xmlMountPointFactory.getRoot());
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setValidating(false);
            builderFactory.setNamespaceAware(true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlFile = builder.parse(new InputSource(xmlMountPointFactory.getRoot()));
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            is = templateManagerService.getTemplatePackageById(XML_EXTERNALPROVIDER).getBundle().getResource(XML_SCHEMA_PATH).openStream();
            Source schemaFile = new StreamSource(is);
            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(new DOMSource(xmlFile));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.warn(String.format("XML mount point %s has validation problem %s", xmlMountPointFactory.getName(), e.getMessage()));
            return false;
        } finally {
            IOUtils.closeQuietly(is);
        }
        return true;
    }

    @Override
    public String getAdminURL(RequestContext requestContext) {
        StringBuilder builder = new StringBuilder(super.getAdminURL(requestContext));
        if (stateCode != null && messageKey != null) {
            builder.append("?stateCode=").append(stateCode).append("&messageKey=").append(messageKey).append("&bundleSource=").append(BUNDLE);
        }
        return builder.toString();
    }
}
