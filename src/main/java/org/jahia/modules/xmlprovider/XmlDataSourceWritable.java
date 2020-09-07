
package org.jahia.modules.xmlprovider;

import com.google.common.collect.Sets;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.external.ExternalData;
import org.jahia.modules.external.ExternalDataSource;
import org.jahia.modules.external.ExternalQuery;
import org.jahia.modules.external.ExternalDataSource.Searchable;
import org.jahia.modules.external.ExternalDataSource.Writable;
import org.jahia.modules.external.query.QueryHelper;
import org.jahia.modules.xmlprovider.utils.XmlUtils;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import to.XmlProductTO;

public class XmlDataSourceWritable implements ExternalDataSource, Writable, Searchable {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmlDataSourceWritable.class);
    private String xmlFilePath = "";
    private static final String PCMIX_PRODUCT = "pcmix:product";
    private static final String JNT_CONTENT_FOLDER = "jnt:contentFolder";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String MODEL = "model";
    private static final String DESCRIPTION = "description";
    private static final String PRICE = "price";
    private static final String IMAGE = "image";
    private static final String MANUAL = "manual";
    private static final String ROOT = "root";
    private static final String ELEMENT_TAG = "product";
    private static final Logger logger = LoggerFactory.getLogger(XmlDataSourceWritable.class);

    public XmlDataSourceWritable() {
    }

    public String getXmlFilePath() {
        return this.xmlFilePath;
    }

    public void setXmlFilePath(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    public void start() {
    }

    private JSONArray queryXML() throws RepositoryException {
        try {
            if (StringUtils.isEmpty(this.xmlFilePath)) {
                return new JSONArray("[]");
            } else {
                logger.info("queryXML(), parsing the file:" + this.xmlFilePath);
                StringBuilder jsonData = new StringBuilder();
                File xmlFile = new File(this.xmlFilePath);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlFile);
                doc.getDocumentElement().normalize();
                logger.info("queryXML(), XML Root element :" + doc.getDocumentElement().getNodeName());
                NodeList nList = doc.getElementsByTagName("product");

                for(int index = 0; index < nList.getLength(); ++index) {
                    Node nNode = nList.item(index);
                    logger.info("queryXML(), XML Current Element :" + nNode.getNodeName());
                    if (nNode.getNodeType() == 1) {
                        Element eElement = (Element)nNode;
                        if (eElement != null) {
                            if (jsonData.length() != 0) {
                                jsonData.append(",");
                            }

                            jsonData.append((new XmlProductTO(eElement)).toJsonString());
                        }
                    }
                }

                return new JSONArray("[" + jsonData.toString() + "]");
            }
        } catch (Exception var10) {
            throw new RepositoryException(var10);
        }
    }

    private JSONObject getProduct(String id, JSONArray products) throws JSONException {
        if (StringUtils.isNotBlank(id)) {
            for(int i = 0; i < products.length(); ++i) {
                JSONObject product = products.getJSONObject(i);
                if (id.equals(product.getString("id"))) {
                    return product;
                }
            }
        }

        return null;
    }

    public List<String> getChildren(String path) throws RepositoryException {
        List<String> r = new ArrayList();
        if (path.equals("/")) {
            try {
                JSONArray children = this.queryXML();

                for(int i = 1; i <= children.length(); ++i) {
                    JSONObject child = (JSONObject)children.get(i - 1);
                    r.add(XmlUtils.displayNumberTwoDigits(i) + "-" + "product" + "-" + child.get("id"));
                }
            } catch (JSONException var6) {
                throw new RepositoryException(var6);
            }
        }

        return r;
    }

    public ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException {
        if (identifier.equals("root")) {
            return new ExternalData(identifier, "/", "jnt:contentFolder", new HashMap());
        } else {
            Map<String, String[]> properties = new HashMap();
            String[] idProduct = identifier.split("-");
            if (idProduct.length == 3) {
                try {
                    JSONObject product = this.getProduct(idProduct[2], this.queryXML());
                    properties.put("id", new String[]{product.getString("id")});
                    properties.put("name", new String[]{product.getString("name")});
                    properties.put("model", new String[]{product.getString("model")});
                    properties.put("description", new String[]{product.getString("description")});
                    properties.put("price", new String[]{product.getString("price")});
                    properties.put("image", new String[]{product.getString("image")});
                    properties.put("manual", new String[]{product.getString("manual")});
                    return new ExternalData(identifier, "/" + identifier, "pcmix:product", properties);
                } catch (Exception var5) {
                    throw new ItemNotFoundException(identifier);
                }
            } else {
                throw new ItemNotFoundException(identifier);
            }
        }
    }

    public ExternalData getItemByPath(String path) throws PathNotFoundException {
        String[] splitPath = path.split("/");

        try {
            return splitPath.length <= 1 ? this.getItemByIdentifier("root") : this.getItemByIdentifier(splitPath[1]);
        } catch (ItemNotFoundException var4) {
            throw new PathNotFoundException(var4);
        }
    }

    public List<String> search(ExternalQuery query) throws RepositoryException {
        List<String> paths = new ArrayList();
        String nodeType = QueryHelper.getNodeType(query.getSource());
        if (NodeTypeRegistry.getInstance().getNodeType("pcmix:product").isNodeType(nodeType)) {
            try {
                JSONArray products = this.queryXML();

                for(int i = 1; i <= products.length(); ++i) {
                    JSONObject product = (JSONObject)products.get(i - 1);
                    String path = "/" + XmlUtils.displayNumberTwoDigits(i) + "-" + "product" + "-" + product.get("id");
                    paths.add(path);
                }
            } catch (JSONException var8) {
                throw new RepositoryException(var8);
            }
        }

        return paths;
    }

    public void saveItem(ExternalData data) throws RepositoryException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(this.xmlFilePath);
            Map<String, String[]> productProps = data.getProperties();
            String productId = productProps.containsKey("id") ? ((String[])productProps.get("id"))[0] : "";
            boolean isNew = true;
            Element rootElement = doc.getDocumentElement();
            NodeList nList = doc.getElementsByTagName("product");

            Element eElement;
            for(int index = 0; index < nList.getLength(); ++index) {
                Node nNode = nList.item(index);
                if (nNode.getNodeType() == 1) {
                    eElement = (Element)nNode;
                    XmlProductTO productTO = new XmlProductTO(eElement);
                    if (productTO.getId().trim().equals(productId.trim())) {
                        setValue("name", eElement, productProps.containsKey("name") ? ((String[])productProps.get("name"))[0] : "");
                        setValue("model", eElement, productProps.containsKey("model") ? ((String[])productProps.get("model"))[0] : "");
                        setValue("description", eElement, productProps.containsKey("description") ? ((String[])productProps.get("description"))[0] : "");
                        setValue("image", eElement, productProps.containsKey("image") ? ((String[])productProps.get("image"))[0] : "");
                        setValue("price", eElement, productProps.containsKey("price") ? ((String[])productProps.get("price"))[0] : "");
                        setValue("manual", eElement, productProps.containsKey("manual") ? ((String[])productProps.get("manual"))[0] : "");
                        logger.info("saveItem(), XML update Current Element :" + nNode.getNodeName());
                        isNew = false;
                    }
                }
            }

            if (isNew) {
                Element newProduct = doc.createElement("product");
                Element idElement = doc.createElement("id");
                idElement.appendChild(doc.createTextNode(productProps.containsKey("id") ? ((String[])productProps.get("id"))[0] : ""));
                eElement = doc.createElement("name");
                eElement.appendChild(doc.createTextNode(productProps.containsKey("name") ? ((String[])productProps.get("name"))[0] : ""));
                Element modelElement = doc.createElement("model");
                modelElement.appendChild(doc.createTextNode(productProps.containsKey("model") ? ((String[])productProps.get("model"))[0] : ""));
                Element descriptionElement = doc.createElement("description");
                descriptionElement.appendChild(doc.createTextNode(productProps.containsKey("description") ? ((String[])productProps.get("description"))[0] : ""));
                Element imageElement = doc.createElement("image");
                imageElement.appendChild(doc.createTextNode(productProps.containsKey("image") ? ((String[])productProps.get("image"))[0] : ""));
                Element priceElement = doc.createElement("price");
                priceElement.appendChild(doc.createTextNode(productProps.containsKey("price") ? ((String[])productProps.get("price"))[0] : ""));
                Element manualElement = doc.createElement("manual");
                manualElement.appendChild(doc.createTextNode(productProps.containsKey("manual") ? ((String[])productProps.get("manual"))[0] : ""));
                rootElement.appendChild(newProduct);
                newProduct.appendChild(idElement);
                newProduct.appendChild(eElement);
                newProduct.appendChild(modelElement);
                newProduct.appendChild(descriptionElement);
                newProduct.appendChild(priceElement);
                newProduct.appendChild(imageElement);
                newProduct.appendChild(manualElement);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(this.xmlFilePath));
            transformer.transform(source, result);
            LOGGER.info("saveItem(), Done......");
        } catch (Exception var18) {
            LOGGER.error("saveItem(), can't save the item, ", var18);
        }

    }

    private static void setValue(String tag, Element element, String input) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodes.item(0);
        node.setTextContent(input);
    }

    public Set<String> getSupportedNodeTypes() {
        return Sets.newHashSet(new String[]{"jnt:contentFolder", "pcmix:product"});
    }

    public boolean isSupportsHierarchicalIdentifiers() {
        return false;
    }

    public boolean isSupportsUuid() {
        return false;
    }

    public boolean itemExists(String path) {
        return false;
    }

    public void move(String oldPath, String newPath) throws RepositoryException {
        LOGGER.info("Move : oldPath=" + oldPath + " newPath=" + newPath);
    }

    public void order(String path, List<String> children) throws RepositoryException {
        LOGGER.info("Order : path=" + path);
    }

    public void removeItemByPath(String path) throws RepositoryException {
        LOGGER.info("Remove item by path : path=" + path);
    }
}
