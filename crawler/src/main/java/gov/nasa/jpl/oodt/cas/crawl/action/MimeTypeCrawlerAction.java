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

//OODT imports
import gov.nasa.jpl.oodt.cas.crawl.config.ProductCrawlerBean;
import gov.nasa.jpl.oodt.cas.crawl.structs.exceptions.CrawlerActionException;
import gov.nasa.jpl.oodt.cas.metadata.Metadata;

//JDK imports
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

//Spring imports
import org.springframework.beans.factory.annotation.Required;

/**
 * 
 * @author bfoster
 * @version $Revision$
 * 
 *          <p>
 *          Calls a {@link CrawlerAction} if this {@link File} matches the
 *          specified set of internal {@link #mimeTypes}.
 *          </p>
 * 
 */
public class MimeTypeCrawlerAction extends CrawlerAction {

	private CrawlerAction actionToCall;

	private List<String> mimeTypes;

	@Override
	public boolean performAction(File product, Metadata productMetadata)
			throws CrawlerActionException {
		List<String> mimeTypeHierarchy = productMetadata
				.getAllMetadata(ProductCrawlerBean.MIME_TYPES_HIERARCHY);
		if (mimeTypeHierarchy == null)
			mimeTypeHierarchy = new Vector<String>();
		if (mimeTypes == null
				|| (mimeTypes != null && !Collections.disjoint(mimeTypes,
						mimeTypeHierarchy))) {
			return this.actionToCall.performAction(product, productMetadata);
		} else {
			LOG.log(Level.INFO, "Skipping action (id = " + this.getId()
					+ " : description = " + this.getDescription()
					+ " - doesn't apply to current product");
			return true;
		}
	}

	@Required
	public void setActionToCall(CrawlerAction actionToCall) {
		this.actionToCall = actionToCall;
		this.setDescription("Mime-type restricted version of '"
				+ this.actionToCall.getId() + "' CrawlerAction");
		this.setPhases(this.actionToCall.getPhases());
	}

	public List<String> getMimeTypes() {
		return mimeTypes;
	}

	public void setMimeTypes(List<String> mimeTypes) {
		this.mimeTypes = mimeTypes;
	}

}
