/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     thibaud
 */
package org.nuxeo.export.doc.xml;

import java.io.IOException;

import org.nuxeo.ecm.core.api.DataModel;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.ecm.core.schema.Namespace;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.directory.DirectoryException;
import org.nuxeo.ecm.directory.api.DirectoryService;
import org.nuxeo.runtime.api.Framework;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.QName;

/**
 * @author Thibaud Arguillere
 *
 * @since 5.9.3
 *
 * The ExportedDocumentImpl does everything already: Convert the document's properties to XML.
 * We extend this class and override just its readProperty() method, so we can add the
 * vocabulary label to the XML tree.
 *
 * Usage is simple: Pass a DocumentModel to the constructor, then call the getXML() method:
 *
 *      . . . input is a Nuxeo document . . .
 *      ExportedDocumentAsXMLWithVocLabels ed = new ExportedDocumentAsXMLWithVocLabels(input);
 *      String xml = ed.getXML();
 *          . . .
 *
 * Basically, ExportedDocumentImpl walks every schema and every field in each schema. But of
 * course, there is no way to tell that field some_field is bound to the vocabulary some_voc.
 * So in this version of the class, we are using a naming convention:
 *      - The name of the field must ends with the name (id) of the vocabulary
 *      - A tag marks the fact that there is a vocabulary in the name= "_voc_"
 *
 * So, for example, if we have a "main_department" field which is bound to the "Department"
 * vocabulary, then the name of the field must be changed to:
 *
 *          main_department_voc_Department
 *          |--------------|----|---------|
 *            "real" name   tag  Vocabulary
 *
 * Then, in the XML, the label will be seen as a field: Same name as the original field with
 * the "_vocabularyLabel" suffix. So, for example, we could have something like...
 *
 *          <my_doc_type:main_department_voc_Department>
 *            <![CDATA[ as ]]>
 *          </my_doc_type:main_department_voc_Department>
 *          <my_doc_type:main_department_voc_Department_vocabularyLabel>
 *            <![CDATA[ Accounting Services ]]>
 *          </my_doc_type:main_department_voc_Department_vocabularyLabel>
 *
 * ...where "as" is the value stored in the field (my_doc_type:main_department_voc_Department)
 * and "Accounting Services" is the label.
 */
public class ExportedDocumentAsXMLWithVocLabels extends ExportedDocumentImpl {

    public static final String kFIELD_EXTENSION_FOR_XML = "_vocabularyLabel";
    public static final String kFIELD_TAG_FOR_VOCABULARY = "_voc_";

    private static final Log log = LogFactory.getLog(ExportedDocumentAsXMLWithVocLabels.class);

    protected DirectoryService directoryService = null;

    ExportedDocumentAsXMLWithVocLabels(DocumentModel doc) throws IOException {
        super(doc);
    }

    @Override
    protected void readProperty(Element parent, Namespace targetNs,
            Field field, Object value, boolean inlineBlobs) throws IOException {

        // In all cases, first return the field value
        super.readProperty(parent, targetNs, field, value, inlineBlobs);

        // Then, add the label if the field declares it uses a vocabulary
        // by following the expected naming convention
        if (value != null && field.getType().isSimpleType()) {
            String fieldName = field.getName().toString();
            int pos = fieldName.indexOf(kFIELD_TAG_FOR_VOCABULARY);
            if (pos > 0) {
                String vocName = fieldName.substring(pos + kFIELD_TAG_FOR_VOCABULARY.length());
                if (vocName.isEmpty()) {
                    log.error("Found tag \"" + kFIELD_TAG_FOR_VOCABULARY
                            + "\" in field name " + fieldName
                            + ", but no vocabulary label afetr the tag");
                } else {
                    String fieldValue = value.toString();
                    org.nuxeo.ecm.directory.Session session = null;
                    try {
                        session = getDirService().open(vocName);
                        if (session == null) { // Vocabulary not found
                            log.error("Vocabulary '" + vocName + "' not found");
                        } else {
                            DocumentModel directoryDoc = session.getEntry(fieldValue);
                            if(directoryDoc == null) {
                                log.error("Entry id <" + fieldValue +"> not found in vocabulary " + vocName );
                            } else {
                                DataModel dm = directoryDoc.getDataModels().values().iterator().next();
                                String label = (String) dm.getData("label");

                                QName name = QName.get(field.getName().getLocalName() + kFIELD_EXTENSION_FOR_XML,
                                                        targetNs.prefix, targetNs.uri);
                                Element element = parent.addElement(name);
                                element.addCDATA(label);// Should be encoded?
                            }
                        }

                    } catch (Exception e) {
                        log.error(e);
                    } finally {
                        if(session != null) {
                            try {
                                session.close();
                            } catch (DirectoryException e) {
                                // We ignore this one
                            }
                        }
                    }
                }
            }
        }
    }

    String getXML() throws IOException {

        return super.document.asXML();

    }

    protected DirectoryService getDirService() throws Exception {
        if (directoryService == null) {
            directoryService = Framework.getService(DirectoryService.class);
        }
        return directoryService;
    }

}
