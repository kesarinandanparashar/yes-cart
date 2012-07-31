/*
 * Copyright 2009 Igor Azarnyi, Denys Pavlov
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */





package org.yes.cart.icecat.transform.xml

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import org.yes.cart.icecat.transform.domain.Product
import org.yes.cart.icecat.transform.domain.ProductFeature
import org.yes.cart.icecat.transform.domain.Feature
import org.yes.cart.icecat.transform.domain.Category
import org.yes.cart.icecat.transform.domain.CategoryFeatureGroup
import org.yes.cart.icecat.transform.Util

/**
 * User: Igor Azarny iazarny@yahoo.com
 * Date: 5/9/12
 * Time: 9:23 PM
 */
class ProductHandler extends DefaultHandler {

    Product product = null;
    Map<String, Category> categoryMap;

    boolean inProductRelated = false



    ProductHandler(Map<String, Category> categoryMap) {
        this.categoryMap = categoryMap
    }

    void startElement(String uri, String localName, String qName, Attributes attributes) {

        if ("ProductRelated" == qName) {
            inProductRelated = true
            String catId = attributes.getValue("Category_ID");
            if (this.categoryMap.containsKey(catId)) {
                product.relatedCategories.add(catId)
            }

        }

        if ("Product" == qName && inProductRelated) {
            product.relatedProduct.add(attributes.getValue("ID"))

        }

        if ("Product" == qName && product == null && !inProductRelated) {
            product = new Product();
            product.Code = Util.maxLength(attributes.getValue("Code"), 255);
            product.HighPic = attributes.getValue("HighPic");
            product.HighPicHeight = attributes.getValue("HighPicHeight");
            product.HighPicSize = attributes.getValue("HighPicSize");
            product.HighPicWidth = attributes.getValue("HighPicWidth");
            product.ID = attributes.getValue("ID");
            product.LowPic = attributes.getValue("LowPic");
            product.LowPicHeight = attributes.getValue("LowPicHeight");
            product.LowPicSize = attributes.getValue("LowPicSize");
            product.LowPicWidth = attributes.getValue("LowPicWidth");
            product.Name = Util.maxLength(attributes.getValue("Name"), 255);
            product.Pic500x500 = attributes.getValue("Pic500x500");
            product.Pic500x500Height = attributes.getValue("Pic500x500Height");
            product.Pic500x500Size = attributes.getValue("Pic500x500Size");
            product.Pic500x500Width = attributes.getValue("Pic500x500Width");
            product.Prod_id = attributes.getValue("Prod_id").replace("_", "-").replace(" ", "-").replace(".", "-").replace("?", "-"); //sku code
            product.Quality = attributes.getValue("Quality");
            product.ReleaseDate = attributes.getValue("ReleaseDate");
            product.ThumbPic = attributes.getValue("ThumbPic");
            product.ThumbPicSize = attributes.getValue("ThumbPicSize");
            product.Title = Util.maxLength(attributes.getValue("Title"), 255);

        }

        if ("Category" == qName) {
            product.CategoryID = attributes.getValue("ID");
        }

        if ("EANCode" == qName) {
            product.EANCode = attributes.getValue("EAN");
        }

        if ("ShortSummaryDescription" == qName) {
            readyToGetShortSummaryDescription = true;
        }

        if ("LongSummaryDescription" == qName) {
            readyToGetLongSummaryDescription = true;
        }

        if ("Supplier" == qName) {
            product.Supplier = attributes.getValue("Name");
        }

        if ("ProductPicture" == qName) {
            product.productPicture.add(attributes.getValue("Pic"));
        }

        if ("ProductFeature" == qName) {
            productFeature = new ProductFeature();
            productFeature.Presentation_Value = attributes.getValue("Presentation_Value")
        }

        if ("Feature" == qName) {
            String featureId = attributes.getValue("ID");
            feature = locateFeature(categoryMap.get(product.CategoryID),  featureId);
            productFeature.feature = feature
        }


    }



    ProductFeature productFeature
    Feature feature

    boolean readyToGetShortSummaryDescription = false;
    boolean readyToGetLongSummaryDescription = false;


    void characters(char[] ch, int start, int length) {
        if (readyToGetShortSummaryDescription) {
            product.ShortSummaryDescription = new String(ch, start, length);
            readyToGetShortSummaryDescription = false;
        }

        if (readyToGetLongSummaryDescription) {
            product.LongSummaryDescription = new String(ch, start, length);
            readyToGetLongSummaryDescription = false;
        }

    }

    Feature locateFeature(Category category, String featureId) {

        if (category != null) {
            for(CategoryFeatureGroup cfg : category.categoryFeatureGroup) {
                for(Feature f:cfg.featureList) {
                    if(f.ID == featureId) {
                        return f;
                    }
                }
            }
        }
        return null;

    }

    void endElement(String ns, String localName, String qName) {

         if ("ProductRelated" == qName) {
            inProductRelated = false

        }

        if ("ProductFeature" == qName) {
            if (productFeature.feature != null) {
                product.productFeatures.add(productFeature);
            }
            productFeature = null;
            feature = null;
        }

        if("Product" == qName  && !inProductRelated) {

            Category c = categoryMap.get(product.CategoryID);
            if (c != null) {
                c.product.add(product);
                product.CategoryName = (c.name == null ? c.id : c.name); //category name and product type
                println("Added product " + product.Prod_id + " with " + product.productFeatures.size() +  " features to category " + product.CategoryName)
            }

        }

    }


}