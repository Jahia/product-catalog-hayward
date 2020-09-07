//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package to;

import org.w3c.dom.Element;

public class XmlProductTO {
    private Integer id;
    private String name;
    private String model;
    private String description;
    private Double price;
    private String image;
    private String manual;

    public XmlProductTO(Element element) {
        try {
            this.setId(element.getElementsByTagName("id").item(0).getTextContent());
        } catch (Exception var9) {
        }

        try {
            this.name = element.getElementsByTagName("name").item(0).getTextContent();
        } catch (Exception var8) {
        }

        try {
            this.model = element.getElementsByTagName("model").item(0).getTextContent();
        } catch (Exception var7) {
        }

        try {
            this.description = element.getElementsByTagName("description").item(0).getTextContent();
        } catch (Exception var6) {
        }

        try {
            this.setPrice(element.getElementsByTagName("price").item(0).getTextContent());
        } catch (Exception var5) {
        }

        try {
            this.image = element.getElementsByTagName("image").item(0).getTextContent();
        } catch (Exception var4) {
        }

        try {
            this.manual = element.getElementsByTagName("manual").item(0).getTextContent();
        } catch (Exception var3) {
        }

    }

    public String getId() {
        return this.id.toString();
    }

    public void setId(String id) {
        this.id = Integer.parseInt(id);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return this.price;
    }

    public void setPrice(String price) {
        this.price = Double.parseDouble(price);
    }

    public String toJsonString() {
        return "{    \"id\": " + (this.id != null ? this.id : "") + ",\n    \"name\": \"" + (this.name != null ? this.name : "") + "\",\n    \"description\": \"" + (this.description != null ? this.description : "") + "\",\n    \"price\": " + (this.price != null ? this.price : "") + ",\n    \"image\": \"" + (this.image != null ? this.image : "") + "\",\n    \"model\": \"" + (this.model != null ? this.model : "") + "\",\n    \"manual\": \"" + (this.manual != null ? this.manual : "") + "\"\n  }";
    }
}
