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
package org.apache.oodt.cas.catalog.cli.action;

//OODT imports
//import org.apache.oodt.cas.catalog.system.impl.CatalogServiceClient;
import org.apache.oodt.cas.cli.exception.CmdLineActionException;

/**
 * A {@link CmdLineAction} which get a list of supported {@link Catalog}
 * IDs for given {@link CatalogServiceClient}.
 *
 * @author bfoster (Brian Foster) 
 */
public class GetSupportedCatalogIdsCliAction extends CatalogServiceCliAction {

   @Override
   public void execute(ActionMessagePrinter printer)
         throws CmdLineActionException {
      try {
         printer.println("CatalogIDs: " + getClient().getCurrentCatalogIds());
      } catch (Exception e) {
         throw new CmdLineActionException(
               "Failed to get supported Catalog IDs : " + e.getMessage(), e);
      }
   }
}
