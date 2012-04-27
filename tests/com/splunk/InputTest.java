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

import org.junit.Test;

import java.util.Map;
import java.util.Set;

public class InputTest extends SplunkTestCase {

    private void touchSpecificInput(Input input) {
        InputKind inputKind = input.getKind();
        TcpConnections tcpConnections = null;
        UdpConnections udpConnections = null;

        switch (inputKind) {
            case Monitor:
                MonitorInput monitorInput = (MonitorInput) input;
                monitorInput.getBlacklist();
                monitorInput.getCrcSalt();
                monitorInput.getFileCount();
                monitorInput.getFollowTail();
                monitorInput.getHost();
                monitorInput.getHostRegex();
                monitorInput.getIgnoreOlderThan();
                monitorInput.getIndex();
                monitorInput.getQueue();
                monitorInput.getRcvBuf();
                monitorInput.getRecursive();
                monitorInput.getSource();
                monitorInput.getSourceType();
                monitorInput.getTimeBeforeClose();
                monitorInput.getWhitelist();
                break;
            case Script:
                ScriptInput scriptInput = (ScriptInput) input;
                scriptInput.getGroup();
                scriptInput.getHost();
                scriptInput.getIndex();
                scriptInput.getInterval();
                scriptInput.getRcvBuf();
                break;
            case Tcp:
                TcpInput tcpInput = (TcpInput) input;
                tcpInput.getConnectionHost();
                tcpInput.getGroup();
                tcpInput.getHost();
                tcpInput.getIndex();
                tcpInput.getQueue();
                tcpInput.getRcvBuf();
                tcpInput.getRestrictToHost();
                tcpInput.getSource();
                tcpInput.getSourceType();
                tcpInput.getSSL();
                tcpConnections = tcpInput.connections();
                tcpConnections.getConnection();
                tcpConnections.getServername();
                break;
            case TcpSplunk:
                TcpSplunkInput tcpSplunkInput = (TcpSplunkInput) input;
                tcpSplunkInput.getConnectionHost();
                tcpSplunkInput.getGroup();
                tcpSplunkInput.getHost();
                tcpSplunkInput.getIndex();
                tcpSplunkInput.getQueue();
                tcpSplunkInput.getRcvBuf();
                tcpSplunkInput.getSource();
                tcpSplunkInput.getSourceType();
                tcpSplunkInput.getSSL();
                tcpConnections = tcpSplunkInput.connections();
                tcpConnections.getConnection();
                tcpConnections.getServername();
                break;
            case Udp:
                UdpInput udpInput = (UdpInput) input;
                udpInput.getConnectionHost();
                udpInput.getGroup();
                udpInput.getHost();
                udpInput.getIndex();
                udpInput.getQueue();
                udpInput.getRcvBuf();
                udpInput.getSource();
                udpInput.getSourceType();
                udpInput.noAppendingTimeStamp();
                udpInput.noPriorityStripping();
                udpConnections = udpInput.connections();
                udpConnections.getGroup();
                break;
            case WindowsActiveDirectory:
                WindowsActiveDirectoryInput windowsActiveDirectoryInput =
                        (WindowsActiveDirectoryInput) input;
                windowsActiveDirectoryInput.getIndex();
                windowsActiveDirectoryInput.getMonitorSubtree();
                windowsActiveDirectoryInput.getStartingNode();
                windowsActiveDirectoryInput.getTargetDc();
                break;
            case WindowsEventLog:
                WindowsEventLogInput windowsEventLogInput =
                        (WindowsEventLogInput) input;
                windowsEventLogInput.getHosts();
                windowsEventLogInput.getIndex();
                windowsEventLogInput.getLocalName();
                windowsEventLogInput.getLogs();
                windowsEventLogInput.getLookupHost();
                break;
            case WindowsPerfmon:
                WindowsPerfmonInput windowsPerfmonInput =
                        (WindowsPerfmonInput) input;
                windowsPerfmonInput.getCounters();
                windowsPerfmonInput.getIndex();
                windowsPerfmonInput.getInstances();
                windowsPerfmonInput.getInterval();
                windowsPerfmonInput.getObject();
                break;
            case WindowsRegistry:
                WindowsRegistryInput windowsRegistryInput =
                        (WindowsRegistryInput) input;
                windowsRegistryInput.getBaseline();
                windowsRegistryInput.getHive();
                windowsRegistryInput.getIndex();
                windowsRegistryInput.getMonitorSubnodes();
                windowsRegistryInput.getProc();
                windowsRegistryInput.getType();
                break;
            case WindowsWmi:
                WindowsWmiInput windowsWmiInput = (WindowsWmiInput) input;
                windowsWmiInput.getClasses();
                windowsWmiInput.getFields();
                windowsWmiInput.getIndex();
                windowsWmiInput.getInstances();
                windowsWmiInput.getInterval();
                windowsWmiInput.getLocalName();
                windowsWmiInput.getLookupHost();
                windowsWmiInput.getServer();
                windowsWmiInput.getWql();
                break;
        }
    }

    @Test public void testInputs() {
        Service service = connect();

        InputCollection inputs = service.getInputs();

        // Iterate inputs and make sure we can read them.
        for (Input input : inputs.values()) {
            input.getName();
            input.getTitle();
            input.getPath();
            input.getKind();
            touchSpecificInput(input);
        }
    }

    @Test public void testInputCrud() {
        Service service = connect();

        InputCollection inputCollection = service.getInputs();

        // Tcp inputs require the name to be the input's port number.
        String name = "9999"; // test port

        if (inputCollection.containsKey(name))
            fail("Input test port already exists: " + name);

        assertFalse(inputCollection.containsKey(name));

        inputCollection.create(name, InputKind.Tcp);
        assertTrue(inputCollection.containsKey(name));

        TcpInput tcpInput = (TcpInput)inputCollection.get(name);

        Args args = new Args();
        args.put("sourcetype", "sdk-tests");
        tcpInput.update(args);
        assertEquals(tcpInput.get("sourcetype"), "sdk-tests");

        tcpInput.remove();
        inputCollection.refresh();
        assertFalse(inputCollection.containsKey(name));

        // create monitor
        String filename;
        ServiceInfo info = service.getInfo();
        if (info.getOsName().equals("Windows"))
            filename = "C:\\Windows\\WindowsUpdate.log"; // normally here
        else if (info.getOsName().equals("Linux"))
            filename = "/var/log/messages";
        else if (info.getOsName().equals("Darwin")) {
            filename = "/var/log/system.log";
        } else {
            throw new Error("OS: " + info.getOsName() + " not supported");
        }
        if (inputCollection.containsKey(filename)) {
            inputCollection.remove(filename);
        }

        // create/read/update/remove Monitor Input
        inputCollection.create(filename, InputKind.Monitor);
        MonitorInput monitorInput = (MonitorInput)inputCollection.get(filename);
        monitorInput.setBlacklist("phonyregex*1");
        monitorInput.setCheckIndex(true);
        monitorInput.setCheckPath(true);
        monitorInput.setCrcSalt("ThisIsSalt");
        monitorInput.setFollowTail(false);
        monitorInput.setHost("three.four.com");
        monitorInput.setHostRegex("host*regex*");
        monitorInput.setHostSegment("");
        monitorInput.setIgnoreOlderThan("1d");
        monitorInput.setIndex("main");
        monitorInput.setRecursive(false);
        monitorInput.setRenameSource("renamedSource");
        monitorInput.setSourcetype("monitor");
        monitorInput.setTimeBeforeClose(120);
        monitorInput.setWhitelist("phonyregex*2");
        monitorInput.update();
        monitorInput.disable();
        // some attributes are write only; check what we can.
        assertEquals(monitorInput.getBlacklist(), "phonyregex*1");
        assertEquals(monitorInput.getFollowTail(), false);
        assertEquals(monitorInput.getHost(), "three.four.com");
        assertEquals(monitorInput.getHostRegex(), "host*regex*");
        assertEquals(monitorInput.getIgnoreOlderThan(), "1d");
        assertEquals(monitorInput.getIndex(), "main");
        assertEquals(monitorInput.getRecursive(), false);
        assertEquals(monitorInput.getSource(), "renamedSource");
        assertEquals(monitorInput.getSourceType(), "monitor");
        assertEquals(monitorInput.getTimeBeforeClose(), 120);
        assertEquals(monitorInput.getWhitelist(), "phonyregex*2");
        monitorInput.remove();


    }
}
