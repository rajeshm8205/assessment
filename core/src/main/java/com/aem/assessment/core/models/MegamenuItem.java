package com.aem.assessment.core.models;

import com.adobe.cq.wcm.core.components.models.ListItem;
import com.day.cq.wcm.api.Page;

import java.util.List;

import org.osgi.annotation.versioning.ConsumerType;

@ConsumerType
public interface MegamenuItem extends ListItem {
    /**
     * @deprecated
     */
    @Deprecated
    default Page getPage() {
        return null;
    }

    default boolean isActive() {
        return false;
    }

    default boolean isCurrent() {
        return false;
    }

    default List<MegamenuItem> getChildren() {
        return null;
    }

    default int getLevel() {
        return 0;
    }
}
