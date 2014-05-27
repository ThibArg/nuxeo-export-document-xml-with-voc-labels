/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and others.
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

import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;

/**
 * @author Thibaud Arguillere
 */
@Operation(id=ExportDocAsXMLWithVocLabelsOp.ID, category=Constants.CAT_CONVERSION, label="Export document as XML with voc. labels", description="Generates an XML export of all the document's properties. The ouput is the exact same as when the user selects \"XML Export\" in the \"Export Options\" toolbar action <b>but</b> it also exports the labels of the vocabularies.<p>This operation uses a naming convention to detect what vocabulary must be used: The name of the field must end with <code>_voc_theName</code>. So for example, if a we have a <code>color</code> field which is always bound to the <code>Colors</code> vocabulary, the name of the field must be changed to <code>color_voc_Colors</code></p><p>In the XML, a new entry is added immediately after the field value. It is displayed like a regular field, with the <code>_vocabularyLabel</code> suffix</p>")
public class ExportDocAsXMLWithVocLabelsOp {

    public static final String ID = "ExportDocAsXMLWithVocLabelsOp";

    @Context
    public OperationContext context;

    @OperationMethod(collector=BlobCollector.class)
    public Blob run(DocumentModel input) throws Exception {

        ExportedDocumentAsXMLWithVocLabels ed = new ExportedDocumentAsXMLWithVocLabels(input);
        String xmlStr = ed.getXML();

        StringBlob blob = new StringBlob( xmlStr, "text/xml", "UTF-8" );
        blob.setFilename(input.getTitle() + ".xml");

        return blob;
    }
}
