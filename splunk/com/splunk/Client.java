/*
 * Copyright 2011 Splunk, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"): you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.splunk;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Parent class that all other endpoints classes will derive from
 */

public class Client {

    public Service service = null;

    public Client() {
        // default constructor
    }
    public Client(Service serv) {
        service = serv;
    }

    private List<String> getList(Document doc, String path) {
        List<String> outlist = new ArrayList<String>();
        NodeList nl = doc.getElementsByTagName("id");
        // extract last element of base path
        String [] bits = path.split("/");
        String delimiter = bits[bits.length-1] + "/";

        // get everything in ID after last path prefix element
        for (int idx=1; idx < nl.getLength(); idx++) {
            String [] pieces = nl.item(idx).getTextContent().split(delimiter);
            outlist.add(pieces[1]);
        }

        return outlist;
    }

    public Element get(String path) throws Exception {
        Convert converter = new Convert();
        return converter.convertXMLData(service.get(path).getContent());
    }

    public Element delete(String path) throws Exception {
        Convert converter = new Convert();
        // place holder -- service needs a delete method
        return converter.convertXMLData(service.delete(path).getContent());
    }

    public Element create(String path,
                          Map<String,String>args) throws Exception {
        Convert converter = new Convert();
        return converter.convertXMLData(service.post(path, args).getContent());
    }

    public List<String> nameList(String path) throws Exception {
        Document doc = service.parseXml(service.get(path));
        return getList(doc, path);
    }
}