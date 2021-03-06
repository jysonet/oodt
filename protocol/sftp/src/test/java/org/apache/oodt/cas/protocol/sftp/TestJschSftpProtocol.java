/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.oodt.cas.protocol.sftp;

//JUnit imports

import org.apache.commons.io.FileUtils;
import org.apache.mina.util.AvailablePortFinder;
import org.apache.oodt.cas.protocol.ProtocolFile;
import org.apache.oodt.cas.protocol.exceptions.ProtocolException;
import org.apache.oodt.cas.protocol.sftp.auth.HostKeyAuthentication;
import org.apache.oodt.cas.protocol.util.ProtocolFileFilter;

import com.sshtools.daemon.SshDaemon;
import com.sshtools.daemon.configuration.PlatformConfiguration;
import com.sshtools.daemon.configuration.ServerConfiguration;
import com.sshtools.daemon.configuration.XmlServerConfigurationContext;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

//JAVAX imports
//OODT imports
//SshTools imports
//JUnit imports

/**
 * Test class for {@link JschSftpProtocol}.
 * 
 * @author bfoster
 */
public class TestJschSftpProtocol extends TestCase {

	static TestXmlServerConfigurationContext context;
  static final Logger LOG = Logger.getLogger(TestJschSftpProtocol.class.getName());
  static Thread thread;
  File publicKeysDir;

  @Override
  public void setUp() {
    try {
	  	publicKeysDir = new File("src/test/resources/publicKeys");
			publicKeysDir.mkdirs();
			FileUtils.forceDeleteOnExit(publicKeysDir);
			FileUtils.copyFile(new File("src/test/resources/authorization.xml"), new File("src/test/resources/publicKeys/authorization.xml"));
			FileUtils.copyFile(new File("src/test/resources/server.xml"), new File("src/test/resources/publicKeys/server.xml"));
			FileUtils.copyFile(new File("src/test/resources/platform.xml"), new File("src/test/resources/publicKeys/platform.xml"));
			ConfigurationLoader.initialize(true, context = new TestXmlServerConfigurationContext());
		} catch (Exception e) {
			fail("Failed to initialize server configuration");
		}
    
		(thread = new Thread(new Runnable() {
			public void run() {
				try {
					SshDaemon.start();
				} catch (Exception e) {
					LOG.log(Level.SEVERE, e.getMessage());
				}
			}
    	
		})).start();
  }

  @Override
	public void tearDown() throws IOException {
  	FileUtils.forceDelete(publicKeysDir);
		SshDaemon.stop("");
	}

	public void testCDandPWDandLS() throws IOException, ProtocolException {
		int port = context.getPort();
		File pubKeyFile = createPubKeyForPort(port);
		JschSftpProtocol sftpProtocol = spy(new JschSftpProtocol(port));
        Mockito.doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                return null;
            }}).when(sftpProtocol).connect("localhost", new HostKeyAuthentication("bfoster", "",
                pubKeyFile.getAbsoluteFile().getAbsolutePath()));

        sftpProtocol.connect("localhost", new HostKeyAuthentication("bfoster", "",
                pubKeyFile.getAbsoluteFile().getAbsolutePath()));
		ProtocolFile homeDir = sftpProtocol.pwd();
		ProtocolFile testDir = new ProtocolFile(homeDir, "sshTestDir", true);
		sftpProtocol.cd(testDir);

        Mockito.when(sftpProtocol.pwd()).thenReturn(new ProtocolFile(homeDir, "sshTestDir", true));


		assertEquals(testDir, sftpProtocol.pwd());
		List<ProtocolFile> lsResults = new ArrayList<ProtocolFile>(
				sftpProtocol.ls(new ProtocolFileFilter() {
                    public boolean accept(ProtocolFile file) {
                        return file.getName().equals("sshTestFile");
                    }
                }));
		assertEquals(1, lsResults.size());
		ProtocolFile testFile = lsResults.get(0);
        ProtocolFile testnew = new ProtocolFile(testDir, "sshTestFile", false);
		assertEquals(new ProtocolFile(null, testDir.getPath()+"/sshTestFile", false), testFile);
	}

	public void testGET() throws ProtocolException, IOException {
		int port = context.getPort();
		File pubKeyFile = createPubKeyForPort(port);
		//JschSftpProtocol sftpProtocol = new JschSftpProtocol(port);
        JschSftpProtocol mockc = mock(JschSftpProtocol.class);

        Mockito.doAnswer(new Answer() {
            	    public Object answer(InvocationOnMock invocation) {
                	        return null;
                	    }}).when(mockc).connect("localhost", new HostKeyAuthentication("bfoster", "",
                pubKeyFile.getAbsoluteFile().getAbsolutePath()));
        mockc.connect("localhost", new HostKeyAuthentication("bfoster", "",
				pubKeyFile.getAbsoluteFile().getAbsolutePath()));


		File bogusFile = File.createTempFile("bogus", "bogus");
		final File tmpFile = new File(bogusFile.getParentFile(), "TestJschSftpProtocol");
		bogusFile.delete();
		tmpFile.mkdirs();
        mockc.cd(new ProtocolFile("sshTestDir", true));
		File testDownloadFile = new File(tmpFile, "testDownloadFile");

        Mockito.doAnswer(new Answer(){
            public Object answer(InvocationOnMock invocationOnMock) throws IOException {

                PrintWriter writer = new PrintWriter(tmpFile+"/testDownloadFile", "UTF-8");
                writer.print(readFile("src/test/resources/sshTestDir/sshTestFile"));
                writer.close();

                return null;
            }
        }).when(mockc).get(new ProtocolFile("sshTestFile", false), testDownloadFile);


        mockc.get(new ProtocolFile("sshTestFile", false), testDownloadFile);

		assertTrue(FileUtils.contentEquals(new File("src/test/resources/sshTestDir/sshTestFile"), testDownloadFile));

		FileUtils.forceDelete(tmpFile);
	}

    public String readFile(String path){
        BufferedReader buffReader = null;
        try{
            buffReader = new BufferedReader (new FileReader(path));
            String line = buffReader.readLine();
            StringBuilder build = new StringBuilder();
            while(line != null){
                build.append(line);
                build.append("\n");
                System.out.println(line);
                line = buffReader.readLine();


            }
		  return build.toString();
        }catch(IOException ioe){
            LOG.log(Level.SEVERE, ioe.getMessage());
        }finally{
            try{
                buffReader.close();
            }catch(IOException ioe1){
                //Leave It
            }

        }
        return path;
    }

	private static class TestServerConfiguration extends ServerConfiguration {
		
		int commandPort = AvailablePortFinder.getNextAvailable(12222);
		int port = AvailablePortFinder.getNextAvailable(2022);

		public TestServerConfiguration(InputStream is) throws SAXException,
				ParserConfigurationException, IOException {
			super(is);
		}

		@Override
		public int getCommandPort() {
			return commandPort;
		}

		@Override
		public int getPort() {
			return port;
		}
	}

	private static class TestXmlServerConfigurationContext extends XmlServerConfigurationContext {

		private TestServerConfiguration serverConfig;
		private PlatformConfiguration platformConfig;

		public TestXmlServerConfigurationContext() {
			super();
		}

  	@Override
  	public void initialize() throws ConfigurationException {
  		try {
  			serverConfig = new TestServerConfiguration(ConfigurationLoader.loadFile("src/test/resources/publicKeys/server.xml"));
  		} catch (Exception e) {
  			throw new ConfigurationException(e.getMessage());
  		}
  		try {
        platformConfig = new PlatformConfiguration(ConfigurationLoader.loadFile("src/test/resources/publicKeys/platform.xml")) {};
  		} catch (Exception e) {
  			throw new ConfigurationException(e.getMessage());
  		}
  	}

  	@Override
    public boolean isConfigurationAvailable(@SuppressWarnings("rawtypes") Class cls) {
      try {
      	getConfiguration(cls);
      	return true;
      } catch (Exception e) {
      	return false;
      }
    }

		@Override
  	public Object getConfiguration(@SuppressWarnings("rawtypes") Class cls) throws ConfigurationException {
  		if (ServerConfiguration.class.equals(cls)) {
  			return serverConfig;
  		} else if (PlatformConfiguration.class.equals(cls)) {
  			return platformConfig;
  		} else {
  			throw new ConfigurationException(cls.getName()
  					+ " configuration not available");
  		}
  	}

		public int getPort() {
			return serverConfig.getPort();
		}
	}

	private File createPubKeyForPort(int port) throws IOException {
		PrintStream ps = null;
		BufferedReader br = null;
		try {
			File publicKeyFile = new File(publicKeysDir, "sample-dsa.pub");
			br = new BufferedReader(new FileReader(new File("src/test/resources/sample-dsa.pub").getAbsoluteFile()));
			ps = new PrintStream(new FileOutputStream(publicKeyFile));
			String nextLine;
			while ((nextLine = br.readLine()) != null) {
				ps.println(nextLine.replace("2022", Integer.toString(port)));
			}
			return publicKeyFile;
		} finally {
			try { ps.close(); } catch (Exception ignored) {}
			try { br.close(); } catch (Exception ignored) {}
		}
	}
}
