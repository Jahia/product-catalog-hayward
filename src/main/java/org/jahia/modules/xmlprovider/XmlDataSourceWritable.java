
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
    private String xmlFilePath = "";
    private static final String CONTENT_TYPE = "pcmix:product";
    private static final String CONTENT_FOLDER = "jnt:contentFolder";
    private static final String ROOT = "root";
    private static final String ELEMENT_TAG = "product";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String MODEL = "model";
    private static final String DESCRIPTION = "description";
    private static final String PRICE = "price";
    private static final String IMAGE = "image";
    private static final String MANUAL = "manual";
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
                return new JSONArray();
            } else {
                logger.info("queryXML(), parsing the file: " + this.xmlFilePath);
                StringBuilder jsonData = new StringBuilder();
                File xmlFile = new File(this.xmlFilePath);
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(xmlFile);
                doc.getDocumentElement().normalize();
                logger.info("queryXML(), XML Root element: " + doc.getDocumentElement().getNodeName());
                NodeList nList = doc.getElementsByTagName(ELEMENT_TAG);

                for(int index = 0; index < nList.getLength(); ++index) {
                    Node nNode = nList.item(index);
                    logger.info("queryXML(), XML Current Element: " + nNode.getNodeName());
                    if (nNode.getNodeType() == 1) {
                        Element eElement = (Element)nNode;
                        if (jsonData.length() != 0) {
                            jsonData.append(",");
                        }

                        jsonData.append((new XmlProductTO(eElement)).toJsonString());
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
                if (id.equals(product.getString(ID))) {
                    return product;
                }
            }
        }

        return null;
    }

    @Override
    public List<String> getChildren(String path) throws RepositoryException {
        ArrayList<String> r = new ArrayList<>();
        if (path.equals("/")) {
            try {
                JSONArray children = this.queryXML();

                for(int i = 1; i <= children.length(); ++i) {
                    JSONObject child = (JSONObject)children.get(i - 1);
                    r.add(XmlUtils.displayNumberTwoDigits(i) + "-" + "product" + "-" + child.get(ID));
                }
            } catch (JSONException var6) {
                throw new RepositoryException(var6);
            }
        }

        return r;
    }

    @Override
    public ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException {
        if (identifier.equals("root")) {
            return new ExternalData(identifier, "/", CONTENT_FOLDER, new HashMap<>());
        } else {
            Map<String, String[]> properties = new HashMap<>();
            String[] idProduct = identifier.split("-");
            if (idProduct.length == 3) {
                try {
                    JSONObject product = this.getProduct(idProduct[2], this.queryXML());
                    assert product != null;
                    properties.put(ID, new String[]{product.getString(ID)});
                    properties.put(NAME, new String[]{product.getString(NAME)});
                    properties.put(MODEL, new String[]{product.getString(MODEL)});
                    properties.put(DESCRIPTION, new String[]{product.getString(DESCRIPTION)});
                    properties.put(PRICE, new String[]{product.getString(PRICE)});
                    properties.put(IMAGE, new String[]{product.getString(IMAGE)});
                    properties.put(MANUAL, new String[]{product.getString(MANUAL)});
                    return new ExternalData(identifier, "/" + identifier, CONTENT_TYPE, properties);
                } catch (Exception var5) {
                    throw new ItemNotFoundException(identifier);
                }
            } else {
                throw new ItemNotFoundException(identifier);
            }
        }
    }

    @Override
    public ExternalData getItemByPath(String path) throws PathNotFoundException {
        String[] splitPath = path.split("/");

        try {
            return splitPath.length <= 1 ? this.getItemByIdentifier(ROOT) : this.getItemByIdentifier(splitPath[1]);
        } catch (ItemNotFoundException e) {
            throw new PathNotFoundException(e);
        }
    }

    @Override
    public List<String> search(ExternalQuery query) throws RepositoryException {
        List<String> paths = new ArrayList<>();
        String nodeType = QueryHelper.getNodeType(query.getSource());
        if (NodeTypeRegistry.getInstance().getNodeType(CONTENT_TYPE).isNodeType(nodeType)) {
            try {
                JSONArray items = this.queryXML();

                for(int i = 1; i <= items.length(); ++i) {
                    JSONObject product = (JSONObject)items.get(i - 1);
                    String path = "/" + XmlUtils.displayNumberTwoDigits(i) + "-" + "product" + "-" + product.get(ID);
                    paths.add(path);
                }
            } catch (JSONException e) {
                throw new RepositoryException(e);
            }
        }

        return paths;
    }

    @Override
    public void saveItem(ExternalData data) throws RepositoryException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(this.xmlFilePath);
            Map<String, String[]> productProps = data.getProperties();
            String productId = productProps.containsKey(ID) ? ((String[])productProps.get(ID))[0] : "";
            boolean isNew = true;
            Element rootElement = doc.getDocumentElement();
            NodeList nList = doc.getElementsByTagName(ELEMENT_TAG);

            Element eElement;
            for(int index = 0; index < nList.getLength(); ++index) {
                Node nNode = nList.item(index);
                if (nNode.getNodeType() == 1) {
                    eElement = (Element)nNode;
                    XmlProductTO productTO = new XmlProductTO(eElement);
                    if (productTO.getId().trim().equals(productId.trim())) {
                        setValue(NAME, eElement, productProps.containsKey(NAME) ? ((String[])productProps.get(NAME))[0] : "");
                        setValue(MODEL, eElement, productProps.containsKey(MODEL) ? ((String[])productProps.get(MODEL))[0] : "");
                        setValue(DESCRIPTION, eElement, productProps.containsKey(DESCRIPTION) ? ((String[])productProps.get(DESCRIPTION))[0] : "");
                        setValue(IMAGE, eElement, productProps.containsKey(IMAGE) ? ((String[])productProps.get(IMAGE))[0] : "");
                        setValue(PRICE, eElement, productProps.containsKey(PRICE) ? ((String[])productProps.get(PRICE))[0] : "");
                        setValue(MANUAL, eElement, productProps.containsKey(MANUAL) ? ((String[])productProps.get(MANUAL))[0] : "");
                        logger.info("saveItem(), XML update Current Element :" + nNode.getNodeName());
                        isNew = false;
                    }
                }
            }

            if (isNew) {
                Element newProduct = doc.createElement(ELEMENT_TAG);
                Element idElement = doc.createElement(ID);
                idElement.appendChild(doc.createTextNode(productProps.containsKey(ID) ? ((String[])productProps.get(ID))[0] : ""));
                eElement = doc.createElement(NAME);
                eElement.appendChild(doc.createTextNode(productProps.containsKey(NAME) ? ((String[])productProps.get(NAME))[0] : ""));
                Element modelElement = doc.createElement(MODEL);
                modelElement.appendChild(doc.createTextNode(productProps.containsKey(MODEL) ? ((String[])productProps.get(MODEL))[0] : ""));
                Element descriptionElement = doc.createElement(DESCRIPTION);
                descriptionElement.appendChild(doc.createTextNode(productProps.containsKey(DESCRIPTION) ? ((String[])productProps.get(DESCRIPTION))[0] : ""));
                Element imageElement = doc.createElement(IMAGE);
                imageElement.appendChild(doc.createTextNode(productProps.containsKey(IMAGE) ? ((String[])productProps.get(IMAGE))[0] : ""));
                Element priceElement = doc.createElement(PRICE);
                priceElement.appendChild(doc.createTextNode(productProps.containsKey(PRICE) ? ((String[])productProps.get(PRICE))[0] : ""));
                Element manualElement = doc.createElement(MANUAL);
                manualElement.appendChild(doc.createTextNode(productProps.containsKey(MANUAL) ? ((String[])productProps.get(MANUAL))[0] : ""));
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
            logger.info("saveItem(), Done......");
        } catch (Exception e) {
            logger.error("saveItem(), can't save the item, ", e);
            throw new RepositoryException(e);
        }

    }

    private static void setValue(String tag, Element element, String input) {
        NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodes.item(0);
        node.setTextContent(input);
    }

    @Override
    public Set<String> getSupportedNodeTypes() {
        return Sets.newHashSet(CONTENT_FOLDER, CONTENT_TYPE);
    }

    @Override
    public boolean isSupportsHierarchicalIdentifiers() {
        return false;
    }

    @Override
    public boolean isSupportsUuid() {
        return false;
    }

    @Override
    public boolean itemExists(String path) {
        return false;
    }

    @Override
    public void move(String oldPath, String newPath) throws RepositoryException {
        logger.info("Move : oldPath=" + oldPath + " newPath=" + newPath);
    }

    @Override
    public void order(String path, List<String> children) throws RepositoryException {
        logger.info("Order : path=" + path);
    }

    @Override
    public void removeItemByPath(String path) throws RepositoryException {
        logger.info("Remove item by path : path=" + path);
    }
}
