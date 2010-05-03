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


package gov.nasa.jpl.oodt.cas.crawl.action;

//JDK imports
import java.io.File;
import java.util.logging.Level;

//OODT imports
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * @author bfoster
 * @author mattmann
 * @version $Revision$
 * 
 * <p>
 * Moves a {@link Product} file as a reponse to a Crawler lifecycle phase
 * </p>.
 */
public class MoveFile extends CrawlerAction {

    private String file;

    private String toDir;

    private String fileExtension;
    
    private boolean createToDir;

    public MoveFile() {
    	super();
    	this.createToDir = false;
    }
    
    public boolean performAction(File product, Metadata productMetadata)
            throws CrawlerActionException {
    	String mvFile = file;
        try {
            if (mvFile == null) {
            	mvFile = product.getAbsolutePath();
                if (this.fileExtension != null)
                    mvFile += "." + this.fileExtension;
            }
            File srcFile = new File(mvFile);
            File toFile = new File(toDir + "/" + srcFile.getName());
            if (createToDir)
            	toFile.getParentFile().mkdirs();
            LOG.log(Level.INFO, "Moving file " + srcFile.getAbsolutePath() 
            		+ " to " + toFile.getAbsolutePath());
            return srcFile.renameTo(toFile);
        } catch (Exception e) {
            throw new CrawlerActionException("Failed to move file from " + mvFile
                    + " to " + this.toDir + " : " + e.getMessage());
        }
    }
    
    public void setCreateToDir(boolean createToDir) {
    	this.createToDir = createToDir;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Required
    public void setToDir(String toDir) {
        this.toDir = toDir;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

}
