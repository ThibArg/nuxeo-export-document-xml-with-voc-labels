nuxeo-export-document-xml-with-voc-labels
=========================================

This plug-in contains a class wich exports a Nuxeo Document (whatever its type) as XML, and an operation wich calls the class.


### Usage Requires a Naming Convention for Fields
It does exactly the same as the "Export XML" option of the "Export Options" toolbar item, but it adds an XML entry for fields bound to a vocabulary: The field stores the `id` of the vocabulary, the plug-in adds the `label` to the XML.

The code walks every schema and every field in each schema of the document. The point is: There is no way to tell that field `some_field` is bound to the vocabulary `some_voc`. This is why this plug-in equires you to use a _strict naming convention_ in the names of your fields bound to a vocabulary:
* The name of the field must ends with the name (id) of the vocabulary
* A tag marks the fact that there is a vocabulary in the name: "\_voc\_"


So, for example, if we have a `main_department` field which is bound to the `Department` vocabulary, then the name of the field must be changed to `main_department_voc_Department`:
```
main_department_voc_Department
|--------------|----|---------|
  "real" name   tag  Vocabulary
```

Then, in the XML, the label will be seen as a field: Same name as the original field with the "_vocabularyLabel" suffix. So, for example, we could have something like...
```XML
<my_doc_type:main_department_voc_Department>
  <![CDATA[ as ]]>
</my_doc_type:main_department_voc_Department>
<my_doc_type:main_department_voc_Department_vocabularyLabel>
  <![CDATA[ Accounting Services ]]>
</my_doc_type:main_department_voc_Department_vocabularyLabel>
```
...where "as" is the value stored in the field (`my_doc_type:main_department_voc_Department`), and "Accounting Services" is the label.

### Using the `ExportDocAsXMLWithVocLabelsOp` Operation in a chain

To use the operation in an Automation Chain using Studio, you must first declare the operation in your Studio project registries, so Nuxeo Studio knows it, knows its input/output and display its description. Adding the operation to your project can be done directly from [Nuxeo IDE in Eclipse](http://doc.nuxeo.com/display/public/IDEDOC/Uploading+Custom+Operations+in+Nuxeo+Studio), or you can [copy-paste the JSON](http://doc.nuxeo.com/display/Studio/Referencing+an+Externally+Defined+Operation) definition of the operation in the registry.

Once the Studio Registry is up-to-date, the operation is available in the "Automation Chains" topic and you can use it. A typical example would be to add a "User Action" which allows the user to download the XML representation of the document. So, you create a "User Action", and bind it to an automation chain which would contain 3 operations:
```
Fetch > Contect Document(s)
Conversion > Export document as XML with voc. labels
User Interface > Download File
```

### Build and Install

Assuming maven (at least 3.2.1) is correctly setup on your computer:

    cd /path/to/nuxeo-export-document-xml-with-voc-labels
    mvn install
    # The plug-in is in /target, named nuxeo-export-document-xml-with-voc-labels-{version}.jar

Just drop this .jar in the "plugins" directory of your server (or in the "bundles" directory)

### MIT License

Copyright 2014 Nuxeo

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


### About Nuxeo

Nuxeo provides a modular, extensible Java-based open source software platform for enterprise content management and packaged applications for document management, digital asset management and case management. Designed by developers for developers, the Nuxeo platform offers a modern architecture, a powerful plug-in model and extensive packaging capabilities for building content applications.

More information on: <http://www.nuxeo.com/>