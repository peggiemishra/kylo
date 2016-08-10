package com.thinkbiganalytics.feedmgr.service.category;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.thinkbiganalytics.feedmgr.rest.model.FeedCategory;
import com.thinkbiganalytics.feedmgr.rest.model.FeedSummary;
import com.thinkbiganalytics.feedmgr.service.UserPropertyTransform;
import com.thinkbiganalytics.feedmgr.service.feed.FeedModelTransform;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryNotFoundException;
import com.thinkbiganalytics.metadata.api.extension.UserFieldDescriptor;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategory;
import com.thinkbiganalytics.metadata.api.feedmgr.category.FeedManagerCategoryProvider;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Transforms categories between Feed Manager and Metadata formats.
 */
public class CategoryModelTransform {

    @Inject
    FeedModelTransform feedModelTransform;


    @Inject
    FeedManagerCategoryProvider categoryProvider;

    public Function<FeedManagerCategory, FeedCategory>
            DOMAIN_TO_FEED_CATEGORY =
            new Function<FeedManagerCategory, FeedCategory>() {
                @Override
                public FeedCategory apply(FeedManagerCategory domainCategory) {
                    if (domainCategory != null) {
                        FeedCategory category = new FeedCategory();
                        category.setId(domainCategory.getId().toString());
                        if (domainCategory.getFeeds() != null) {
                            List<FeedSummary> summaries = feedModelTransform.domainToFeedSummary(domainCategory.getFeeds());
                            category.setFeeds(summaries);
                            category.setRelatedFeeds(summaries.size());
                        }
                        category.setIconColor(domainCategory.getIconColor());
                        category.setIcon(domainCategory.getIcon());
                        category.setName(domainCategory.getDisplayName());
                        category.setSystemName(domainCategory.getName());
                        category.setDescription(domainCategory.getDescription());
                        category.setCreateDate(domainCategory.getCreatedTime() != null ? domainCategory.getCreatedTime().toDate() : null);
                        category.setUpdateDate(domainCategory.getModifiedTime() != null ? domainCategory.getModifiedTime().toDate() : null);

                        // Transforms the domain user-defined fields to Feed Manager user-defined fields
                        category.setUserFields(UserPropertyTransform.toUserFields(categoryProvider.getFeedUserFields(domainCategory.getId())));

                        // Transforms the domain user-defined properties to Feed Manager user-defined properties
                        final Set<UserFieldDescriptor> userFields = categoryProvider.getUserFields();
                        category.setUserProperties(UserPropertyTransform.toFeedManagerProperties(userFields, domainCategory.getUserProperties()));

                        return category;
                    } else {
                        return null;
                    }
                }
            };

    public Function<FeedManagerCategory, FeedCategory>
            DOMAIN_TO_FEED_CATEGORY_SIMPLE =
            new Function<FeedManagerCategory, FeedCategory>() {
                @Override
                public FeedCategory apply(FeedManagerCategory domainCategory) {
                    if (domainCategory != null) {
                        FeedCategory category = new FeedCategory();
                        category.setId(domainCategory.getId().toString());
                        category.setIconColor(domainCategory.getIconColor());
                        category.setIcon(domainCategory.getIcon());
                        category.setName(domainCategory.getDisplayName());
                        category.setSystemName(domainCategory.getName());
                        category.setDescription(domainCategory.getDescription());
                        category.setCreateDate(domainCategory.getCreatedTime() != null ? domainCategory.getCreatedTime().toDate() : null);
                        category.setUpdateDate(domainCategory.getModifiedTime() != null ? domainCategory.getModifiedTime().toDate() : null);
                        return category;
                    } else {
                        return null;
                    }
                }
            };

    public Function<FeedCategory, FeedManagerCategory>
            FEED_CATEGORY_TO_DOMAIN =
            new Function<FeedCategory, FeedManagerCategory>() {
                @Override
                public FeedManagerCategory apply(FeedCategory feedCategory) {
                    Category.ID domainId = feedCategory.getId() != null ? categoryProvider.resolveId(feedCategory.getId()) : null;
                    FeedManagerCategory category = null;
                    if (domainId != null) {
                        category = (FeedManagerCategory) categoryProvider.findById(domainId);
                    }

                    if (category == null) {
                        category = categoryProvider.ensureCategory(feedCategory.getSystemName());
                    }
                    if (category == null) {
                        throw new CategoryNotFoundException("Unable to find Category ", domainId);
                    }
                    domainId = category.getId();
                    feedCategory.setId(domainId.toString());
                    category.setDisplayName(feedCategory.getName());
                    category.setName(feedCategory.getSystemName());
                    category.setDescription(feedCategory.getDescription());
                    category.setIcon(feedCategory.getIcon());
                    category.setIconColor(feedCategory.getIconColor());
                    category.setCreatedTime(new DateTime(feedCategory.getCreateDate()));
                    category.setModifiedTime(new DateTime(feedCategory.getUpdateDate()));

                    // Transforms the Feed Manager user-defined properties to domain user-defined properties
                    if (feedCategory.getUserProperties() != null) {
                        category.setUserProperties(UserPropertyTransform.toMetadataProperties(feedCategory.getUserProperties()));
                    }

                    return category;
                }
            };

    public List<FeedCategory> domainToFeedCategory(Collection<FeedManagerCategory> domain) {
        return new ArrayList<>(Collections2.transform(domain, DOMAIN_TO_FEED_CATEGORY));
    }

    public List<FeedCategory> domainToFeedCategorySimple(Collection<FeedManagerCategory> domain) {
        return new ArrayList<>(Collections2.transform(domain, DOMAIN_TO_FEED_CATEGORY_SIMPLE));
    }

    public List<FeedManagerCategory> feedCategoryToDomain(Collection<FeedCategory> feedCategories) {
        return new ArrayList<>(Collections2.transform(feedCategories, FEED_CATEGORY_TO_DOMAIN));
    }
}
