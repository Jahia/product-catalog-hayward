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

import org.hibernate.validator.constraints.NotEmpty;
import org.jahia.modules.external.admin.mount.AbstractMountPointFactory;
import org.jahia.services.content.JCRNodeWrapper;
import org.osgi.framework.BundleContext;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.Serializable;

public class XmlMountPointFactory extends AbstractMountPointFactory implements Serializable {
    private static final long serialVersionUID = -41541258548484556L;
    public static final String XMLPATH_PROPERTY = "j:xmlPath";
    public static final String XMLMOUNTPOINT_NT = "jnt:xmlMountPoint";
    public static final String MOUNTPOINT_NT = "mountPoint";

    @NotEmpty
    private String name;

    private String localPath;
    @NotEmpty
    private String root;

    public XmlMountPointFactory() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    @Override
    public void populate(JCRNodeWrapper nodeWrapper) throws RepositoryException {
        super.populate(nodeWrapper);
        this.name = getName(nodeWrapper.getName());
        try {
            this.localPath = nodeWrapper.getProperty(MOUNTPOINT_NT).getNode().getPath();
        } catch (PathNotFoundException e) {

        }
        this.root = nodeWrapper.getPropertyAsString(XMLPATH_PROPERTY);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLocalPath() {
        return localPath;
    }

    @Override
    public String getMountNodeType() {
        return XMLMOUNTPOINT_NT;
    }

    @Override
    public void setProperties(JCRNodeWrapper mountNode) throws RepositoryException {
        mountNode.setProperty(XMLPATH_PROPERTY,root);
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
