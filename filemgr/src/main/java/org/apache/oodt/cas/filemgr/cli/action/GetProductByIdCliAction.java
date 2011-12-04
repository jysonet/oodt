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
package org.apache.oodt.cas.filemgr.cli.action;

//Apache imports
import org.apache.commons.lang.Validate;

//OODT imports
import org.apache.oodt.cas.filemgr.structs.Product;

/**
 * A {@link CmdLineAction} which gets information about a {@link Product}
 * by ID.
 *
 * @author bfoster (Brian Foster)
 */
public class GetProductByIdCliAction extends AbstractGetProductCliAction {

   private String productId;

   @Override
   public Product getProduct() throws Exception {
      Validate.notNull(productId, "Must specify productId");

      Product p = getClient().getProductById(productId);
      if (p == null) {
         throw new Exception("FileManager returned null product for id '"
               + productId + "'");
      }
      return p;
   }

   public void setProductId(String productId) {
      this.productId = productId;
   }
}
